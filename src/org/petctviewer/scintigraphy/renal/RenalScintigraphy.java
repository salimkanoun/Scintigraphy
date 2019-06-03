package org.petctviewer.scintigraphy.renal;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.Arrays;

public class RenalScintigraphy extends Scintigraphy {

	private ImageSelection impAnt;
	private ImageSelection impPost;
	private int[] frameDurations;

	public RenalScintigraphy() {
		super("Renal scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException, ReadTagException {
		// Check number of images
		if (selectedImages.length != 1 && selectedImages.length != 2)
			throw new WrongNumberImagesException(selectedImages.length, 1, 2);

		// Check orientations
		// With 1 image
		if (selectedImages.length == 1) {
			if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				// Set images
				ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(selectedImages[0]);
				this.impAnt = imps[0];
				this.impPost = imps[1];
			} else if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_POST) {
				// Only Dyn Post
				this.impPost = selectedImages[0];
			} else
				throw new WrongColumnException.OrientationColumn(selectedImages[0].getRow(),
						selectedImages[0].getImageOrientation(), new Orientation[]{Orientation.DYNAMIC_POST,
						Orientation.DYNAMIC_ANT_POST}, "You" + " can also use 2 dynamics (Ant and Post)");
		}
		// With 2 images
		else {
			Orientation[] acceptedOrientations = new Orientation[]{Orientation.DYNAMIC_POST, Orientation.DYNAMIC_ANT};
			String hint = "You can also use only 1 dynamic (Ant_Post)";

			// Image 0 must be Dyn Ant or Post
			if (Arrays.stream(acceptedOrientations).noneMatch(o -> o == selectedImages[0].getImageOrientation()))
				throw new WrongColumnException.OrientationColumn(selectedImages[0].getRow(),
						selectedImages[0].getImageOrientation(), acceptedOrientations);
			// Image 1 must be the invert of image 0
			if (selectedImages[1].getImageOrientation() != selectedImages[0].getImageOrientation().invert())
				throw new WrongColumnException.OrientationColumn(selectedImages[1].getRow(),
						selectedImages[1].getImageOrientation(),
						new Orientation[]{selectedImages[0].getImageOrientation().invert()});

			// Set images
			if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT) {
				this.impAnt = selectedImages[0].clone();
				this.impPost = selectedImages[1].clone();
			} else {
				this.impAnt = selectedImages[1].clone();
				this.impPost = selectedImages[0].clone();
			}
		}

		// Close images
		for (ImageSelection ims : selectedImages)
			ims.getImagePlus().close();

		// Build frame duration
		this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost.getImagePlus());

		// Ant processing
		if (this.impAnt != null) {
			// Check frame duration identical
			if (!ArrayUtils.isEquals(this.frameDurations, Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus())))
				throw new WrongInputException("Frame durations are not the same for Ant and Post!");

			// Flip Ant
			for (int i = 1; i <= this.impAnt.getImagePlus().getStackSize(); i++) {
				this.impAnt.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
		}

		// TODO: from this part, still no refactored @noa
		// \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/


		ImageSelection impPostCountPerSec = this.impPost.clone();
		Library_Dicom.normalizeToCountPerSecond(impPostCountPerSec);

		ImageSelection impProjetee = Library_Dicom.project(impPostCountPerSec, 0,
				impPostCountPerSec.getImagePlus().getStackSize(), "avg");
		ImageStack stack = impProjetee.getImagePlus().getStack();

		// deux premieres minutes
		int fin = ModelScinDyn.getSliceIndexByTime(2 * 60 * 1000, frameDurations);
		ImageSelection impPostFirstMin = Library_Dicom.project(impPostCountPerSec, 0, fin, "avg");
		stack.addSlice(impPostFirstMin.getImagePlus().getProcessor());

		// MIP
		ImagePlus pj = ZProjector.run(impPostCountPerSec.getImagePlus(), "max", 0,
				impPostCountPerSec.getImagePlus().getNSlices());
		stack.addSlice(pj.getProcessor());

		// ajout de la prise ant si elle existe

		ImageSelection impAntCountPerSec = this.impAnt.clone();
		Library_Dicom.normalizeToCountPerSecond(impAntCountPerSec);

		ImageSelection impProjAnt = Library_Dicom.project(impAntCountPerSec, 0,
				impAntCountPerSec.getImagePlus().getStackSize(), "avg");
		impProjAnt.getImagePlus().getProcessor().flipHorizontal();
		impAnt = impProjAnt;
		stack.addSlice(impProjAnt.getImagePlus().getProcessor());

		// ajout du stack a l'imp
		impProjetee.getImagePlus().setStack(stack);
		int nbImage = 0;
		if (impPost != null) nbImage++;
		nbImage++;
		if (impAnt != null) nbImage++;

		ImageSelection[] selection = new ImageSelection[nbImage];

		nbImage = 0;

		selection[nbImage] = impProjetee;
		nbImage++;
		if (impPost != null) {
			selection[nbImage] = impPost;
			nbImage++;
		}
		if (impAnt != null) {
			selection[nbImage] = impAnt;
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplication_Renal(selectedImages[0], this.getStudyName(), this));
		this.getFenApplication().setController(new ControllerWorkflowRenal(this,
				(FenApplicationWorkflow) this.getFenApplication(), new Model_Renal(this.frameDurations, selectedImages
				, "Renal scintigraphy")));
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	public ImageSelection getImpAnt() {
		return impAnt;
	}

	public ImageSelection getImpPost() {
		return impPost;
	}

}
