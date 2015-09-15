package info.benjaminhill.imagesorter.extract;

/**
 * "I had a problem finding the date" Yes I'm being lazy using exceptions.
 *
 * @author benjaminhill@gmail.com
 *
 */
public class DateFinderException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   *
   * @param message
   */
  public DateFinderException(final String message) {
    super(message);
  }

  /**
   *
   * @param message
   * @param cause
   */
  public DateFinderException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   *
   * @param cause
   */
  public DateFinderException(final Throwable cause) {
    super(cause);
  }

}
