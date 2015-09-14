package imagesorter;

import com.drew.imaging.ImageProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;

/**
 * Non-destructive sorting of images folder based on image creation dates.
 *
 * @author Benjamin Hill benjaminhill@gmail.com
 */
public class ImageSorter {

  private static final Pattern ONLY_DIGEST_PARENT = Pattern.compile("^\\d+$");
  private static final Pattern POINTLESS_PARENT = Pattern.compile("^\\d{1,4}[-\\._]\\d{1,2}[-\\._]\\d{1,4}$");
  private static final Pattern REMOVABLE_PARENT = Pattern.compile("^\\d{4}[-\\._]{0,1}\\d{0,2}[-\\._]{0,1}(.+)$");





  private static Path getTargetFolder(final Path filePath, final BasicFileAttributes attrs) throws ImageProcessingException, IOException {
    final Calendar bestDate = ImageDateExtractor.getBestDate(filePath, attrs);
    if (bestDate == null) {
      return null;
    }
    String newFolder = String.format("%d%s%02d%s", bestDate.get(Calendar.YEAR), File.separator, bestDate.get(Calendar.MONTH) + 1, File.separator);
    final String parentName = filePath.getParent().getFileName().toString();

    // Try to salvage some of the parent folder name
    if (!POINTLESS_PARENT.matcher(parentName).matches() && !ImageDateExtractor.YEAR_MONTH_MATCHER.matcher(parentName).matches()) {
      final Matcher m = REMOVABLE_PARENT.matcher(parentName);
      if (m.matches()) {
        newFolder += m.group(1) + File.separator;
      } else {
        if (ONLY_DIGEST_PARENT.matcher(parentName).matches()) {
          // ignore
        } else {
          newFolder += filePath.getParent().getFileName() + File.separator;
        }
      }
    }

    newFolder = newFolder.replaceAll(" ", "");

    return Paths.get(newFolder);
  }



 

      try {
        // Relative from root
        final Path targetFolder = getTargetFolder(filePath, attrs);

        // If you don't know where, don't try.
        if (targetFolder == null) {
          System.out.println("WARNING: Don't know where to place:" + filePath.toString());
          return FileVisitResult.CONTINUE;
        }

        switch (FileMover.moveFileWithoutOverwrite(filePath, root.resolve("sorted").resolve(targetFolder), false)) {
          case MOVED:
            break;
          case EXISTS:
            // TODO: Check for size matches in other file names
            FileMover.moveFileWithoutOverwrite(filePath, root.resolve("sorted").resolve(targetFolder), true);
            break;
          case FILE_SIZE_MATCH:
            FileMover.moveFileWithoutOverwrite(filePath, root.resolve("trash").resolve(targetFolder), true);
            break;
          default:
            throw new IllegalStateException("How did we get here");
        }

        

      } catch (ImageProcessingException | IOException ex) {
        System.err.println("Error during moving: " + ex + " for " + filePath.toString());
      }
      return FileVisitResult.CONTINUE;
    }
  };



}
