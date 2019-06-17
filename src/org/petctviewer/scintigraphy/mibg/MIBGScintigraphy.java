package org.petctviewer.scintigraphy.mibg;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class MIBGScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "MIBG Scintigraphy";

	public MIBGScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Carrio JACC 2010",
																   "https://www.ncbi.nlm.nih.gov/pubmed/20129538"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowMIBG(STUDY_NAME, (FenApplicationWorkflow) this.getFenApplication(),
										   preparedImages.toArray(new ImageSelection[0])));
		this.createDocumentation();

	}

	@Override
	public Column[] getColumns() {
		// Orientation column
		String[] orientationValues =
				{Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString(), Orientation.ANT.toString()};
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Choose columns to display
		return new Column[]{Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
							Column.STACK_SIZE, orientation};
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
		// Check number
		if (openedImages.size() != 2) throw new WrongNumberImagesException(openedImages.size(), 2);

		List<ImageSelection> impSelect = new ArrayList<>();
		for (ImageSelection openedImage : openedImages) {
			if (openedImage.getImageOrientation() == Orientation.ANT_POST ||
					openedImage.getImageOrientation() == Orientation.POST_ANT) {
				impSelect.add(Library_Dicom.ensureAntPostFlipped(openedImage));
			} else if (openedImage.getImageOrientation() == Orientation.ANT) {
				impSelect.add(openedImage);
			} else {
				throw new WrongColumnException.OrientationColumn(openedImage.getRow(),
																 openedImage.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT,
																				   Orientation.ANT});
			}
		}

		// Order images by time
		impSelect.sort(new ChronologicalAcquisitionComparator());

		// Close images
		for (ImageSelection ims : openedImages)
			ims.close();

		return impSelect;
	}

	@Override
	public String instructions() {
		return "2 images. Ant-Post / Post-Ant or Ant orientations accepted.";
	}
}
