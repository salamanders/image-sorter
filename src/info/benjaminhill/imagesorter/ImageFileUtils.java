package info.benjaminhill.imagesorter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.JFileChooser;

import com.google.common.collect.ImmutableSet;

/**
 * Handful of static file walkers, extension filters
 * 
 * @author benjaminhill@gmail.com
 *
 */
public class ImageFileUtils {

  public static Path getStartingPath() {
    final JFileChooser jfc = new JFileChooser();
    jfc.setDialogTitle("Choose your folder of images and movies to sort");
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setApproveButtonText("Start in this Folder");
    final int returnVal = jfc.showOpenDialog(null);
    if (returnVal != JFileChooser.APPROVE_OPTION) {
      throw new RuntimeException("No directory chosen.");
    }
    return Paths.get(jfc.getSelectedFile().toURI());
  }

  private static final ImmutableSet<String> MEDIA_EXTENSIONS = ImmutableSet.of("jpg", "jpeg", "mov", "mpg", "avi",
      "3gp", "wmv", "mp4", "psd", "png", "cr2", "crw", "tif", "tiff", "m4v", "webm", "gif");
  private static final ImmutableSet<String> IGNORE_ENDINGS = ImmutableSet.of("txt", "log", "syncignore", "ini", "db",
      "dat", "ds_store", "lnk", "doc", "syncid");

  public static SortedSet<Path> getImages(final Path root) throws IOException {
    final SortedSet<Path> images = new ConcurrentSkipListSet<>();
    final SimpleFileVisitor<Path> myVisitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path filePath, final BasicFileAttributes attrs) {
        if (!attrs.isRegularFile()) {
          return FileVisitResult.CONTINUE;
        }

        if (filePath.toString().contains("sorted") || filePath.toString().toLowerCase().contains("thumbnail")) {
          return FileVisitResult.CONTINUE;
        }

        final String lowerFilename = filePath.getFileName().toString().toLowerCase();
        final String extension = com.google.common.io.Files.getFileExtension(lowerFilename);
        if (!MEDIA_EXTENSIONS.contains(extension)) {
          if (!IGNORE_ENDINGS.contains(extension)) {
            System.err.println("Skipping unknown type " + filePath.toString());
          }
          return FileVisitResult.CONTINUE;
        }
        // Success!
        images.add(filePath);
        return FileVisitResult.CONTINUE;
      }
    };
    Files.walkFileTree(root, myVisitor);
    return images;
  }

}
