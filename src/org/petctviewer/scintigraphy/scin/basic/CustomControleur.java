package org.petctviewer.scintigraphy.scin.basic;

import java.awt.event.ActionEvent;

import ij.gui.Roi;

public interface CustomControleur {

	public void fin();
	
	public Roi getOrganRoi(Roi roi);
	
	public void notifyClic(ActionEvent arg0);
	
}
