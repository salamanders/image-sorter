
package imagesorter;

/**
 * What can happen to a file when it moves
 * @author benjamin
 */
public enum MoveStates {

  /**
   *
   */
  MOVED, 

  /**
   *
   */
  MOVED_EMPTY_SLOT, 

  /**
   *
   */
  EXISTS, 

  /**
   *
   */
  FILE_SIZE_MATCH, 

  /**
   *
   */
  SELF
  
}
