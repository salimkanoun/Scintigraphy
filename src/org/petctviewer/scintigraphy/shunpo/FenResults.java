package org.petctviewer.scintigraphy.shunpo;

import java.awt.BorderLayout;

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

	
		this.setLayout(new BorderLayout());

		// Add components
		this.add(this.tabPane, BorderLayout.CENTER);
		this.pack();
	}
	
	public ModeleScin getModel() {
		return this.model;
	}
	
	public void addTab(TabResult tab) {
		this.tabPane.add(tab.getTitle(), tab);
		this.pack();
	}

}
