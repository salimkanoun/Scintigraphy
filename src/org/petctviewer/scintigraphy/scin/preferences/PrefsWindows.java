package org.petctviewer.scintigraphy.scin.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import ij.Prefs;
import ij.plugin.PlugIn;

public class PrefsWindows extends WindowAdapter implements PlugIn {

	private Container main, renal, bone, gastric;
	
	private JLabel status;

	@Override
	public void run(String arg) {
		this.main = new prefsTabMain();
		this.renal = new prefsTabRenal();
		this.bone = new prefsTabBone();
		this.gastric = new PrefsTabGastric(this);
		showGUI();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		Prefs.savePreferences();
	}

	private void showGUI() {

		// Create and set up the window.
		JFrame frame = new JFrame("Results Renal Exam");
		frame.addWindowListener(this);
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		
		JPanel panel = new JPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.main);
		tabbedPane.addTab("Renal", this.renal);
		tabbedPane.addTab("Bone", this.bone);
		tabbedPane.addTab("Gastric", this.gastric);
		
		// Status bar
		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4,4,4,4)));
		this.status = new JLabel();
		statusBar.add(this.status);
		panel.add(statusBar, BorderLayout.SOUTH);
		
		panel.add(tabbedPane, BorderLayout.CENTER);
		frame.getContentPane().add(panel);

		// Display the window
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void setStatus(String message) {
		status.setText(message);
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println("ok");
//				status.setText(message);
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				status.setText(null);
//			}
//		});
	}

}
