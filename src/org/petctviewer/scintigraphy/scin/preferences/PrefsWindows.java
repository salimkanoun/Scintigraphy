package org.petctviewer.scintigraphy.scin.preferences;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import ij.plugin.PlugIn;

public class PrefsWindows implements PlugIn{

	private Container main, renal, bone;
	
	@Override
	public void run(String arg) {
		this.main = new prefsTabMain();
		this.renal = new prefsTabRenal();
		this.bone = new prefsTabBone();
		

		showGUI();
		
	}
	
	private void showGUI() {

		// Create and set up the window.
		JFrame frame = new JFrame("Results Renal Exam");
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.main);
		tabbedPane.addTab("Renal", this.renal);
		tabbedPane.addTab("Bone", this.bone);

		
		
		frame.getContentPane().add(tabbedPane);

		// Display the window

		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}


}
