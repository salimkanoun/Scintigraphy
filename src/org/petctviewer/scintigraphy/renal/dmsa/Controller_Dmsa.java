package org.petctviewer.scintigraphy.renal.dmsa;

import ij.gui.Overlay;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.Controller_OrganeFixe;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class Controller_Dmsa extends Controller_OrganeFixe {

	public static final String[] ORGANES = {"L. Kidney", "L. bkg", "R. Kidney", "R. bkg" };
	private int maxIndexRoi = 0;

	private final boolean antPost;
	private boolean over;

	protected Controller_Dmsa(Scintigraphy scin, ImageSelection[] selectedImages, String studyName) {
		super(scin, new Model_Dmsa(selectedImages, studyName));

		this.antPost = this.model.getImagePlus().getNSlices() == 2;
		if (antPost) {
			this.setSlice(2);
			String[] organes = new String[ORGANES.length];
			for (int i = 0; i < organes.length; i++) {
				organes[i] = ORGANES[i % ORGANES.length];
			}
			this.setOrganes(organes);
		}

	}

	@Override
	public boolean isOver() {
		return indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void end() {
		this.over = true;
		// Clear the result hashmap in case of a second validation
		((Model_Dmsa) this.model).data.clear();
		this.nomRois.clear();

		indexRoi = 0;
		BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 400, 400)
				.getBufferedImage();

		for (Roi roi : this.model.getRoiManager().getRoisAsArray()) {
			this.indexRoi++;
			this.model.getImagePlus().setRoi(roi);
			String nom = getNomOrgane(indexRoi);
			this.setSlice(1);
			((Model_Dmsa) this.model).enregistrerMesure(this.addTag(nom), this.model.getImagePlus());
			if (this.antPost) {
				this.setSlice(2);
				((Model_Dmsa) this.model).enregistrerMesure(this.addTag(nom), this.model.getImagePlus());

			}

		}

		this.model.calculateResults();

		new FenResultats_Dmsa(capture, this);

		this.over = false;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public void setSlice(int indexSlice) {
		super.setSlice(indexSlice);
		this.hideAndColorLabel("R. bkg", Color.GRAY);
		this.hideAndColorLabel("L. bkg", Color.GRAY);
	}

	private void hideAndColorLabel(String name, Color c) {
		Overlay ov = this.model.getImagePlus().getOverlay();
		Roi roi = ov.get(ov.getIndex(name));
		if (roi != null) {
			roi.setName("");
			roi.setStrokeColor(c);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.indexRoi == 1 | this.indexRoi == 3) {
			return Library_Roi.createBkgRoi(this.model.getRoiManager().getRoi(indexRoi - 1), this.model.getImagePlus(),
					Library_Roi.KIDNEY);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		if (this.over && antPost) {
			return this.model.getImagePlus().getCurrentSlice() != 2;
		}
		return true;
	}

}
