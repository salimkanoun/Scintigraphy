package org.petctviewer.scintigraphy.statics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneLayout;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public class FenResultat_ScinStatic extends JFrame {

	private SidePanel side;
	private Scintigraphy scin;
	
	public FenResultat_ScinStatic( Scintigraphy scin, BufferedImage capture, ModeleScinStatic modele) {

		this.scin = scin;
		side = new SidePanel (null, "Static Quant\n", scin.getImp());
	
		this.add(new DynamicImage(capture),	 BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.pack();
		this.setVisible(true);
	}


	
	public void addAntTab(Object[][] data){
		JTable table ;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		String[] title = {"Name","Count","Avg","Std"};
		 table = new JTable(data,title);
		
		p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        
        JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);
        this.side.add(new JLabel("Ant"));
		this.side.add(fixedSize);	
		

	}
	
	public void addPostTab(Object[][] data){
		JTable table ;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		String[] title = {"Name","Count","Avg","Std"};
		 table = new JTable(data,title);
		
		p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        
        JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);
        
        this.side.add(new JLabel("Post"));
		this.side.add(fixedSize);		

	}
	//csv
	//2 ant et post sur la cpture
	
	
	public void addMoyGeomTab(Object[][] data) {
		JTable table;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		String[] title = {"Name","Geom Mean"};
		table = new JTable(data,title);
		
		p.add(table.getTableHeader(), BorderLayout.NORTH);
		p.add(table,BorderLayout.CENTER);
		
		JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);
        
		this.side.add(new JLabel("Geom Mean"));
		this.side.add(fixedSize);
		
	}
	
	public void addCaptureButton() {
		side.addCaptureBtn(scin, "cc");
	}
	

}
