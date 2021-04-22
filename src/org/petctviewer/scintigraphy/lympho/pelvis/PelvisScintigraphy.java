package org.petctviewer.scintigraphy.lympho.pelvis;

import org.petctviewer.scintigraphy.lympho.ModelLympho;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class PelvisScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Post Scintigraphy";
	final TabResult resultTab;

	public PelvisScintigraphy(TabResult tab) {
		super(STUDY_NAME);

		this.resultTab = tab;
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		// Check number of images
		if (selectedImages.size() != 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection impSorted;
		List<ImageSelection> impsSortedAntPost = new ArrayList<>();

		for (ImageSelection imp : selectedImages) {

			if (imp.getImageOrientation() == Orientation.ANT_POST ||
					imp.getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else {
				throw new WrongColumnException.OrientationColumn(imp.getRow(), imp.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT});
			}
			int ratio = (int) (25000 / impSorted.getImagePlus().getStatistics().max);
			// On augmente le contraste(uniquement visuel, n'impacte pas les données)
			impSorted.getImagePlus().getProcessor().setMinAndMax(0, impSorted.getImagePlus().getStatistics().max *
					(1.0d / ratio));

			impsSortedAntPost.add(impSorted);
			imp.getImagePlus().close();
		}

		for (ImageSelection selected : selectedImages)
			selected.close();

		List<ImageSelection> selection = new ArrayList<>();
		for (ImageSelection imageSelection : impsSortedAntPost) {
			selection.add(imageSelection.clone());
		}
		return selection;
	}

	@Override
	public String instructions() {
		return "1 image Ant-Post or Post-Ant";
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.initOverlayOnPreparedImages(preparedImages);

		((ModelLympho) this.resultTab.getParent().getModel()).setImagePelvis(preparedImages.get(0));

		this.setFenApplication(
				new FenApplicationPelvis(((ModelLympho) this.resultTab.getParent().getModel()).getImagePelvis(),
										 this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowPelvis(this, (FenApplicationWorkflow) this.getFenApplication(),
											 this.resultTab.getParent().getModel(), this.resultTab));

		this.getFenApplication().setVisible(true);

	}

}
