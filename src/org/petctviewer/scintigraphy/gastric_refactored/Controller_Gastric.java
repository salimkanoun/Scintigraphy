package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

public class Controller_Gastric extends ControleurScin {

	public Controller_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages, String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));
	}

	@Override
	public boolean isOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void end() {
		// TODO Auto-generated method stub

	}

}
