package org.petctviewer.scintigraphy.mibg.tabResults;

import java.util.List;

import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.PanelImpContrastSlider;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class TabContrastMIBG extends PanelImpContrastSlider {

	public TabContrastMIBG(String nomFen, String additionalInfo, FenResults parent, List<ImagePlus> captures) {
		super(nomFen, additionalInfo, parent);

		@SuppressWarnings("deprecation")
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(parent.getModel().getImagesPlus());

		MontageMaker mm = new MontageMaker();
		ImagePlus montage = new ImagePlus("Results MIBG", stackCapture);
		montage = mm.makeMontage2(montage, 1, 2, 1, 1, 2, 1, 10, false);

		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);

		this.setImp(montage);

		this.reloadDisplay();
	}

}
