package imagesorter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tries many different ways to get the creation date of an image.
 *
 * @author benjamin
 */
public class ImageDateExtractor {

  private static final Set<DateFormat> MATCHERS = new HashSet<>();

  /**
   *
   */
  public static final Pattern YEAR_MONTH_MATCHER = Pattern.compile("^(\\d{4})\\D(\\d{2}).*$");
  private static final Logger LOG = Logger.getLogger(ImageDateExtractor.class.getName());

  private static Calendar getEarliestFileDate(final File file) throws ImageProcessingException, IOException {
    Date result = null;
    if (file.getName().toLowerCase().endsWith("jpg") || file.getName().toLowerCase().endsWith("jpeg")) {
      final Metadata metadata = ImageMetadataReader.readMetadata(file);
      for (final Directory directory : metadata.getDirectories()) {
        for (final Tag tag : directory.getTags()) {

          // System.out.println("Tag:" + tag.getTagName() + "=" +
          // tag.getDescription());
          if (tag.getTagName().contains("Date")
                  && !tag.getTagName().contains("Mode")
                  && !tag.getTagName().contains("Profile")
                  && tag.getDescription() != null
                  && tag.getDescription().length() > 3) {
            for (final DateFormat df : MATCHERS) {
              try {
                final Date d = df.parse(tag.getDescription());
                if (result == null || d.before(result)) {
                  result = d;
                }
                break;
              } catch (final ParseException ex) {
                // ignore
              }
            }
          }
        }
      }
      if (result == null) {
        return null;
      }
      final Calendar cresult = Calendar.getInstance();
      cresult.setTime(result);
      if (isReasonableDate(cresult)) {
        return cresult;
      }
    }
    return null;
  }

  private static Calendar getPathDate(final Path filePath) {
    for (Path segment = filePath; segment != null && segment.getFileName() != null; segment = segment.getParent()) {
      final Matcher m = YEAR_MONTH_MATCHER.matcher(segment.getFileName().toString());
      if (m.matches()) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
        cal.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
        if (isReasonableDate(cal)) {
          return cal;
        }
      }
    }
    return null;
  }

  private static Calendar getDate(final Path filePath) {
    return ExecWrapper.getCreationDate(filePath.toString());
  }

  /**
   *
   * @param filePath
   * @param attrs
   * @return
   * @throws ImageProcessingException
   * @throws IOException
   */
  public static Calendar getBestDate(final Path filePath, final BasicFileAttributes attrs) throws ImageProcessingException, IOException {
    final Calendar mdlsDate = ImageDateExtractor.getDate(filePath);
    final Calendar attrsDate = Calendar.getInstance();
    attrsDate.setTimeInMillis(attrs.creationTime().toMillis());
    final Calendar exifDate = ImageDateExtractor.getEarliestFileDate(filePath.toFile());
    final Calendar pathDate = ImageDateExtractor.getPathDate(filePath);

    Calendar bestDate = null;

    if (ImageDateExtractor.isReasonableDate(mdlsDate)) {
      bestDate = mdlsDate;
    }

    if (bestDate == null && ImageDateExtractor.isReasonableDate(attrsDate)) {
      bestDate = attrsDate;
    }

    if (ImageDateExtractor.isReasonableDate(exifDate) && (bestDate == null || exifDate.before(bestDate))) {
      bestDate = exifDate;
    }

    if (bestDate == null && ImageDateExtractor.isReasonableDate(pathDate)) {
      bestDate = pathDate;
    }

    return bestDate;
  }

  private ImageDateExtractor() {
    // empty
  }
}
