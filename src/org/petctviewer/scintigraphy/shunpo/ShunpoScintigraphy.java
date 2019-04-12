package org.petctviewer.scintigraphy.shunpo;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.shunpo.FenSelectionDicom.Column;

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
		FenSelectionDicom fen = new FenSelectionDicom(this.getExamType(), this);

		// Orientation column
		String[] orientationValues = { Orientation.ANT.toString(), Orientation.POST.toString(),
				Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString(), Orientation.UNKNOWN.toString() };
		Column orientation = new Column("Orientation", orientationValues);

		// Organ column
		String[] organValues = { ORGAN_KIDNEY_PULMON, ORGAN_BRAIN };
		this.orgranColumn = new Column("Ogran", organValues);

		// Choose columns to display
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation, this.orgranColumn };
		fen.declareColumns(cols);

		fen.setVisible(true);
	}

	/**
	 * TODO: remove this method and use preparerImp
	 * 
	 * @param selectedImages Selected images by the user
	 */
	public void startExam(ImageSelection[] selectedImages) {
		// Check that number of images is correct
		if (selectedImages.length != 2) {
			JOptionPane.showMessageDialog(this.getFenApplication(),
					"2 images expected, " + selectedImages.length + " received", "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (selectedImages[0].getValue(this.orgranColumn.getName()) == selectedImages[1]
				.getValue(this.orgranColumn.getName())) {
			JOptionPane.showMessageDialog(this.getFenApplication(),
					"Organs must be " + ORGAN_KIDNEY_PULMON + " and " + ORGAN_BRAIN, "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Order selectedImages: 1st KIDNEY-PULMON; 2nd BRAIN
		ImageSelection tmp;
		if (!selectedImages[0].getValue(this.orgranColumn.getName()).equals(ORGAN_KIDNEY_PULMON)) {
			tmp = selectedImages[0];
			selectedImages[0] = selectedImages[1];
			selectedImages[1] = tmp;
		}

		// Check orientation
		for (int idImg = 0; idImg < 2; idImg++) {
			if (selectedImages[idImg].getImageOrientation() == Orientation.ANT_POST) {
				selectedImages[idImg].getImagePlus().getStack().getProcessor(2).flipHorizontal();
			} else if (selectedImages[idImg].getImageOrientation() == Orientation.POST_ANT) {
				selectedImages[idImg].getImagePlus().getStack().getProcessor(1).flipHorizontal();
			} else {
				JOptionPane.showMessageDialog(this.getFenApplication(), "Bad orientation", "",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		this.setImp(selectedImages[0].getImagePlus());

		// Start program
		this.setFenApplication(new FenApplication_Shunpo(this));
		this.getFenApplication().setControleur(new ControleurShunpo(this, this.getFenApplication(), selectedImages));
//		this.getFenApplication().setControleur(new ControleurShunpo_KidneyPulmon(this));
		this.getFenApplication().setVisible(true);
	}

	@Override
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		return null;
	}

	@Override
	public void lancerProgramme() {
	}

}
