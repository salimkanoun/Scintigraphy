package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.awt.Frame;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.exceptions.UnauthorizedRoiLoadException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.ModeleScin;

import ij.gui.Roi;

public class FenApplicationSecondHepaticDyn extends FenApplicationWorkflow {
	private static final long serialVersionUID = -910237891674972798L;

	public FenApplicationSecondHepaticDyn(ImageSelection ims, String nom) {
		super(ims, nom);

	}

	public static void importRoiList(Frame frame, ModeleScin modele, ControllerWorkflow controller) throws UnauthorizedRoiLoadException {

		List<Roi> rois = Library_Roi.getRoiFromZipWithWindow(frame, controller);
		modele.getRoiManager().removeAll();
		
		for (Roi roi : rois)
			modele.getRoiManager().addRoi(roi);

	}
}