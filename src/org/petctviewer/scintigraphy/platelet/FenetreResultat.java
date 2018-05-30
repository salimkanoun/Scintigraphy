package org.petctviewer.scintigraphy.platelet;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.renal.gui.BackgroundPanel;

public class FenetreResultat extends JFrame {
  private static final long serialVersionUID = -9097595151860174657L;
  private Label lbl_csv;
  private JButton btn_capture = new JButton();
  
  public FenetreResultat(ImagePlus imp, JTable tableResults) {
    
 
    setLayout(new BorderLayout());
    JPanel panelImage=new JPanel();
    this.add(panelImage, BorderLayout.WEST);
    panelImage.add(new BackgroundPanel(imp.getBufferedImage()));
    
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
    this.btn_capture.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.log("Capture");
			
		}
    	
    });
    buttonPanel.add(this.btn_capture);
    
    add(scrollPane, BorderLayout.EAST);
    add(buttonPanel, BorderLayout.SOUTH);
    
    pack();

  }
  
  
}
