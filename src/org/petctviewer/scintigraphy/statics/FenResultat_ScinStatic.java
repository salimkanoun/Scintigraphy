package org.petctviewer.scintigraphy.statics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneLayout;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

public class FenResultat_ScinStatic extends FenResultatSidePanel {

	
	private JPanel j;
	
	public FenResultat_ScinStatic( Scintigraphy scin, BufferedImage capture, String additionalInfo, ModeleScinStatic modele) {
		super("Static Quant \n", scin, capture, additionalInfo);

		this.finishBuildingWindow(true);
		this.setVisible(true);
	}

	@Override
	public Component getSidePanelContent() {
		
	
		this.j = new JPanel();
		
		
		return this.j;
	}
	
	public void addDonneeTab(Object[][] data, int nbSlice) {
		
		JTable table ;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		if(nbSlice == 2) {
			String[] title = {"Name","Count","Avg (count/mm2)","Std"};
			 table = new JTable(data,title);


		}else {
			String[] title = {"Name","Count","Avg (count/mm2)"};
			 table = new JTable(data,title);

		}

		
		p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        
		this.j.add(p);		

	}
	
	
	

}
