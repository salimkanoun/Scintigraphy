package org.petctviewer.scintigraphy.shunpo;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.IJ;
import ij.Prefs;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class ControleurShunpo extends ControleurScin {

	private Modele_Shunpo modele;

	protected ControleurShunpo(Scintigraphy scin) {
		super(scin);
		String[] organes = { "Right lung", "Left lung", "Right kidney", "Left kidney", "Background" };
		this.setOrganes(organes);

		this.modele = new Modele_Shunpo();

		Library_Gui.setOverlayDG(scin.getImp().getOverlay(), scin.getImp());
		IJ.setTool(Toolbar.POLYGON);
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= this.getOrganes().length -1;
	}

	@Override
	public void fin() {
		JOptionPane.showMessageDialog(null, "Fin !", "", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public boolean isPost() {
		return true;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 0;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		// Sens aller
		if (lastRoi < indexRoi) {
			String org = this.getNomOrgane(lastRoi);
			System.out.println("organRoiName" + org);
			// roi de bruit de fond
			boolean pelvis = Prefs.get("renal.pelvis.preferred", true);
			if (!pelvis && org.contains("Kidney")) {
				Roi roi = roiManager.getRoi(indexRoi - 1);
				return Library_Roi.createBkgRoi(roi, getScin().getImp(), Library_Roi.KIDNEY);
			} else if (pelvis && org.contains("Pelvis")) {
				Roi roi = roiManager.getRoi(indexRoi - 2);
				return Library_Roi.createBkgRoi(roi, getScin().getImp(), Library_Roi.KIDNEY);
			}
		}

		// Sens Retour
		if (lastRoi == indexRoi) {
			return roiManager.getRoi(indexRoi);
		}

		// Should not happen
		return null;
	}

}
