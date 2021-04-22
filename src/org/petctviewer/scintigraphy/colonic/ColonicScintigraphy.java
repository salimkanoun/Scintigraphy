package org.petctviewer.scintigraphy.colonic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class ColonicScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Colonic Scintigraphy";

	public ColonicScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Maurer JNM 2013",
																   "https://www.ncbi.nlm.nih.gov/pubmed/24092937"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.initOverlayOnPreparedImages(preparedImages);
		this.setFenApplication(new FenApplicationColonicTransit(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowColonicTransit((FenApplicationColonicTransit) this.getFenApplication(),
													 preparedImages.toArray(new ImageSelection[0])));
		this.createDocumentation();
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
		// Check number
		if (openedImages.size() < 2 || openedImages.size() > 4) throw new WrongNumberImagesException(
				openedImages.size(), 2, 4);

		List<ImageSelection> impSelect = new ArrayList<>();
		for (ImageSelection openedImage : openedImages) {
			if (openedImage.getImageOrientation() == Orientation.ANT_POST ||
					openedImage.getImageOrientation() == Orientation.POST_ANT) {
				impSelect.add(Library_Dicom.ensureAntPostFlipped(openedImage));
			} else {
				throw new WrongColumnException.OrientationColumn(openedImage.getRow(),
																 openedImage.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT});
			}
		}

		for (ImageSelection opened : openedImages)
			opened.close();

		// Order images by time
		impSelect.sort(new ChronologicalAcquisitionComparator());
		return impSelect;
	}

	@Override
	public String instructions() {
		return "2 to 4 images. Ant-Post or Post-Ant orientations accepted.";
	}
}
