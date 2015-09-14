package info.benjaminhill.imagesorter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.drew.imaging.ImageProcessingException;
import com.google.common.collect.ImmutableSet;

import info.benjaminhill.imagesorter.extract.DateFinder;
import info.benjaminhill.imagesorter.extract.DateFinderException;
import info.benjaminhill.imagesorter.extract.FindDateByExec;
import info.benjaminhill.imagesorter.extract.FindDateByEXIF;

/**
 * Main sorter, gets the folder from the user via a popup, collects the files,
 * detects the @FileMetadata, and moves them to a "sorted" subfolder
 * 
 * @author benjaminhill@gmail.com
 *
 */
public class ImageSorter {
  private static final Logger LOG = Logger.getLogger(ImageSorter.class.getName());
  private final Path root;

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

  /**
   *
   * @param root
   */
  public ImageSorter(final Path root) {
    this.root = root;
  }

  void run() throws IOException {
    final Set<FileMetadata> images = ImageFileUtils.getImages(root).stream()
        .map(imagePath -> new FileMetadata(root, imagePath)).collect(Collectors.toSet());
    LOG.info("Files Found:" + images.size());

    final Set<DateFinder> finders = ImmutableSet.of(new FindDateByEXIF(), new FindDateByExec());

    for (final FileMetadata image : images) {
      for (final DateFinder finder : finders) {
        if (image.getConfidence() < .5) {
          try {
            if (finder.appliesToFile(image)) {
              finder.findDate(image);
              System.out.format("%s:%s-%s%n", finder.getClass().getSimpleName(), image.getYear(), image.getMonth());
              image.setConfidence(.75);
            }
          } catch (final DateFinderException dfe) {
            System.err.println(dfe);
          }
        }
      }
    }
  }
}
