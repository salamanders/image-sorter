package info.benjaminhill.imagesorter.extract;

import info.benjaminhill.imagesorter.FileMetadata;

public interface DateFinder {
  boolean appliesToFile(final FileMetadata fm);

  void findDate(final FileMetadata fm) throws DateFinderException;
}
