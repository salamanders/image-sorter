package info.benjaminhill.imagesorter;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

/**
 * Everything we know (so far) about a file, and our confidence about it
 * 
 * @author benjaminhill@gmail.com
 *
 */
public class FileMetadata {
  private final Calendar cal = Calendar.getInstance();
  private double confidence = 0;
  private final Path startLocation;

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

  public FileMetadata(final Path startLocation) {
    super();
    this.startLocation = startLocation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((startLocation == null) ? 0 : startLocation.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FileMetadata other = (FileMetadata) obj;
    if (startLocation == null) {
      if (other.startLocation != null)
        return false;
    } else if (!startLocation.equals(other.startLocation))
      return false;
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
