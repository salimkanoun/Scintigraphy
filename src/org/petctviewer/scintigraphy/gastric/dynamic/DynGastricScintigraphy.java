package org.petctviewer.scintigraphy.gastric.dynamic;

import org.petctviewer.scintigraphy.gastric.Model_Gastric;
import org.petctviewer.scintigraphy.gastric.tabs.TabMethod1;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.ReversedChronologicalAcquisitionComparator;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynGastricScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Dynamic Gastric Scintigraphy";
	private final Model_Gastric model;
	private final TabMethod1 tabResult;

	public DynGastricScintigraphy(Model_Gastric model, TabMethod1 tabResult) {
		super(STUDY_NAME);
		this.model = model;
		this.tabResult = tabResult;
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.setFenApplication(new FenApplication_DynGastric(preparedImages.get(0), "Dynamic Gastric Scintigraphy"));
		this.getFenApplication().setController(
				new ControllerWorkflow_DynGastric((FenApplicationWorkflow) this.getFenApplication(), this.model,
												  preparedImages.toArray(new ImageSelection[0]), tabResult));
		this.getFenApplication().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tabResult.enableDynamicAcquisition(true);
			}
		});
		this.getFenApplication().setVisible(true);
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException,
			ReadTagException {
		// Check number of images
		if (openedImages.size() == 0) throw new WrongNumberImagesException(openedImages.size(), 1, Integer.MAX_VALUE);

		// Check orientation
		Orientation[] acceptedOrientations =
				new Orientation[]{Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT, Orientation.DYNAMIC_ANT};
		List<ImageSelection> selection = new ArrayList<>();
		for (ImageSelection ims : openedImages) {
			if (Arrays.stream(acceptedOrientations).noneMatch(o -> o.equals(ims.getImageOrientation()))) {
				throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
																 acceptedOrientations);
			}

			// Sort orientation to always have Ant
			if (ims.getImageOrientation() == Orientation.DYNAMIC_ANT_POST ||
					ims.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
				ImageSelection[] dyn = Library_Dicom.splitDynamicAntPost(ims);
				selection.add(Library_Dicom.project(dyn[0], 1, 10, "sum"));
			} else {
				selection.add(ims.clone());
			}
		}

		// Close other images
		openedImages.forEach(ImageSelection::close);

		// Order image by time (reversed)
		selection.sort(new ReversedChronologicalAcquisitionComparator());

		return selection;
	}

	@Override
	public String instructions() {
		return "Minimum 1 image. Dynamic Ant-Post, Post-Ant or Ant orientations accepted.";
	}
}
