package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.awt.Frame;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.ModeleScin;

import ij.gui.Roi;

public class FenApplicationSecondHepaticDyn extends FenApplicationWorkflow {
	private static final long serialVersionUID = -910237891674972798L;

	public FenApplicationSecondHepaticDyn(ImageSelection ims, String nom) {
		super(ims, nom);
		
		

	}

	public static void importRoiList(Frame frame, ModeleScin modele, ControleurScin controller) {

		List<Roi> rois = Library_Roi.getRoiFromZipWithWindow(frame);

		int result = JOptionPane.YES_OPTION;
		if (modele.getRoiManager().getCount() > 0) {
			String message = "Do you want to delete the ROIs already registred ??";
			result = JOptionPane.showConfirmDialog(frame, message, "ROIs already registred", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				modele.getRoiManager().removeAll();
				for (Roi roi : rois)
					modele.getRoiManager().addRoi(roi);
			} else if (result == JOptionPane.NO_OPTION)
				for (int index = modele.getRoiManager().getCount(); index < rois.size(); index++)
					modele.getRoiManager().addRoi(rois.get(index));

		} else
			for (Roi roi : rois)
				modele.getRoiManager().addRoi(roi);
		// }
	}
}