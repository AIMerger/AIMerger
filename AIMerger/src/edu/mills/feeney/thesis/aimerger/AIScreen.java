package edu.mills.feeney.thesis.aimerger;

/**
 * Class to define an App Inventor Screen by its path, name and if it is to be merged.
 * 
 * @author Kate Feeney, feeney.kate@gmail.com 
 */
public class AIScreen {

  // Backing for the screen's directory path from project file
  private String screenPath;

  // Backing for the screen's name
  private String screenName;

  /**
   * Creates a new AIScreen.
   *
   * @param String  screenPath, string of the screen's directory path from project file
   */
  public AIScreen(String screenPath) {
    this.screenPath = screenPath;  
    // The screenName is the name of the screen's file.
    this.screenName = screenPath.substring(screenPath.lastIndexOf('/') + 1,
        screenPath.lastIndexOf('.'));
  }

  /**
   * Returns an AIScreen's directory path from the project file.
   * 
   * @return string of AIScreen's directory path from the project file
   */
  public String getPath() {
    return screenPath;
  }

  /**
   * Sets the AIScreen's path.
   *
   * @param String  screenPath, string of the screen's directory path from project file
   */
  public void setPath(String screenPath) {
    this.screenPath = screenPath;
  }

  /**
   * Returns the AIScreen's name.
   * 
   * @return string of AIScreen's name
   */
  public String getName() {
    return screenName;
  }

  /**
   * Sets the AIScreen's name.
   *
   * @param String  screenName
   */
  public void setName(String screenName) {
    this.screenName = screenName;
  }
}
