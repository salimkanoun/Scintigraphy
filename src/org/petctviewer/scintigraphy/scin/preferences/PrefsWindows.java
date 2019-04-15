package org.petctviewer.scintigraphy.scin.preferences;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import ij.plugin.PlugIn;

public class PrefsWindows implements PlugIn{

	private Container main, renal, bone;
	
	private static JFrame frame = null;												// Trying to implement Singleton pattern
	
	private static boolean INSTANCE = false;										// Trying to implement Singleton pattern
	
	@Override
	public void run(String arg) {
		if (!INSTANCE) {															// Trying to implement Singleton pattern
			this.main = new prefsTabMain();
			this.renal = new prefsTabRenal();
			this.bone = new prefsTabBone();
			
	
			showGUI();
		}else {																		// Trying to implement Singleton pattern
			PrefsWindows.frame.toFront();
		}
		
	}
	
	private void showGUI() {

		// Create and set up the window.
		PrefsWindows.frame = new JFrame("Preferences panel");
		PrefsWindows.frame.getContentPane().setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.main);
		tabbedPane.addTab("Renal", this.renal);
		tabbedPane.addTab("Bone", this.bone);

		
		
		PrefsWindows.frame.getContentPane().add(tabbedPane);

		// Display the window

		PrefsWindows.frame.pack();
		PrefsWindows.frame.setVisible(true);
		PrefsWindows.frame.setResizable(true);
		PrefsWindows.frame.setLocationRelativeTo(null);
		PrefsWindows.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		INSTANCE = true;
	}


}
