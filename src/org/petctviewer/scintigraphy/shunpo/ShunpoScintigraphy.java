package org.petctviewer.scintigraphy.shunpo;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.shunpo.FenSelectionDicom.Column;

import ij.ImagePlus;

public class ShunpoScintigraphy extends Scintigraphy {

	public ShunpoScintigraphy() {
		super("Pulmonary Shunt");
	}

	@Override
	public void run(String arg) {
		// Override to use custom dicom selection window
		FenSelectionDicom fen = new FenSelectionDicom(this.getExamType(), this);

		// Choose columns to display
		String[] organValues = { "KIDNEY-PULMON", "BRAIN" };
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, Column.ORIENTATION, new Column("Organ", organValues) };
		fen.declareColumns(cols);
		fen.setVisible(true);
	}

	/**
	 * TODO: remove this method and use preparerImp
	 * 
	 * @param selectedImages Selected images by the user
	 */
	public void startExam(ImageSelection[] selectedImages) {
		if (selectedImages.length != 2) {
			JOptionPane.showMessageDialog(this.getFenApplication(),
					"2 images expected, " + selectedImages.length + " received", "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (selectedImages[0].getValue("Organ") == selectedImages[1].getValue("Organ")) {
			JOptionPane.showMessageDialog(this.getFenApplication(), "Organs must be KIDNEY-PULMON and BRAIN", "",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.setImp(selectedImages[0].getImagePlus());
		this.lancerProgramme();
		return;
	}

	@Override
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		return null;
	}

	@Override
	public void lancerProgramme() {
		// Create FenApplication
		this.setFenApplication(new FenApplication_Shunpo(this));
		this.getFenApplication().setControleur(new ControleurShunpo(this));
		this.getFenApplication().setVisible(true);
	}

}
