package org.petctviewer.scintigraphy.gastric.dynamic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class FenApplication_DynGastric extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public FenApplication_DynGastric(ImageSelection ims, String nom) {
		super(ims, nom);

		Library_Gui.initOverlay(getImagePlus());
		Library_Gui.setOverlayDG(getImagePlus(), Color.YELLOW);
	}

}
