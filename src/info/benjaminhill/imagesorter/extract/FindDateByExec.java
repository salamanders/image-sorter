package info.benjaminhill.imagesorter.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import info.benjaminhill.imagesorter.FileMetadata;

public class FindDateByExec implements DateFinder {

  @Override
  public boolean appliesToFile(final FileMetadata fm) {
    return true;
  }

  @Override
  public void findDate(final FileMetadata fm) throws DateFinderException {
    final Calendar cal = getCreationDate(fm.getStartLocation().toString());
    // Success! Maybe.
    try {
      fm.setCalendar(cal.getTime());
    } catch (final IllegalArgumentException iae) {
      throw new DateFinderException(iae);
    }
  }

  private static final ThreadLocal<SimpleDateFormat> TFORMATTER = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss +SSSS");
    }
  };
  private static final Logger LOG = Logger.getLogger(FindDateByExec.class.getName());

  /**
   * Run command line (may be buggy)
   *
   * @param args
   * @return
   */
  private static String exec(final String... args) {
    final StringBuilder result = new StringBuilder(1_024);
    try {
      final Runtime rt = Runtime.getRuntime();
      final Process proc = rt.exec(args);

      try (final InputStream stdis = proc.getInputStream();
          final InputStreamReader isr = new InputStreamReader(stdis);
          final BufferedReader br = new BufferedReader(isr);) {
        String line;
        while ((line = br.readLine()) != null) {
          if (result.length() > 0) {
            result.append(System.getProperty("line.separator"));
          }
          result.append(line);
        }
        final int exitVal = proc.waitFor();
      }
    } catch (InterruptedException | IOException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return result.toString();
  }

  private static Calendar getCreationDate(final String filePath) {
    try {

      final Calendar result = Calendar.getInstance();
      if (isMac()) {
        final String execResult = exec("mdls", "-name", "kMDItemContentCreationDate", "-raw", filePath);
        result.setTime(TFORMATTER.get().parse(execResult));
      } else if (isWindows()) {
        // http://www.mkyong.com/java/how-to-get-the-file-creation-date-in-java/
        final String execResult = exec("cmd", "/c", "dir", filePath, "/tc");
        final String[] lines = execResult.split(System.getProperty("line.separator"));
        final StringTokenizer st = new StringTokenizer(lines[lines.length - 1]);
        final String date = st.nextToken();// Get date
        System.out.println(date);
        throw new java.lang.IllegalArgumentException("Still working on Windows support, sorry!");
      }
      return result;
    } catch (final ParseException ex) {
      System.err.println(ex);
    }
    return null;
  }

  private static boolean isMac() {
    return System.getProperty("os.name").toLowerCase().contains("mac");
  }

  private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }

}
