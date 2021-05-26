package org.petctviewer.scintigraphy.hepatic.tab;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import org.petctviewer.scintigraphy.hepatic.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TabMainHepaticDyn extends TabResult {

	final ImagePlus montage;

	final ModelHepaticDynamic model;

	public TabMainHepaticDyn(FenResults parent, ModelHepaticDynamic model) {
		super(parent, "Main", true);
		this.model = model;

		ImagePlus currentImage = this.getParent().getController().getVue().getImagePlus();
		ImageSelection imageAnt = this.parent.getController().getModel().getImageSelection()[0].clone();
		ImageSelection impProjeteeAnt = Library_Dicom.project(imageAnt, 0, imageAnt.getImagePlus().getStackSize(),
				"avg");
		this.getParent().getController().getVue().setImage(impProjeteeAnt.getImagePlus());
		this.model.setCapture(this.getParent().getController().getVue().getImagePlus(), 3);
		this.getParent().getController().getVue().setImage(currentImage);

		ImagePlus[] captures = model.getCaptures();

		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures);
		this.montage = this.montage(stackCapture);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		String[] result = model.getResult();
		JPanel res = new JPanel(new GridLayout(result.length, 1));
		for (String s : result)
			res.add(new JLabel(s));
		
		res.setMaximumSize(new Dimension((int) (this.parent.getWidth() * 0.25), res.getHeight()));
		return res;
	}

	@Override
	public JPanel getResultContent() {
		//		dynamic.setPreferredSize(new Dimension((int) (this.parent.getWidth() * 0.75), dynamic.getHeight()));
		return new DynamicImage(montage.getImage());
	}

	protected ImagePlus montage(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		ImagePlus impInfo = this.getParent().getModel().getImageSelection()[0].getImagePlus();
		HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(impInfo);
		String patientID = infoPatient.get(Library_Capture_CSV.PATIENT_INFO_ID);
		if (patientID.isEmpty()) patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Results Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		return imp;
	}

}
