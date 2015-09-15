package info.benjaminhill.imagesorter.extract;

import java.util.Calendar;

import info.benjaminhill.imagesorter.FileMetadata;

/**
 * Interface of all ways we can determine a date
 *
 * @author benjaminhill@gmail.com
 */
public interface DateFinder {

  /**
   *
   * @param fm
   * @return if this finder should be tried against the given file
   */
  boolean appliesToFile(final FileMetadata fm);

  /**
   *
   * @param fm
   * @return the date extracted from the file
   * @throws DateFinderException
   *           if there are any issues finding the date
   */
  Calendar findDate(final FileMetadata fm) throws DateFinderException;

  /**
   *
   * @return how confident this finder is
   */
  public double getConfidence();
}
