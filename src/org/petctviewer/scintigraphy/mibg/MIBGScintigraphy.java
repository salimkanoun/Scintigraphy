package org.petctviewer.scintigraphy.mibg;

import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class MIBGScintigraphy extends Scintigraphy {

	public MIBGScintigraphy() {
		super("MIBG Scintigraphy");
	}

	@Override
	public void run(String arg) {
		// Override to use custom dicom selection window
		FenSelectionDicom fen = new FenSelectionDicom(this.getStudyName(), this);

		// Orientation column
		String[] orientationValues = { Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString(),
				Orientation.ANT.toString() };
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Choose columns to display
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation };
		fen.declareColumns(cols);

		fen.setVisible(true);
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// Check number
		if (openedImages.length != 2)
			throw new WrongNumberImagesException(openedImages.length, 2);

		ImageSelection[] impSelect = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			if (openedImages[i].getImageOrientation() == Orientation.ANT_POST
					|| openedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSelect[i] = Library_Dicom.ensureAntPostFlipped(openedImages[i]);
			} else if (openedImages[i].getImageOrientation() == Orientation.ANT) {
				impSelect[i] = openedImages[i];
			} else {
				throw new WrongColumnException.OrientationColumn(openedImages[i].getRow(),
						openedImages[i].getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT, Orientation.ANT });
			}
		}

		// Order images by time
		Arrays.parallelSort(impSelect, new ChronologicalAcquisitionComparator());

		// Close images
		for(ImageSelection ims : openedImages)
			ims.close();

		return impSelect;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], this.getStudyName()));
		this.getFenApplication().setController(new ControllerWorkflowMIBG("MIBG Scintigraphy",
				(FenApplicationWorkflow) this.getFenApplication(), selectedImages));

	}

}
