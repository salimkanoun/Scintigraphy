package org.petctviewer.scintigraphy.renal.followup;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.plugin.PlugIn;

public class FollowUp implements PlugIn{

	@Override
	public void run(String arg0) {
		
		FenCSVChoice f = new FenCSVChoice();
		f.setVisible(true);
	}
}
