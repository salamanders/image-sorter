package info.benjaminhill.imagesorter;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Everything we know (so far) about a file, and our confidence about it
 * 
 * @author benjaminhill@gmail.com
 *
 */
public class FileMetadata {
  private final Calendar cal = Calendar.getInstance();
  private double confidence = 0;
  private final Path root, startLocation;

  public void setCalendar(final Date newDate) throws IllegalArgumentException {
    testDate(newDate);
    cal.setTime(newDate);
  }

  public int getYear() {
    return cal.get(Calendar.YEAR);
  }

  public int getMonth() {
    return cal.get(Calendar.MONTH) + 1;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public Path getStartLocation() {
    return startLocation;
  }
  
  public Path getDesiredDestination(final int offset) {
    Preconditions.checkState(offset>=0, "Offset must be zero or positive.");
    
    Path result = root;
    result = result.resolve("sorted");
    result = result.resolve(String.valueOf(cal.get(Calendar.YEAR)));
    result = result.resolve(String.format("%02d", cal.get(Calendar.MONTH) + 1));
    
    final String fileName = Files.getNameWithoutExtension(startLocation.toString());
    Preconditions.checkState(!fileName.isEmpty(), "Unable to get file name of " + startLocation);
    
    final String extension = Files.getFileExtension(startLocation.toString());
    Preconditions.checkState(!extension.isEmpty(), "Unable to get extension of " + startLocation);
    
    final String fileNameMerged =  offset==0? String.format("%s.%s", fileName, extension):
        String.format("%s_%04d.%s", fileName, offset, extension);

    result = result.resolve(fileNameMerged);
    return result;
  }

  public void move() {
    final File destinationDir = getDesiredDestination(0).getParent().toFile();
    if(!destinationDir.exists()) {
      destinationDir.mkdirs();
      Preconditions.checkState(destinationDir.canWrite(), "Unable to make folder:" + destinationDir);      
    }
    
    for (int i = 0; i < 10_000; i++) {
      final File desiredDest = getDesiredDestination(i).toFile();
      if(!desiredDest.exists()) {
        if (!root.resolve(startLocation).toFile().renameTo(desiredDest)) {
          System.err.format("ERROR: Unable to move '%s' to '%s'%n", root.resolve(startLocation), desiredDest);
        }
      }
    }
  }
  
  public FileMetadata(final Path root, final Path startLocation) {
    super();
    this.root = root;
    this.startLocation = startLocation;
  }



  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final FileMetadata other = (FileMetadata) obj;
    if(!root.equals(other.root)) {
      return false;
    }
    if(!startLocation.equals(other.startLocation)) {
      return false;
    }
    return true;
  }

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
  private static void testDate(final Date date) {
    if (date == null) {
      throw new IllegalArgumentException("Date was null");
    }
    if (EARLIEST_DATE.get().after(date)) {
      throw new IllegalArgumentException(String.format("Earliest date %s was after %s", EARLIEST_DATE, date));
    }
    if (LATEST_DATE.get().before(date)) {
      throw new IllegalArgumentException(String.format("Latest date %s was before %s", LATEST_DATE, date));
    }
  }

}
