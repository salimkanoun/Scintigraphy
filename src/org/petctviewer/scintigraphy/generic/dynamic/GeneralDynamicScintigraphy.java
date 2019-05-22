package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;

public class GeneralDynamicScintigraphy extends Scintigraphy {

	private ImageSelection impAnt, impPost, impProjetee, impProjeteeAnt, impProjeteePost;
	private int[] frameDurations;

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_GeneralDyn(selectedImages[0], this.getStudyName(), this));
		((FenApplicationWorkflow) this.getFenApplication()).setControleur(
				new ControllerWorkflowScinDynamic(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Modele_GeneralDyn(selectedImages, "General Dynamic", this.getFrameDurations())));
		IJ.setTool(Toolbar.POLYGON);
	}

	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		// Check number images
		if (selectedImages.length != 1)
			throw new WrongNumberImagesException(selectedImages.length, 1);

		ImageSelection[] imps = new ImageSelection[2];

		if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT) {
			if (imps[0] != null)
				throw new WrongInputException("Multiple dynamic Antorior Image");
			imps[0] = selectedImages[0].clone();
		} else if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_POST) {
			if (imps[1] != null)
				throw new WrongInputException("Multiple dynamic Posterior Image");
			imps[1] = selectedImages[0].clone();
		} else if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
			if (imps[1] != null || imps[0] != null)
				throw new WrongInputException("Multiple dynamic Image");
			imps[0] = Library_Dicom.splitDynamicAntPost(selectedImages[0])[0];
			imps[1] = Library_Dicom.splitDynamicAntPost(selectedImages[0])[1];
		} else {
			throw new WrongColumnException.OrientationColumn(selectedImages[0].getRow(),
					selectedImages[0].getImageOrientation(), new Orientation[] { Orientation.DYNAMIC_ANT,
							Orientation.DYNAMIC_POST, Orientation.DYNAMIC_ANT_POST });
		}

		selectedImages[0].getImagePlus().close();

		if (imps[0] != null) {
			this.impAnt = imps[0];
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus());
			IJ.run(this.impAnt.getImagePlus(), "32-bit", "");
//			Library_Dicom.normalizeToCountPerSecond(this.impAnt.getImagePlus(), frameDurations);
		}

		if (imps[1] != null) {
			this.impPost = imps[1];
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost.getImagePlus());
			Library_Dicom.flipStackHorizontal(impPost);
			IJ.run(this.impPost.getImagePlus(), "32-bit", "");
//			Library_Dicom.normalizeToCountPerSecond(this.impPost.getImagePlus(), frameDurations);
		}

		if (this.impAnt != null) {
			impProjeteeAnt = Library_Dicom.project(this.impAnt, 0, impAnt.getImagePlus().getStackSize(), "avg");
			impProjetee = impProjeteeAnt;

		}
		if (this.impPost != null) {
			impProjeteePost = Library_Dicom.project(this.impPost, 0, impPost.getImagePlus().getStackSize(), "avg");
			impProjetee = impProjeteePost;

		}
		if (this.impAnt != null && this.impPost != null) {
			ImagePlus Ant = impProjeteeAnt.getImagePlus().duplicate();
			ImagePlus Post = impProjeteePost.getImagePlus().duplicate();
			ImageStack img = new ImageStack(Ant.getWidth(), Ant.getHeight());
			img.addSlice(Ant.getProcessor());
			img.addSlice(Post.getProcessor());
			ImagePlus ImageRetour = new ImagePlus();
			ImageRetour.setStack(img);

			ImageRetour.setProperty("Info", impPost.getImagePlus().getInfoProperty());
			impProjetee.setImagePlus(ImageRetour);
		}

		impProjetee.getImagePlus().getProcessor().setMinAndMax(0,
				impProjetee.getImagePlus().getStatistics().max * 1f / 1f);

		ImageSelection[] selection = new ImageSelection[5];
		selection[0] = impProjetee;
		selection[1] = impAnt != null ? impAnt : null;
		selection[2] = impPost != null ? impPost : null;
		selection[3] = impProjeteeAnt != null ? impProjeteeAnt : null;
		selection[4] = impProjeteePost != null ? impProjeteePost : null;
		return selection;
	}

	public ImagePlus getImpAnt() {
		return impAnt != null ? impAnt.getImagePlus() : null;
	}

	public ImagePlus getImpPost() {
		return impPost != null ? impPost.getImagePlus() : null;
	}

	public int[] getFrameDurations() {
		return frameDurations != null ? frameDurations : null;
	}

}
