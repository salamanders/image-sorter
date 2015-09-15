package info.benjaminhill.imagesorter.extract;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.google.common.collect.ImmutableSet;

import info.benjaminhill.imagesorter.FileMetadata;

/**
 *
 * @author benjaminhill@gmail.com
 */
public class FindDateByEXIF implements DateFinder {

  private static final Set<DateFormat> MATCHERS = ImmutableSet.of(new SimpleDateFormat("yyyy:MM:dd hh:mm:ss"),
      new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy"), new SimpleDateFormat("dd.MM.yyyy hh:mm:ss"),
      new SimpleDateFormat("yyyy:MM:dd"));

  /**
   *
   * @param fm
   * @return
   */
  @Override
  public boolean appliesToFile(final FileMetadata fm) {
    final File file = fm.getStartLocation().toFile();
    return file.getName().toLowerCase().endsWith("jpg") || file.getName().toLowerCase().endsWith("jpeg");
  }

  /**
   *
   * @param fm
   * @return
   * @throws DateFinderException
   */
  @Override
  public Calendar findDate(final FileMetadata fm) throws DateFinderException {
    final File file = fm.getStartLocation().toFile();
    Date result = null;
    try {
      final Metadata metadata = ImageMetadataReader.readMetadata(file);
      for (final Directory directory : metadata.getDirectories()) {
        for (final Tag tag : directory.getTags()) {

          // System.out.println("Tag:" + tag.getTagName() + "=" +
          // tag.getDescription());
          if (tag.getTagName().contains("Date") && !tag.getTagName().contains("Mode")
              && !tag.getTagName().contains("Profile") && tag.getDescription() != null
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
    } catch (final ImageProcessingException | IOException e) {
      // ignore
    }
    if (result == null) {
      throw new DateFinderException(String.format("No metadata found within %s", fm.getStartLocation().toString()));
    }
    final Calendar cal = Calendar.getInstance();
    cal.setTime(result);
    return cal;
  }

  /**
   *
   * @return
   */
  @Override
  public double getConfidence() {
    return 0.75;
  }

}
