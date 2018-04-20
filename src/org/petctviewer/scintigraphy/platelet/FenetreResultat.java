package org.petctviewer.scintigraphy.platelet;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class FenetreResultat extends ImageWindow {
  private static final long serialVersionUID = -9097595151860174657L;
  private Label lbl_csv;
  private Button btn_capture;
  
  public FenetreResultat(ImagePlus imp, JTable tableResults) {
    super(imp, new ImageCanvas(imp));
    
    setLayout(new FlowLayout());
    
    setLayout(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(tableResults);
    tableResults.setAutoResizeMode(4);
    tableResults.setAutoCreateRowSorter(true);
    tableResults.setFillsViewportHeight(true);
    tableResults.getRowSorter().toggleSortOrder(0);
    
    Panel buttonPanel = new Panel();
    buttonPanel.setLayout(new FlowLayout());
    
    
    this.lbl_csv = new Label();
    String path = Prefs.get("dir.preferred", null);
    if (path == null)
    {
      this.lbl_csv.setText("No CSV output");
      this.lbl_csv.setForeground(Color.RED);
    }
    else
    {
      this.lbl_csv.setText("CSV Save OK");
    }
    buttonPanel.add(this.lbl_csv);
    buttonPanel.add(this.btn_capture);
    
    add(scrollPane, "Center");
    add(buttonPanel, "South");
    
    pack();
    
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Point loc = getLocation();
    Dimension size = getSize();
    if (loc.y + size.height > screen.height) {
      getCanvas().zoomOut(0, 0);
    }
  }
}
