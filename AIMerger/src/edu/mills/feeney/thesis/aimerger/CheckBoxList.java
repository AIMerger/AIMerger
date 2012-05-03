package edu.mills.feeney.thesis.aimerger;
//Trevor Harmon www.devx.com/tips/Tip/5342
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
public class CheckBoxList extends JList
{
  /**
   * 
   */
  static //private static final long serialVersionUID = 1L;
  LinkedList <String> text = new LinkedList<String>();
  LinkedList <String> checked = new LinkedList<String>();
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
  public CheckBoxList()
  {
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          if (!checkbox.getText().equals("Screen1")){
            checkbox.setSelected(!checkbox.isSelected());
            if(text.contains(checkbox.getText()) && checkbox.isEnabled() && 
                !checked.contains(checkbox.getText())){
              checked.add(checkbox.getText());
            }else if(checked.contains(checkbox.getText())){
              checked.remove(checkbox.getText());
            }
          }
          repaint();
        }
      }
    }
        );
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  public LinkedList<String> getChecked(){
    return checked;
  }

  protected class CellRenderer implements ListCellRenderer
  {
    public Component getListCellRendererComponent(JList list, Object value, int index, 
                                                  boolean isSelected, boolean cellHasFocus)
    {
      JCheckBox checkbox = (JCheckBox) value;
      text.add(checkbox.getText());
      checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ?
          UIManager.getBorder(
              "List.focusCellHighlightBorder") : noFocusBorder);
      if(checkbox.getText().equals("Screen1")){
        checkbox.setForeground(Color.gray);
      }
      return checkbox;
    }
  }
}