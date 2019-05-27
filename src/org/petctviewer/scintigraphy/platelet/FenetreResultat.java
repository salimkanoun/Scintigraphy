package org.petctviewer.scintigraphy.platelet;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import javax.swing.*;
import java.awt.*;

public class FenetreResultat extends JFrame {
  private static final long serialVersionUID = -9097595151860174657L;

	public FenetreResultat(ImagePlus imp, JTable tableResults) {
    
 
    setLayout(new BorderLayout());
    JPanel panelImage=new JPanel();
    this.add(panelImage, BorderLayout.WEST);
    panelImage.add(new DynamicImage(imp.getBufferedImage()));
    
    JScrollPane scrollPane = new JScrollPane(tableResults);
    tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    tableResults.setAutoCreateRowSorter(true);
    tableResults.setFillsViewportHeight(true);
    tableResults.getRowSorter().toggleSortOrder(0);
    
    Panel buttonPanel = new Panel();
    buttonPanel.setLayout(new FlowLayout());


		Label lbl_csv = new Label();
    String path = Prefs.get("dir.preferred", null);
    if (path == null)
    {
      lbl_csv.setText("No CSV output");
      lbl_csv.setForeground(Color.RED);
    }
    else
    {
      lbl_csv.setText("CSV Save OK");
    }
    buttonPanel.add(lbl_csv);
		JButton btn_capture = new JButton();
		btn_capture.addActionListener(arg0 -> IJ.log("Capture"));
    buttonPanel.add(btn_capture);
    
    add(scrollPane, BorderLayout.EAST);
    add(buttonPanel, BorderLayout.SOUTH);
    
    pack();

  }
  
  
}
