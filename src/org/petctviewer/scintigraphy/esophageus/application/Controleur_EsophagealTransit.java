package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class Controleur_EsophagealTransit extends Controleur_OrganeFixe {

	public static String[] ORGANES = { "Esophage" };

	public Controleur_EsophagealTransit(EsophagealTransit esoPlugin, ImagePlus[][] sauvegardeImagesSelectDicom) {
		super(esoPlugin, new Modele_EsophagealTransit(sauvegardeImagesSelectDicom, esoPlugin));
		this.setOrganes(ORGANES);
		this.tools = Toolbar.RECTANGLE;
	}

	@Override
	public boolean isOver() {
		return (this.indexRoi + 1) >= this.model.getImagesPlus()[0].getStackSize();
	}

	@Override
	public void end() {
		model.calculerResultats();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(
				((Modele_EsophagealTransit) model).getExamenMean(), ((Modele_EsophagealTransit) model).getDicomRoi(),
				((Modele_EsophagealTransit)model));
		fen.pack();
		fen.setLocationRelativeTo(null);
		fen.setVisible(true);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return (roiIndex + 1);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {

		if (indexRoi > 0 && lastRoi < this.indexRoi) {
			return this.model.getRoiManager().getRoi(lastRoi);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		return false;
	}

}
