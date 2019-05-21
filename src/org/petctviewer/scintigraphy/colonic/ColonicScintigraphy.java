package org.petctviewer.scintigraphy.colonic;

import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class ColonicScintigraphy extends Scintigraphy {

	public ColonicScintigraphy(String studyName) {
		super("ColonicScintigraphy");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// TODO Auto-generated method stub

		
		
		ImageSelection[] impSelect = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			if (openedImages[i].getImageOrientation() == Orientation.ANT_POST
					|| openedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSelect[i] = Library_Dicom.ensureAntPostFlipped(openedImages[i]);
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT_POST | POST_ANT ");
			}
			openedImages[i].getImagePlus().close();
		}
		
		// Order images by time
		Arrays.parallelSort(impSelect, new ChronologicalAcquisitionComparator());
		
		return impSelect;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// TODO Auto-generated method stub

		this.setFenApplication(new FenApplicationColonicTransit(selectedImages[0], this.getStudyName()));
		((FenApplicationColonicTransit) this.getFenApplication()).setControleur(new ControllerWorkflowColonicTransit(
				this, (FenApplicationColonicTransit) this.getFenApplication(), selectedImages));
	}

}
