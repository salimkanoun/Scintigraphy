package org.petctviewer.scintigraphy.lympho.gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import org.petctviewer.scintigraphy.lympho.ModelLympho;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TabPrincipalLympho extends TabResult {

	final ModelScin model;

	final ImagePlus montage;

	private JLabel visualGradation;

	public TabPrincipalLympho(FenResults parent, String title, ModelScin model, ImagePlus[] captures) {
		super(parent, title);

		this.model = model;
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures);
		this.montage = this.montage(stackCapture);

		this.createCaptureButton();

		this.reloadDisplay();

	}

	@Override
	public Component getSidePanelContent() {
		System.out.println(model != null);
		String[] result = ((ModelLympho) parent.getModel()).getResult();
		
		JPanel res = new JPanel(new GridLayout(result.length + 2, 1));
		
		this.visualGradation = new JLabel();
		res.add(this.visualGradation);
		
		res.add(new JLabel());
		
		
		for (String s : result)
			res.add(new JLabel(s));
		
		return res;
	}

	@Override
	public JPanel getResultContent() {
		if (montage != null)
			return new DynamicImage(montage.getImage());
		else
			return null;
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

	public void updateVisualGradation(String gradation) {
		this.visualGradation.setText(gradation);
	}


}
