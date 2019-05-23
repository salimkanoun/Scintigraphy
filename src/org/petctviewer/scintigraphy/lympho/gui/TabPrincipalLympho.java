package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.ModelLympho;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class TabPrincipalLympho extends TabResult {

	ModelScin model;

	ImagePlus montage;

	public TabPrincipalLympho(FenResults parent, String title, ModelScin model, ImagePlus[] captures) {
		super(parent, title);

		System.out.println("TabPrincipalLympho : " + model != null);
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
		JPanel res = new JPanel(new GridLayout(result.length, 1));
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
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Results Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		return imp;
	}

}
