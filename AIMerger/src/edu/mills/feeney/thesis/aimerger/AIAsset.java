package edu.mills.feeney.thesis.aimerger;

/**
 * Class to define an App Inventor Asset by its path, name and if it is to be merged.
 * 
 * @author Kate Feeney, feeney.kate@gmail.com 
 */
public class AIAsset {
  
  // Backing for the asset's directory path from project file
  private String assetPath;
  
  // Backing for the asset's name
  private String assetName;

  /**
   * Creates a new AIAsset.
   *
   * @param String  assetPath, string of the asset's directory path from project file
   */
  public AIAsset(String assetPath) {
    this.assetPath = assetPath;  
    // The assetName is the name of the asset's file.
    this.assetName = assetPath.substring(assetPath.lastIndexOf('/') + 1,
        assetPath.lastIndexOf('.')); 
  }

  /**
   * Returns an AIAsset's directory path from the project file.
   * 
   * @return string of AIAsset's directory path from the project file
   */
  public String getPath() {
    return assetPath;
  }

  /**
   * Sets the AIAsset's path.
   *
   * @param String  assetPath, string of the asset's directory path from project file
   */
  public void setPath(String assetPath) {
    this.assetPath = assetPath;
  }

  /**
   * Returns the AIAsset's name
   * 
   * @return string of AIAsset's name
   */
  public String getName() {
    return assetName;
  }

  /**
   * Sets the AIAsset's name.
   *
   * @param String  assetName
   */
  public void setName(String assetName) {
    this.assetName = assetName;
  }
}
