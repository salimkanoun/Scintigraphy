package org.petctviewer.scintigraphy.lympho.pelvis;

import java.util.ArrayList;
import java.util.List;

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

public class PelvisScintigraphy extends Scintigraphy {

	final TabResult resultTab;

	public PelvisScintigraphy(String studyName, TabResult tab) {
		super("Post Scintigraphy");

		this.resultTab = tab;
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		// Check number of images
		if (selectedImages.size() != 1)
			throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection impSorted;
		List<ImageSelection> impsSortedAntPost = new ArrayList<>();

		for (int index = 0; index < selectedImages.size(); index++) {

			ImageSelection imp = selectedImages.get(index);
			if (selectedImages.get(index).getImageOrientation() == Orientation.ANT_POST
					|| selectedImages.get(index).getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else {
				throw new WrongColumnException.OrientationColumn(selectedImages.get(index).getRow(),
						selectedImages.get(index).getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
			}
			int ratio = (int) (25000 / impSorted.getImagePlus().getStatistics().max);
			// On augmente le contraste(uniquement visuel, n'impacte pas les données)
			impSorted.getImagePlus().getProcessor().setMinAndMax(0,
					impSorted.getImagePlus().getStatistics().max * (1.0d / ratio));

			impsSortedAntPost.add(impSorted);
			selectedImages.get(index).getImagePlus().close();
		}

		List<ImageSelection> selection = new ArrayList<>();
		for (int i = 0; i < impsSortedAntPost.size(); i++) {
			selection.add(impsSortedAntPost.get(i).clone());
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationPelvis(selectedImages[0], this.getStudyName()));
		this.getFenApplication()
				.setController(new ControllerWorkflowPelvis(this, (FenApplicationWorkflow) this.getFenApplication(),
						new ModelPelvis(selectedImages, "Pelvis Scinty", this.resultTab), this.resultTab));
		this.getFenApplication().setVisible(true);

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Column[] getColumns() {
		// TODO Auto-generated method stub
		return null;
	}

}
