package org.petctviewer.scintigraphy.generic.dynamic;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class GeneralDynamicScintigraphy extends Scintigraphy {

	private ImageSelection impAnt, impPost, impProjetee, impProjeteeAnt, impProjeteePost;
	private int[] frameDurations;

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_GeneralDyn(selectedImages[0], this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowScinDynamic(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Model_GeneralDyn(selectedImages, "General Dynamic", this.getFrameDurations())));
		IJ.setTool(Toolbar.POLYGON);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImagePlus getImpAnt() {
		return impAnt != null ? impAnt.getImagePlus() : null;
	}

	public ImagePlus getImpPost() {
		return impPost != null ? impPost.getImagePlus() : null;
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	@Override
	public Column[] getColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number images
		if (selectedImages.size() != 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection[] imps = new ImageSelection[2];

		if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_ANT) {
			if (imps[0] != null)
				throw new WrongInputException("Multiple dynamic Antorior Image");
			imps[0] = selectedImages.get(0).clone();
		} else if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_POST) {
			if (imps[1] != null)
				throw new WrongInputException("Multiple dynamic Posterior Image");
			imps[1] = selectedImages.get(0).clone();
		} else if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
			if (imps[1] != null || imps[0] != null)
				throw new WrongInputException("Multiple dynamic Image");
			imps[0] = Library_Dicom.splitDynamicAntPost(selectedImages.get(0))[0];
			imps[1] = Library_Dicom.splitDynamicAntPost(selectedImages.get(0))[1];
		} else {
			throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
															 selectedImages.get(0).getImageOrientation(),
															 new Orientation[]{Orientation.DYNAMIC_ANT,
							Orientation.DYNAMIC_POST, Orientation.DYNAMIC_ANT_POST });
		}

		selectedImages.get(0).getImagePlus().close();

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

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(impProjetee);
		selection.add(impAnt);
		selection.add(impPost);
		selection.add(impProjeteeAnt);
		selection.add(impProjeteePost);
		return selection;
	}


}
