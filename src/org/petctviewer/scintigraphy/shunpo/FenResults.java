package org.petctviewer.scintigraphy.shunpo;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.petctviewer.scintigraphy.scin.ModeleScin;

public class FenResults extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabPane;
	
	private ModeleScin model;

	public FenResults(ModeleScin model) {
		this.setLocationRelativeTo(null);
		this.setTitle("Result for " + model.getStudyName());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.model = model;

		this.tabPane = new JTabbedPane();
	
		this.add(tabPane);
		
		this.setVisible(true);
	}
	
	public ModeleScin getModel() {
		return this.model;
	}
	
	public void addTab(TabResult tab) {
		this.tabPane.addTab(tab.getTitle(), tab.getPanel());
		this.pack();
	}

}
