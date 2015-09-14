package imagesorter;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Safe moving of files with optional name increment.
 *
 * @author benjamin
 */
public class FileMover {

  private static final Logger LOG = Logger.getLogger(FileMover.class.getName());
  private static final boolean isReal = true;

  /**
   * Increment the file name for when you are sure you want to move to a folder
   */
  private static File findFirstEmptySlot(final Path filePath, final Path targetFolder) {
    // http://stackoverflow.com/questions/4545937/java-splitting-the-filename-into-a-base-and-extension
    String[] tokens = filePath.getFileName().toString().split("\\.(?=[^\\.]+$)");
    if (tokens.length != 2) {
      throw new IllegalArgumentException("Unable to split:" + filePath.getFileName());
    }
    for (int i = 0; i < 10_000; i++) {
      final File destination = targetFolder.resolve(filePath).resolve(
              (i == 0 ? filePath.getFileName().toString() : String.format("%s_%04d.%s", tokens[0], i, tokens[1]))).toFile();
      if (!destination.exists()) {
        return destination;
      }
    }
    throw new IllegalArgumentException("All spaces taken when moving:" + filePath.getFileName() + " to " + targetFolder.toString());
  }

  /**
   *
   * @param filePath
   * @param targetFolder
   * @param findEmptySpot
   * @return
   */
  public static MoveStates moveFileWithoutOverwrite(final Path filePath, final Path targetFolder, final boolean findEmptySpot) {
    if (filePath.startsWith(targetFolder)) {
      LOG.log(Level.INFO, "Can''t move file onto itself:{0}", filePath);
      return MoveStates.SELF;
    }

    File destination = targetFolder.resolve(filePath.getFileName().toString()).toFile();
    final File destinationFolder = targetFolder.toFile();
    long myFileSize = filePath.toFile().length();
    MoveStates result = MoveStates.MOVED;
    if (destination.exists()) {
      if (!findEmptySpot) {
        if (myFileSize == destination.length()) {
          return MoveStates.FILE_SIZE_MATCH;
        }
        return MoveStates.EXISTS;
      }
      destination = findFirstEmptySlot(filePath, targetFolder);
      result = MoveStates.MOVED_EMPTY_SLOT;
    }

    destinationFolder.mkdirs();
    if (!destinationFolder.canWrite()) {
      throw new RuntimeException("Unable to make folder:" + targetFolder);
    }

    LOG.log(Level.INFO, "Moving file:\t{0}\t{1}", new Object[]{filePath, destination});

    if (isReal) {
      if (!filePath.toFile().renameTo(destination)) {
        System.err.format("ERROR: Unable to move '%s' to '%s'%n", filePath.toString(), destination);
      }
    }

    System.out.format("INFO: moved '%s' to '%s' : %s%n",  filePath.toString(), destination.getAbsolutePath(), result.toString());

    return result;
  }

  private FileMover() {
    // empty
  }

}
