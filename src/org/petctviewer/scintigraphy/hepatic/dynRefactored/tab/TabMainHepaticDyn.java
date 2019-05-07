package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.lympho.ModeleLympho;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class TabMainHepaticDyn extends TabResult{
	
	private ImagePlus[] captures;
	
	ImagePlus montage;
	
	ModelHepaticDynamic model;

	public TabMainHepaticDyn(FenResults parent, ModelHepaticDynamic model) {
		super(parent, "Main", true);
		this.model = model;
		ImageSelection image = this.parent.getController().getModel().getImageSelection()[0].clone();
		ImageSelection capture = image;
		capture.getImagePlus().show();
		capture.getImagePlus().getCanvas().setSize(this.parent.getController().getVue().getCanvas().getWidth(), this.parent.getController().getVue().getCanvas().getHeight());
		capture.getImagePlus().getCanvas().setPreferredSize(new Dimension(this.parent.getController().getVue().getCanvas().getWidth(), this.parent.getController().getVue().getCanvas().getHeight()));
		capture.getImagePlus().getProcessor().resize(this.parent.getController().getVue().getCanvas().getWidth());
		model.setCapture(capture.getImagePlus(), 3);
//		capture.getImagePlus().close();
		this.captures = model.getCaptures();
		System.out.println(capture.getImagePlus().getCanvas().getWidth());
		System.out.println(capture.getImagePlus().getCanvas().getHeight());
		
		
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures);
		this.montage = this.montage(stackCapture);
		
		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		System.out.println(model != null);
		String[] result = model.getResult();
		JPanel res = new JPanel(new GridLayout(result.length, 1));
		for (String s : result)
			res.add(new JLabel(s));
		return res;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}
	
	protected ImagePlus montage(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		System.out.println(captures != null);
		System.out.println(this.model != null );
		System.out.println(this.model.getStudyName());
		ImagePlus imp = new ImagePlus("Results Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		return imp;
	}

}
