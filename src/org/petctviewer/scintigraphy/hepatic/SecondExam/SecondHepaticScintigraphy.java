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
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SecondHepaticScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Second Method";
	private ImageSelection impAnt, impPost, impProjeteeAnt, impProjeteePost;
	private final ModelHepaticDynamic model;
	private final TabResult tab;
	private int[] frameDurations;

	public SecondHepaticScintigraphy(TabResult tab, ModelHepaticDynamic model) {
		super(STUDY_NAME);
		this.model = model;
		this.tab = tab;
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

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException,
			ReadTagException {
		if (openedImages.size() > 2) {
			// TODO: use exceptions!!!!!!!!!!!!!!
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		if (openedImages.get(0).getImageOrientation() != Orientation.DYNAMIC_ANT
				&& openedImages.get(0).getImageOrientation() != Orientation.DYNAMIC_ANT_POST)
			throw new WrongColumnException.OrientationColumn(openedImages.get(0).getRow(),
															 openedImages.get(0).getImageOrientation(),
															 new Orientation[] { Orientation.DYNAMIC_ANT, Orientation.DYNAMIC_ANT_POST });

		ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(openedImages.get(0));
		if (imps[0] != null) {
			this.impAnt = imps[0].clone();
		}

		if (imps[1] != null) {
			this.impPost = imps[1].clone();
			for (int i = 1; i <= this.impPost.getImagePlus().getStackSize(); i++) {
				this.impPost.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
		}

		openedImages.get(0).getImagePlus().close();

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

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(impProjeteeAnt);
		selection.add(impAnt);
		selection.add(impPost);
		selection.add(impProjeteePost);

		return selection;
	}
}
