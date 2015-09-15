package info.benjaminhill.imagesorter;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Everything we know (so far) about a file, and our confidence about it
 *
 * @author benjaminhill@gmail.com
 *
 */
public class FileMetadata {
  private static final ThreadLocal<Calendar> EARLIEST_DATE = new ThreadLocal<Calendar>() {
    @Override
    protected Calendar initialValue() {
      final Calendar cal = Calendar.getInstance();
      cal.set(1_997, 1, 1);
      return cal;
    }
  };
  private static final ThreadLocal<Calendar> LATEST_DATE = new ThreadLocal<Calendar>() {
    @Override
    protected Calendar initialValue() {
      final Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(System.currentTimeMillis());
      return cal;
    }
  };

  /**
   * Don't allow a bad date to be set.
   *
   * @param date
   */
  private static void testCal(final Calendar cal) {
    if (cal == null) {
      throw new IllegalArgumentException("Date was null");
    }
    if (EARLIEST_DATE.get().after(cal)) {
      throw new IllegalArgumentException(String.format("Earliest date %s was after %s", EARLIEST_DATE, cal));
    }
    if (LATEST_DATE.get().before(cal)) {
      throw new IllegalArgumentException(String.format("Latest date %s was before %s", LATEST_DATE, cal));
    }
  }

  private final Calendar cal = Calendar.getInstance();
  private double confidence = 0;
  private final Path root, startLocation;

  /**
   *
   * @param root
   * @param startLocation
   */
  public FileMetadata(final Path root, final Path startLocation) {
    super();
    this.root = root;
    this.startLocation = startLocation;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FileMetadata other = (FileMetadata) obj;
    if (!root.equals(other.root)) {
      return false;
    }
    return startLocation.equals(other.startLocation);
  }

  /**
   *
   * @return
   */
  public double getConfidence() {
    return confidence;
  }

  /**
   *
   * @param offset
   * @return
   */
  public Path getDesiredDestination(final int offset) {
    Preconditions.checkState(offset >= 0, "Offset must be zero or positive.");

    Path result = root;
    result = result.resolve("sorted");
    result = result.resolve(String.valueOf(cal.get(Calendar.YEAR)));
    result = result.resolve(String.format("%02d", cal.get(Calendar.MONTH) + 1));

    final String fileName = Files.getNameWithoutExtension(startLocation.toString());
    Preconditions.checkState(!fileName.isEmpty(), "Unable to get file name of " + startLocation);

    final String extension = Files.getFileExtension(startLocation.toString());
    Preconditions.checkState(!extension.isEmpty(), "Unable to get extension of " + startLocation);

    final String fileNameMerged = offset == 0 ? String.format("%s.%s", fileName, extension)
        : String.format("%s_%04d.%s", fileName, offset, extension);

    result = result.resolve(fileNameMerged);
    return result;
  }

  /**
   *
   * @return
   */
  public int getMonth() {
    return cal.get(Calendar.MONTH) + 1;
  }

  /**
   *
   * @return
   */
  public Path getStartLocation() {
    return startLocation;
  }

  /**
   *
   * @return
   */
  public int getYear() {
    return cal.get(Calendar.YEAR);
  }

  /**
   *
   */
  public void move() {
    final File destinationDir = getDesiredDestination(0).getParent().toFile();
    if (!destinationDir.exists()) {
      destinationDir.mkdirs();
      Preconditions.checkState(destinationDir.canWrite(), "Unable to make folder:" + destinationDir);
    }

    for (int i = 0; i < 10_000; i++) {
      final File desiredDest = getDesiredDestination(i).toFile();
      if (!desiredDest.exists()) {
        if (!root.resolve(startLocation).toFile().renameTo(desiredDest)) {
          System.err.format("ERROR: Unable to move '%s' to '%s'%n", root.resolve(startLocation), desiredDest);
        }
      }
    }
  }

  /**
   *
   * @param newCal
   * @throws IllegalArgumentException
   */
  public void setCalendar(final Calendar newCal) throws IllegalArgumentException {
    testCal(newCal);
    cal.setTime(newCal.getTime());
  }

  /**
   *
   * @param confidence
   */
  public void setConfidence(final double confidence) {
    this.confidence = confidence;
  }

}
