package edu.mills.feeney.thesis.aimerger;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AIMerger extends JFrame {

  // Action listener for the main project's browse button.
  private class MainProjectBrowseBActionListener implements ActionListener {
    // Action performed when button is  clicked.
    @Override
    public void actionPerformed(ActionEvent event) {
      String path;
      if ((path = getFileToOpen())==null) {
        return;
      }
      mainProjectTF.setText(path);
    }
  }

  //Action listener for the second project's browse button.
  private class SecondProjectBrowseBActionListener implements ActionListener {
    // Action performed when button is  clicked.
    @Override
    public void actionPerformed(ActionEvent event) {
      String path;
      if ((path = getFileToOpen())==null) {
        return;
      }
      secondProjectTF.setText(path);
    }       
  }

  //Action listener for the main project's load button.
  private class MainProjectLoadBActionListener implements ActionListener {
    // Action performed when button is  clicked.
    @Override
    public void actionPerformed(ActionEvent event) {
      // Create AIProject for the main project.
      mainProject = new AIProject(mainProjectTF.getText());
      // Display main project.
      if (mainProject.isValid()) {
        lowerLeftP.setVisible(true);
        updateMainProjectView();
        lowerCenterP.setVisible(lowerRightP.isVisible());
      } else {
        lowerLeftP.setVisible(false);
      }
    }
  }

  //Action listener for the second project's load button.
  private class SecondProjectLoadBActionListener implements ActionListener {
    // Action performed when button is  clicked.
    @Override
    public void actionPerformed(ActionEvent event) {
      secondProject = new AIProject(secondProjectTF.getText());
      if (secondProject.isValid()) {
        lowerRightP.setVisible(true);
        updateSecondProjectView();
        lowerCenterP.setVisible(lowerLeftP.isVisible());
      } else {
        lowerRightP.setVisible(false);
      }
    }
  }

  // Action listener for the merge button.
  private class MergeBActionListener implements ActionListener {

    // Action performed when button is  clicked.
    @Override
    public void actionPerformed(ActionEvent event) {

      if(checkDuplicates()){
        // List to hold files to be included in the new project from the main project.
        List <String> filesFromMainProject = new ArrayList<String>();
        // The properties file from the main project is always included in the new project.
        filesFromMainProject.add(mainProject.getPropertiesFilePath());

        // List to hold files to be included in the new project from the second project.
        List <String> filesFromSecondProject = new ArrayList<String>();

        // Temporary list to hold the name of files that have been checked to be included in 
        // the new project.
        LinkedList <String> checked = new LinkedList<String>();
        // checked is first the list of screens checked from the main project.
        checked = mainProjectScreensCBL.getChecked();
        // Add checked screens to the list of files to include from the main project.
        if (!checked.isEmpty()){
          for (AIScreen aiScreen: mainProject.getScreensList()) {
            if (checked.contains( aiScreen.getName())) {
              String path = aiScreen.getPath();
              filesFromMainProject.add(path);
              filesFromMainProject.add(path.substring(0, path.lastIndexOf(".scm")).concat(".blk"));
            }
          }
        }

        // checked is now the list of assets checked from the main project.
        checked = mainProjectAssetsCBL.getChecked();
        // Add checked assets to the list of files to include from the main project.
        if (!checked.isEmpty()) {
          for (AIAsset aiAsset: mainProject.getAssetsList()) {
            if (checked.contains( aiAsset.getName())) {
              filesFromMainProject.add(aiAsset.getPath());
            }
          }
        }

        // checked is now the list of screens checked from the second project.
        checked = secondProjectScreensCBL.getChecked();
        // Add checked screens to the list of files to include from the second project.
        if (!checked.isEmpty()){
          for (AIScreen aiScreen: secondProject.getScreensList()) {
            if (checked.contains( aiScreen.getName())) {
              String path = aiScreen.getPath();
              filesFromSecondProject.add(path);
              filesFromSecondProject.add(path.substring(0, 
                  path.lastIndexOf(".scm")).concat(".blk"));
            }
          }
        }

        // checked is now the list of assets checked from the second project.
        checked = secondProjectAssetsCBL.getChecked();
        // Add checked assets to the list of files to include from the second project.
        if (!checked.isEmpty()) {
          for (AIAsset aiAsset: secondProject.getAssetsList()) {
            if (checked.contains( aiAsset.getName())) {
              filesFromSecondProject.add(aiAsset.getPath());
            }
          }
        }

        try {
          getFileToSaveTo();
          ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(mergeProjectPath));
          ZipInputStream mainZipInput = new ZipInputStream(new BufferedInputStream(
              new FileInputStream(mainProject.getProjectPath())));
          ZipInputStream secondZipInput = new ZipInputStream(new BufferedInputStream(
              new FileInputStream(secondProject.getProjectPath())));

          byte [] buf = new byte[1024];

          // Write files from main project to new project.
          ZipEntry curEntry;
          while ((curEntry = mainZipInput.getNextEntry())!=null) {
            if (filesFromMainProject.contains(curEntry.getName())) {
              outZip.putNextEntry(curEntry);
              int len;
              while ((len = mainZipInput.read(buf))>0) {
                outZip.write(buf, 0, len);
              }
              outZip.closeEntry();
              mainZipInput.closeEntry();
            }
          }
          mainZipInput.close();

          // Write files from second project to new project.
          while ((curEntry = secondZipInput.getNextEntry())!=null) {
            if (filesFromSecondProject.contains(curEntry.getName())) {
              outZip.putNextEntry(curEntry);
              int len;
              while ((len = secondZipInput.read(buf))>0) {
                outZip.write(buf, 0, len);
              }
              outZip.closeEntry();
              secondZipInput.closeEntry();
            }
          }
          secondZipInput.close();
          outZip.close();
          successfulMerge();
        } catch (IOException e1) {
          JOptionPane.showMessageDialog(myCP,"Invalide file name.", "Inane error", 
              JOptionPane.ERROR_MESSAGE);
          actionPerformed(event);
        }
      }
    }    
  }

  /*
   * Resets UI to what appears when the project is loaded. 
   * If a project's path is passed then this is loaded as the main project.
   */
  private void resetAIMerger(String aiProjectPath) {
    mainProjectTF.setText(null);
    secondProjectTF.setText(null);
    lowerLeftP.setVisible(false);
    lowerRightP.setVisible(false);
    lowerCenterP.setVisible(false);
    if (aiProjectPath != null) {
      mainProjectTF.setText(aiProjectPath);
      mainProject = new AIProject(aiProjectPath);
      lowerLeftP.setVisible(true);
      updateMainProjectView();
    }   
  }

  /*
   * Informs the user their merge was successful and asks if they would like to merge 
   * another project.
   */
  private void successfulMerge() {
    switch (JOptionPane.showOptionDialog(myCP, "Projects Successfully Merged. " +
        "Would you like to Merge more Projects?", "Projects Merged", JOptionPane.YES_NO_OPTION,
        JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.YES_OPTION)) {
          default:
            // This should never happen
            throw new IllegalArgumentException("not an option");
          case JOptionPane.CLOSED_OPTION:
            closeApplication();
            break;
          case JOptionPane.NO_OPTION:
            closeApplication();
            break;
          case JOptionPane.YES_OPTION:
            mergeAnotherProject();
            break;
    }      
  }

  /*
   * Asks user if they want to merge another project to the recently merged project.
   */
  private void mergeAnotherProject() {
    switch (JOptionPane.showOptionDialog(myCP, "Would you like one of the projects to merge" +
        "to be the project you just created?", "Merge More Projects", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION)) {
          default:
            // This should never happen
            throw new IllegalArgumentException("not an option");
          case JOptionPane.CLOSED_OPTION:
            closeApplication();
            break;
          case JOptionPane.NO_OPTION:
            resetAIMerger(null); 
            break;
          case JOptionPane.YES_OPTION:
            resetAIMerger(mergeProjectPath); 
            break;
    }          
  }

  /*
   * Confirms the user wants to exit AIMerger.
   */
  private void closeApplication() {
    switch (JOptionPane.showOptionDialog(myCP, "Exit AIMerger?", "Exit", 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 
        JOptionPane.YES_OPTION)){
          default:
            // This should never happen
            throw new IllegalArgumentException("not an option");
          case JOptionPane.CLOSED_OPTION:
            successfulMerge();
            break;
          case JOptionPane.CANCEL_OPTION:
            successfulMerge();
            break;
          case JOptionPane.OK_OPTION:
            System.exit(0);
            break;
    }
  }

  private void getFileToSaveTo() {
    // Get new project's file path.
    JFileChooser mergeProjectFS = new JFileChooser();
    mergeProjectFS.setDialogType(JFileChooser.SAVE_DIALOG);
    int validPath = mergeProjectFS.showSaveDialog(myCP);
    if (validPath == JFileChooser.ERROR_OPTION || validPath == JFileChooser.CANCEL_OPTION) {
      return;
    } else {
      // The file must be a zip file.
      File mergeProjectFile = mergeProjectFS.getSelectedFile();
      mergeProjectPath = mergeProjectFile.getPath();
      if (!mergeProjectPath.endsWith(".zip")) {// want to make this ignore case
        if (mergeProjectPath.matches("(?i).*\\.zip")) {
          mergeProjectPath = mergeProjectPath.substring(0, mergeProjectPath.lastIndexOf("."));     
        }
        mergeProjectPath = mergeProjectPath.concat(".zip");
        mergeProjectFile = new File(mergeProjectPath);
      }

      // Confirm the user wants to overwrite an existing project, but can not 
      // overwrite one of the two projects being merged.
      if(mergeProjectFile.exists()){
        if (mergeProjectFile.getPath().equalsIgnoreCase(mainProject.getProjectPath()) ||
            mergeProjectFile.getPath().equalsIgnoreCase(secondProject.getProjectPath())) {
          JOptionPane.showMessageDialog(myCP, "You can not overwrite one of the two " +
              "projects being merged. Select anther file name.");
          getFileToSaveTo();
          return;
        }
        validPath = JOptionPane.showOptionDialog(myCP, "The file name you selected already " +
            "exists. Would you like to replace it?", "Replace", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 
            JOptionPane.NO_OPTION);
        if (validPath == JOptionPane.CLOSED_OPTION || validPath == JOptionPane.NO_OPTION){
          getFileToSaveTo();
          return;
        }
      }
      // The projects name is the name of the zip file.
      String projectName = mergeProjectPath.substring(mergeProjectPath.lastIndexOf("/") + 1, 
          mergeProjectPath.lastIndexOf(".zip"));
      // The projects name must start with a letter and can only contain letters, 
      // numbers and underscores.
      if (!(Character.isLetter(projectName.charAt(0))) ||
          !(projectName.matches("^[a-zA-Z0-9_]*$"))) {
        JOptionPane.showMessageDialog(myCP,"Project neames must start with a letter and " +
            "can contain only letters, numbers, and underscores", "Inane error",
            JOptionPane.ERROR_MESSAGE);
        getFileToSaveTo();
      }
    }    
  }

  private boolean checkDuplicates() {
    for(String screen: mainProjectScreensCBL.getChecked()) {
      if(secondProjectScreensCBL.getChecked().contains(screen)){
        JOptionPane.showMessageDialog(myCP, "You can not select two screens with the " +
            "same name. Please uncheck a one of the screens and remerge.","Inage error", 
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    for(String asset: mainProjectAssetsCBL.getChecked()) {
      if(secondProjectAssetsCBL.getChecked().contains(asset)) {
        JOptionPane.showMessageDialog(myCP, "You can not select two assets with the " +
            "same name. Please uncheck a one of the assets and remerge.","Inage error", 
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true; 
  }

  /*
   * Launches a file chooser and returns the file chosen. 
   */
  private String getFileToOpen() {
    JFileChooser projectFC = new JFileChooser();
    int validPath = projectFC.showOpenDialog(myCP);
    if(validPath == JFileChooser.ERROR_OPTION || validPath == JFileChooser.CANCEL_OPTION) {
      return null;
    } else {
      return projectFC.getSelectedFile().toString();
    }
  }

  /*
   * Updates the lower left part of the screen to display the main project.
   */
  private static void updateMainProjectView() {
    mainProjectTitleL.setText("Main Project: " + mainProject.getProjectName());

    mainProjectScreensCBL.setListData(getScreenCheckBoxes(mainProject, true)); 
    mainProjectScreensCBL.checked.add("Screen1");
    mainProjectScreensP.setViewportView(mainProjectScreensCBL);

    mainProjectAssetsCBL.setListData(getAssetCheckBoxes(mainProject)); 
    mainProjectAssetsP.setViewportView(mainProjectAssetsCBL);

    lowerLeftP.repaint();
  }

  /*
   * Updates the lower right part of the screen to display the second project.
   */
  private static void updateSecondProjectView() {
    secondProjectTitleL.setText("Second Project: " + secondProject.getProjectName());

    secondProjectScreensCBL.setListData(getScreenCheckBoxes(secondProject, false)); 
    secondProjectScreensP.setViewportView(secondProjectScreensCBL);

    secondProjectAssetsCBL.setListData(getAssetCheckBoxes(secondProject)); 
    secondProjectAssetsP.setViewportView(secondProjectAssetsCBL);

    lowerRightP.repaint();
  }

  /*
   * Creates an array of JCheckBoxes, a JCheckBox for each asset in the project.
   */
  private static JCheckBox[] getAssetCheckBoxes(AIProject project) {
    List<AIAsset> tempAssetsList = new LinkedList<AIAsset>();
    tempAssetsList=project.getAssetsList();
    JCheckBox [] assetCheckBoxLabels= new JCheckBox[tempAssetsList.size()];
    for (int i = 0; i < tempAssetsList.size(); i++) {
      assetCheckBoxLabels[i]= new JCheckBox(tempAssetsList.get(i).getName());
    }
    return assetCheckBoxLabels;
  }

  /*
   * Creates and array of JCheckBoxes, a JCheckBox for each screen in the project.
   * If the project is the main project then "Screen1" is a checked JCheckBox.
   */
  private static JCheckBox [] getScreenCheckBoxes(AIProject project, boolean isMainProject) {
    List<AIScreen> tempScreensList = new LinkedList<AIScreen>();
    tempScreensList=project.getScreensList();
    JCheckBox [] screenCheckBoxLabels= new JCheckBox[tempScreensList.size()];
    for (int i = 0; i < tempScreensList.size(); i++) {
      String tempScreenName = tempScreensList.get(i).getName();
      if (tempScreenName.equals("Screen1") && isMainProject) {
        screenCheckBoxLabels[i]=new JCheckBox(tempScreenName, true);
      } else {
        screenCheckBoxLabels[i]=new JCheckBox(tempScreenName);
      }
    }
    return screenCheckBoxLabels; 
  }

  // to hold a reference to the content pane of the JFrame
  public static Container myCP; 
  private static JPanel lowerLeftP;
  private static JPanel lowerCenterP;
  private static JPanel lowerRightP;

  private static JLabel instructMainProjectL; 
  private static JLabel instructMainProjectNotesL;
  private static JLabel instructSecondProjectL; 
  private static JLabel mainProjectTitleL;
  private static JLabel secondProjectTitleL;
  private static JLabel mainProjectAssetsL;
  private static JLabel mainProjectScreensInstrucL;
  private static JLabel mainProjectScreensL;
  private static JLabel mainProjectAssetsInstrucL;
  private static JLabel secondProjectAssetsL;
  private static JLabel secondProjectScreensInstrucL;
  private static JLabel secondProjectScreensL;
  private static JLabel secondProjectAssetsInstrucL;
  private static JButton mainProjectBrowseB;
  private static JButton mainProjectLoadB; 
  private static JButton secondProjectBrowseB;
  private static JButton secondProjectLoadB;
  private static JButton mergeB;
  private static JTextField mainProjectTF;
  private static JTextField secondProjectTF;
  private static Font HEADER_TWO_FONT = new Font("Dialog", Font.PLAIN, 18);
  private static Font HEADER_THREE_FONT = new Font("Dialog", Font.ITALIC, 12);
  private static Double HEIGHT_PERCENT_OF_SCREEN = 0.8;
  private static Double WIDTH_PERCENT_OF_SCREEN = 0.8;
  private static Dimension lowerPanelSize;
  private static Point lowerPanelLocation;
  private static JScrollPane mainProjectScreensP;
  private static JScrollPane mainProjectAssetsP;
  private static JScrollPane secondProjectScreensP;
  private static JScrollPane secondProjectAssetsP;
  private static CheckBoxList mainProjectScreensCBL;
  private static CheckBoxList mainProjectAssetsCBL;
  private static AIProject mainProject;
  private static CheckBoxList secondProjectScreensCBL;
  private static CheckBoxList secondProjectAssetsCBL;
  private static AIProject secondProject;
  private static String mergeProjectPath;
  private final Color AndroidGreen;

  public AIMerger () {	
    super("App Inventor Merger");

    AndroidGreen = new Color(166,199,58);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    setSize((int) (screenSize.width*WIDTH_PERCENT_OF_SCREEN), 
        (int) (screenSize.height*HEIGHT_PERCENT_OF_SCREEN));
    setLocation((int)((screenSize.width - screenSize.width*WIDTH_PERCENT_OF_SCREEN)/2),
        (int)((screenSize.height - screenSize.height*HEIGHT_PERCENT_OF_SCREEN)/2));

    myCP = this.getContentPane();
    myCP.setLayout(null);
    myCP.setBackground(AndroidGreen);

    //Main Project Layout
    instructMainProjectL = new JLabel("Browse for and load your Main Project.");
    instructMainProjectL.setFont(HEADER_TWO_FONT);
    instructMainProjectL.setSize(1000, 24);
    instructMainProjectL.setLocation(20, 20);
    myCP.add(instructMainProjectL);

    instructMainProjectNotesL = new JLabel("The main project's Screen1 will be the " +
        "merged project's Screen1");
    instructMainProjectNotesL.setFont(HEADER_THREE_FONT);
    instructMainProjectNotesL.setSize(1000, 20);
    instructMainProjectNotesL.setLocation(20, instructMainProjectL.getLocation().y + 20);
    myCP.add(instructMainProjectNotesL);

    mainProjectTF = new JTextField(300);
    mainProjectTF.setBackground(Color.WHITE);
    mainProjectTF.setEditable(true);
    mainProjectTF.setSize(300, 30);
    mainProjectTF.setLocation(20,instructMainProjectNotesL.getLocation().y + 20);
    myCP.add(mainProjectTF);

    mainProjectBrowseB = new JButton("Browse");
    mainProjectBrowseB.setSize(100, 30);
    mainProjectBrowseB.setLocation(mainProjectTF.getLocation().x + mainProjectTF.getSize().width + 
        10, mainProjectTF.getLocation().y);
    mainProjectBrowseB.addActionListener(new MainProjectBrowseBActionListener());
    myCP.add(mainProjectBrowseB);

    mainProjectLoadB = new JButton("Load");
    mainProjectLoadB.setSize(100, 30);
    mainProjectLoadB.setLocation(mainProjectBrowseB.getLocation().x + 
        mainProjectBrowseB.getSize().width + 10, mainProjectTF.getLocation().y);
    mainProjectLoadB.addActionListener(new MainProjectLoadBActionListener());
    myCP.add(mainProjectLoadB);

    // Second Project Layout
    instructSecondProjectL = new JLabel("Browse for and load your Second Project.");
    instructSecondProjectL.setFont(HEADER_TWO_FONT);
    instructSecondProjectL.setSize(1000, 24);
    instructSecondProjectL.setLocation(20, mainProjectTF.getLocation().y +
        mainProjectTF.getSize().height + 20);
    myCP.add(instructSecondProjectL);

    secondProjectTF = new JTextField(300);
    secondProjectTF.setBackground(Color.WHITE);
    secondProjectTF.setEditable(true);
    secondProjectTF.setSize(300, 30);
    secondProjectTF.setLocation(20,instructSecondProjectL.getLocation().y + 25);
    myCP.add(secondProjectTF);

    secondProjectBrowseB = new JButton("Browse");
    secondProjectBrowseB.setSize(100, 30);
    secondProjectBrowseB.setLocation(secondProjectTF.getLocation().x + 
        secondProjectTF.getSize().width + 10, secondProjectTF.getLocation().y);
    secondProjectBrowseB.addActionListener(new SecondProjectBrowseBActionListener());
    myCP.add(secondProjectBrowseB);

    secondProjectLoadB = new JButton("Load");
    secondProjectLoadB.setSize(100, 30);
    secondProjectLoadB.setLocation(secondProjectBrowseB.getLocation().x + 
        secondProjectBrowseB.getSize().width + 10, secondProjectTF.getLocation().y);
    secondProjectLoadB.addActionListener(new SecondProjectLoadBActionListener());
    myCP.add(secondProjectLoadB);

    // Layout for Icon
    BufferedImage myPicture = null;
    try {
      myPicture = ImageIO.read(new File("logoclear.png"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
    picLabel.setSize(400, 145);
    picLabel.setLocation(575, 20);
    myCP.add(picLabel);
    
    // Lower Panel
    lowerPanelLocation = new Point(20,secondProjectTF.getLocation().y + 50);
    lowerPanelSize = new Dimension((int)(screenSize.width*.8) - 40,
        (int)(screenSize.height*.8)-lowerPanelLocation.y - 40);

    // Lower left Panel
    lowerLeftP = new JPanel();
    lowerLeftP.setVisible(false);
    lowerLeftP.setSize(lowerPanelSize.width/3, lowerPanelSize.height);
    lowerLeftP.setLocation(20,lowerPanelLocation.y);
    lowerLeftP.setLayout(new BoxLayout(lowerLeftP,BoxLayout.Y_AXIS));
    lowerLeftP.setBorder(new EmptyBorder(5,20,20,20));

    myCP.add(lowerLeftP);

    mainProjectTitleL = new JLabel();
    mainProjectTitleL.setFont(HEADER_TWO_FONT);
    lowerLeftP.add(mainProjectTitleL);

    lowerLeftP.add(Box.createRigidArea(new Dimension(0,10)));

    mainProjectScreensL = new JLabel("Screens");
    mainProjectScreensL.setFont(HEADER_TWO_FONT);
    lowerLeftP.add(mainProjectScreensL);

    mainProjectScreensInstrucL = new JLabel("Check screens to merge into new project");
    mainProjectScreensInstrucL.setFont(HEADER_THREE_FONT);
    lowerLeftP.add(mainProjectScreensInstrucL);

    mainProjectScreensCBL = new CheckBoxList();
    mainProjectScreensP = new JScrollPane();

    lowerLeftP.add(mainProjectScreensP);

    lowerLeftP.add(Box.createRigidArea(new Dimension(0,10)));

    mainProjectAssetsL = new JLabel("Assets");
    mainProjectAssetsL.setFont(HEADER_TWO_FONT);
    lowerLeftP.add(mainProjectAssetsL);

    mainProjectAssetsInstrucL = new JLabel("Check assets to merge into new project");
    mainProjectAssetsInstrucL.setFont(HEADER_THREE_FONT);
    lowerLeftP.add(mainProjectAssetsInstrucL);

    mainProjectAssetsCBL = new CheckBoxList();
    mainProjectAssetsP = new JScrollPane();
    lowerLeftP.add(mainProjectAssetsP);

    // Lower Center Panel
    lowerCenterP = new JPanel();
    lowerCenterP.setVisible(false);
    lowerCenterP.setSize(lowerPanelSize.width/3, lowerPanelSize.height);
    lowerCenterP.setLocation(lowerPanelSize.width/3+20, lowerPanelLocation.y);
    lowerCenterP.setBackground(AndroidGreen);
    lowerCenterP.setLayout(null);
    myCP.add(lowerCenterP);

    mergeB = new JButton("Merge");
    mergeB.setSize(150, 100);
    mergeB.setLocation(lowerCenterP.getWidth()/2-mergeB.getWidth()/2,
        lowerCenterP.getHeight()/2-mergeB.getHeight()/2);
    mergeB.addActionListener(new MergeBActionListener());
    lowerCenterP.add(mergeB);

    //BufferedImage myPicture = null;
    try {
      myPicture = ImageIO.read(new File("arrows3.png"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    picLabel = new JLabel(new ImageIcon( myPicture ));
    picLabel.setSize(332, 250);
    picLabel.setLocation(lowerCenterP.getWidth()/2-picLabel.getWidth()/2,
        lowerCenterP.getHeight()/2-picLabel.getHeight()/2);
    lowerCenterP.add(picLabel);

    // Lower right Panel
    lowerRightP = new JPanel();
    lowerRightP.setVisible(false);
    lowerRightP.setSize(lowerPanelSize.width/3, lowerPanelSize.height);
    lowerRightP.setLocation(2*lowerPanelSize.width/3 +20, lowerPanelLocation.y);
    lowerRightP.setLayout(new BoxLayout(lowerRightP,BoxLayout.Y_AXIS));
    lowerRightP.setBorder(new EmptyBorder(5,20,20,20));
    myCP.add(lowerRightP);

    secondProjectTitleL = new JLabel();
    secondProjectTitleL.setFont(HEADER_TWO_FONT);
    lowerRightP.add(secondProjectTitleL);

    lowerRightP.add(Box.createRigidArea(new Dimension(0,10)));

    secondProjectScreensL = new JLabel("Screens");
    secondProjectScreensL.setFont(HEADER_TWO_FONT);
    lowerRightP.add(secondProjectScreensL);

    secondProjectScreensInstrucL = new JLabel("Check Screens to Merge into New Project");
    secondProjectScreensInstrucL.setFont(HEADER_THREE_FONT);
    lowerRightP.add(secondProjectScreensInstrucL);

    secondProjectScreensCBL = new CheckBoxList();
    secondProjectScreensP = new JScrollPane();

    lowerRightP.add(secondProjectScreensP);

    lowerRightP.add(Box.createRigidArea(new Dimension(0,10)));

    secondProjectAssetsL = new JLabel("Assets");
    secondProjectAssetsL.setFont(HEADER_TWO_FONT);
    lowerRightP.add(secondProjectAssetsL);

    secondProjectAssetsInstrucL = new JLabel("Check Assets to Merge into New Project");
    secondProjectAssetsInstrucL.setFont(HEADER_THREE_FONT);
    lowerRightP.add(secondProjectAssetsInstrucL);

    secondProjectAssetsCBL = new CheckBoxList();
    secondProjectAssetsP = new JScrollPane();
    lowerRightP.add(secondProjectAssetsP);

    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    AIMerger myApp = new AIMerger();
  }

}

