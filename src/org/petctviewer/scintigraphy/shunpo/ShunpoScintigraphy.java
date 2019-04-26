package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;

public class ShunpoScintigraphy extends Scintigraphy {

	private Column orgranColumn;
	private static final String ORGAN_KIDNEY_PULMON = "KIDNEY-PULMON", ORGAN_BRAIN = "BRAIN";

	public ShunpoScintigraphy() {
		super("Pulmonary Shunt");
	}

	@Override
	public void run(String arg) {
		// Override to use custom dicom selection window
		FenSelectionDicom fen = new FenSelectionDicom(this.getStudyName(), this);

		// Orientation column
		String[] orientationValues = { Orientation.ANT.toString(), Orientation.POST.toString(),
				Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString(), Orientation.UNKNOWN.toString() };
		Column orientation = new Column("Orientation", orientationValues);

		// Organ column
		String[] organValues = { ORGAN_KIDNEY_PULMON, ORGAN_BRAIN };
		this.orgranColumn = new Column("Organ", organValues);

		// Choose columns to display
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation, this.orgranColumn };
		fen.declareColumns(cols);

		fen.setVisible(true);
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		// Check that number of images is correct
		if (selectedImages.length != 2)
			throw new WrongNumberImagesException(selectedImages.length, 2);

		if (selectedImages[0].getValue(this.orgranColumn.getName()) == selectedImages[1]
				.getValue(this.orgranColumn.getName()))
			throw new WrongColumnException(orgranColumn, selectedImages[0].getRow(),
					"expecting " + ORGAN_KIDNEY_PULMON + " and " + ORGAN_BRAIN);

		// Order selectedImages: 1st KIDNEY-PULMON; 2nd BRAIN
		ImageSelection tmp;
		if (!selectedImages[0].getValue(this.orgranColumn.getName()).equals(ORGAN_KIDNEY_PULMON)) {
			tmp = selectedImages[0];
			selectedImages[0] = selectedImages[1];
			selectedImages[1] = tmp;
		}

		// Check orientation
		ImagePlus[] toClose = new ImagePlus[selectedImages.length];
		for (int idImg = 0; idImg < 2; idImg++) {
			toClose[idImg] = selectedImages[idImg].getImagePlus();
			selectedImages[idImg] = Library_Dicom.ensureAntPostFlipped(selectedImages[idImg]);
		}
		for(ImagePlus imp : toClose)
			imp.close();

		return selectedImages;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// Start program
		this.setFenApplication(new FenApplication_Shunpo(this, selectedImages[0].getImagePlus()));
//		this.getFenApplication()
////				.setControleur(new ControleurShunpo(this, this.getFenApplication(), selectedImages, "Pulmonary Shunt"));
//				.setControleur(new ControllerShunpo(this, getFenApplication(), selectedImages, "Pulmonary Shunt"));
		this.getFenApplication()
//		.setControleur(new ControleurShunpo(this, this.getFenApplication(), selectedImages, "Pulmonary Shunt"));
		.setControleur(new ControllerWorkflowShunpo(this, getFenApplication(),new ModeleShunpo(selectedImages, "Pulmonary Shunt")));
		this.getFenApplication().setVisible(true);
	}

}
