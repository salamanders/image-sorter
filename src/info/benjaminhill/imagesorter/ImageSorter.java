package info.benjaminhill.imagesorter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.drew.imaging.ImageProcessingException;
import com.google.common.collect.ImmutableSet;

import info.benjaminhill.imagesorter.extract.DateFinder;
import info.benjaminhill.imagesorter.extract.DateFinderException;
import info.benjaminhill.imagesorter.extract.FindDateByEXIF;
import info.benjaminhill.imagesorter.extract.FindDateByExec;

/**
 * Main sorter, gets the folder from the user via a popup, collects the files,
 * detects the @FileMetadata, and moves them to a "sorted" subfolder
 *
 * @author benjaminhill@gmail.com
 *
 */
public class ImageSorter {

  private static final Logger LOG = Logger.getLogger(ImageSorter.class.getName());

  /**
   * @param args
   *          the command line arguments
   * @throws ImageProcessingException
   * @throws IOException
   */
  public static void main(final String[] args) throws ImageProcessingException, IOException {
    final ImageSorter is = new ImageSorter(ImageFileUtils.getStartingPath());
    is.run();
  }

  private final Path root;

  /**
   *
   * @param root
   */
  public ImageSorter(final Path root) {
    this.root = root;
  }

  void run() throws IOException {
    final Set<DateFinder> finders = ImmutableSet.of(new FindDateByEXIF(), new FindDateByExec());

    final Set<FileMetadata> allMD = ImageFileUtils.getImages(root).stream()
        .map(imagePath -> new FileMetadata(root, imagePath)).collect(Collectors.toSet());
    LOG.log(Level.INFO, "Files Found:{0}", allMD.size());

    allMD.stream().map((fm) -> {
      finders.stream().filter((finder) -> (fm.getConfidence() < finder.getConfidence()))
          .filter((finder) -> finder.appliesToFile(fm)).forEach((finder) -> {
        try {
          final Calendar cal = finder.findDate(fm);
          fm.setCalendar(cal);
          fm.setConfidence(finder.getConfidence());
        } catch (final IllegalArgumentException | DateFinderException dfe) {
          System.err.println(dfe);
        }
      });
      return fm;
    }).filter((fm) -> (fm.getConfidence() > 0)).forEach((fm) -> {
      fm.move();
    });
  }
}
