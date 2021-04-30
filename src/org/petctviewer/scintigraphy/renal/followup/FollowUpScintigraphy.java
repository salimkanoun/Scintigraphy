package org.petctviewer.scintigraphy.renal.followup;

import ij.plugin.PlugIn;

public class FollowUpScintigraphy implements PlugIn{

	@Override
	public void run(String arg0) {
		
		FenCSVChoice f = new FenCSVChoice();
		f.setVisible(true);
	}
}
