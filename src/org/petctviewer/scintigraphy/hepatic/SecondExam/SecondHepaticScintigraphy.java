package org.petctviewer.scintigraphy.hepatic.SecondExam;

import ij.IJ;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.hepatic.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class SecondHepaticScintigraphy extends Scintigraphy {

	private ImageSelection impAnt, impPost, impProjeteeAnt, impProjeteePost;
	private final ModelHepaticDynamic model;
	private final TabResult tab;
	private int[] frameDurations;

	public SecondHepaticScintigraphy(TabResult tab, ModelHepaticDynamic model) {
		super("Second Method");
		this.model = model;
		this.tab = tab;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException, ReadTagException {
		if (openedImages.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		if (openedImages[0].getImageOrientation() != Orientation.DYNAMIC_ANT
				&& openedImages[0].getImageOrientation() != Orientation.DYNAMIC_ANT_POST)
			throw new WrongColumnException.OrientationColumn(openedImages[0].getRow(),
					openedImages[0].getImageOrientation(),
					new Orientation[] { Orientation.DYNAMIC_ANT, Orientation.DYNAMIC_ANT_POST });

		ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(openedImages[0]);
		if (imps[0] != null) {
			this.impAnt = imps[0].clone();
		}

		if (imps[1] != null) {
			this.impPost = imps[1].clone();
			for (int i = 1; i <= this.impPost.getImagePlus().getStackSize(); i++) {
				this.impPost.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
		}

		openedImages[0].getImagePlus().close();

		this.frameDurations = Library_Dicom.buildFrameDurations(impAnt.getImagePlus());

		// Library_Dicom.normalizeToCountPerSecond(impAnt.getImagePlus(),
		// this.frameDurations);
		IJ.run(this.impAnt.getImagePlus(), "32-bit", "");
		// Library_Dicom.normalizeToCountPerSecond(impPost.getImagePlus(),
		// this.frameDurations);
		IJ.run(this.impPost.getImagePlus(), "32-bit", "");

		if (this.impAnt != null) {
			impProjeteeAnt = this.impAnt.clone();
			Library_Dicom.normalizeToCountPerSecond(impProjeteeAnt);
			impProjeteeAnt = Library_Dicom.project(this.impProjeteeAnt, 0, impProjeteeAnt.getImagePlus().getStackSize(),
					"avg");

		}
		if (this.impPost != null) {
			impProjeteePost = Library_Dicom.project(this.impPost, 0, impPost.getImagePlus().getStackSize(), "avg");
		}

		ImageSelection[] selection = new ImageSelection[4];
		selection[0] = impProjeteeAnt;
		selection[1] = impAnt;
		selection[2] = impPost;
		selection[3] = impProjeteePost;

		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		this.model.setImpSecondMethod(selectedImages);

		this.setFenApplication(new FenApplicationSecondHepaticDyn(impProjeteeAnt, this.getStudyName()));
		impProjeteeAnt.getImagePlus().setOverlay(overlay);

		this.getFenApplication()
				.setController(new ControllerWorkflowHepaticDyn((FenApplicationWorkflow) this.getFenApplication(),
						new ModelSecondMethodHepaticDynamic(selectedImages, this.getStudyName(), this.frameDurations),
						this.tab));

	}

}
