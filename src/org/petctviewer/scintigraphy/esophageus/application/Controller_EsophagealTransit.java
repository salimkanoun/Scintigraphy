package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.Controller_OrganeFixe;

import ij.gui.Roi;
import ij.gui.Toolbar;

public class Controller_EsophagealTransit extends Controller_OrganeFixe {

	public static String[] ORGANES = { "Esophage" };

	public Controller_EsophagealTransit(EsophagealTransit esoPlugin, ImageSelection[][] sauvegardeImagesSelectDicom,
	                                    String studyName) {
		super(esoPlugin, new Model_EsophagealTransit(sauvegardeImagesSelectDicom, studyName, esoPlugin));
		this.setOrganes(ORGANES);
		this.tools = Toolbar.RECTANGLE;
	}

	@Override
	public boolean isOver() {
		return (this.indexRoi + 1) >= this.model.getImageSelection()[0].getImagePlus().getStackSize();
	}

	@Override
	public void end() {
		model.calculateResults();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(
				((Model_EsophagealTransit) model).getExamenMean(), ((Model_EsophagealTransit) model).getDicomRoi(),
				((Model_EsophagealTransit) model), "Esophageal Transit", this);
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
