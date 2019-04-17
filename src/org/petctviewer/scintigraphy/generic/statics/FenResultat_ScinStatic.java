package org.petctviewer.scintigraphy.generic.statics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.TabResult;

public class FenResultat_ScinStatic extends FenResults {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel panAnt, panPost, panAvgGeo;
	private BufferedImage capture;
	
	private TabResult tab;
	
	public FenResultat_ScinStatic(BufferedImage capture, ModeleScin model) {
		super(model);
		this.capture = capture;
		
		this.tab = new Tab(this, "Result");
		
		this.addAntTab(((ModeleScinStatic)model).calculerTableauAnt());
		this.addPostTab(((ModeleScinStatic)model).calculerTableauPost());
		this.addMoyGeomTab(((ModeleScinStatic)model).calculerTaleauMayGeom());
	
		this.setLocationRelativeTo(model.getImagePlus().getWindow());
	}
	
	private class Tab extends TabResult {

		public Tab(FenResults parent, String title) {
			super(parent, title);
			this.createCaptureButton("cc");
		}

		@Override
		public Component getSidePanelContent() {
			Box box = new Box(BoxLayout.PAGE_AXIS);
			if(panAnt != null)
				box.add(panAnt);
			if(panPost != null)
				box.add(panPost);
			if(panAvgGeo != null)
				box.add(panAvgGeo);
			return box;
		}

		@Override
		public JPanel getResultContent() {
			return new DynamicImage(capture);
		}
		
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
        
        this.panAnt.add(new JLabel("Ant"));
		this.panAnt.add(fixedSize);
		
		this.tab.reloadDisplay();
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
        
        this.panPost.add(new JLabel("Post"));
		this.panPost.add(fixedSize);		

		this.tab.reloadDisplay();
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
        
		this.panAvgGeo.add(new JLabel("Geom Mean"));
		this.panAvgGeo.add(fixedSize);
		
		this.tab.reloadDisplay();
	}
	

}
