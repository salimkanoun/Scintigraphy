package org.petctviewer.scintigraphy.lympho.pelvis;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class PelvisScintigraphy extends Scintigraphy {

	TabResult resultTab;

	public PelvisScintigraphy(String studyName, TabResult tab) {
		super("Post Scintigraphy");

		this.resultTab = tab;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		ImageSelection impSorted = null;
		ImageSelection[] impsSortedAntPost = new ImageSelection[selectedImages.length];

		for (int i = 0; i < selectedImages.length; i++) {

			impSorted = null;
			ImageSelection imp = selectedImages[i];
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST || selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT ");
			}
			int ratio = (int) (25000 / impSorted.getImagePlus().getStatistics().max);
			// On augmente le contraste(uniquement visuel, n'impacte pas les données)
			impSorted.getImagePlus().getProcessor().setMinAndMax(0, impSorted.getImagePlus().getStatistics().max * (1.0d / ratio)); 


			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] selection = new ImageSelection[impsSortedAntPost.length];
		for (int i = 0; i < impsSortedAntPost.length; i++) {
			selection[i] = impsSortedAntPost[i].clone();
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationPelvis(selectedImages[0].getImagePlus(), this.getStudyName()));
		this.getFenApplication().setControleur(
				new ControllerWorkflowPelvis(this, this.getFenApplication(), new ModelePelvis(selectedImages, "Pelvis Scinty", this.resultTab), this.resultTab));
		this.getFenApplication().setVisible(true);

	}

}
