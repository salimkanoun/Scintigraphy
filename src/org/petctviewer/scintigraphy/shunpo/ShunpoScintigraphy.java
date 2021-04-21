package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.shunpo.ControllerWorkflowShunpo.DisplayState;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;

public class ShunpoScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Pulmonary Shunt";
	private static final String ORGAN_KIDNEY_PULMON = "KIDNEY-PULMON", ORGAN_BRAIN = "BRAIN";
	private Column orgranColumn;

	public ShunpoScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.setDeveloper("Someone");
		doc.addReference(DocumentationDialog.Field.createTextField("With Kidney", "VILLANEUEVA-MEYER Clinical " +
				"Nuclear Medecine 1986"));
		doc.addReference(
				DocumentationDialog.Field.createLinkField("Brain alone", "KROWKA Chest 2000", "http://google" + ".fr"
				));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	private void inflateMenuBar(ItemListener listener) {
		// Menu change labels
		Menu menu = new Menu("Display");
		RadioGroup group = new RadioGroup();

		CheckboxMenuItem itemChangeLabelRL = new CheckboxMenuItem(DisplayState.RIGHT_LEFT.label);
		group.addRadioItem(itemChangeLabelRL);
		menu.add(itemChangeLabelRL);

		CheckboxMenuItem itemChangeLabelLR = new CheckboxMenuItem(DisplayState.LEFT_RIGHT.label);
		group.addRadioItem(itemChangeLabelLR);
		menu.add(itemChangeLabelLR);

		CheckboxMenuItem itemChangeLabelAP = new CheckboxMenuItem(DisplayState.ANT_POST.label, true); // default
		group.addRadioItem(itemChangeLabelAP);
		menu.add(itemChangeLabelAP);

		// Add listeners
		itemChangeLabelRL.addItemListener(listener);
		itemChangeLabelLR.addItemListener(listener);
		itemChangeLabelAP.addItemListener(listener);

		this.getFenApplication().getMenuBar().add(menu);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		// Start program
		this.initOverlayOnPreparedImages(preparedImages);
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowShunpo((FenApplicationWorkflow) getFenApplication(),
											 preparedImages.toArray(new ImageSelection[0])));

		this.createDocumentation();
		this.inflateMenuBar((ControllerWorkflowShunpo) this.getFenApplication().getController());

		this.getFenApplication().setVisible(true);
	}

	@Override
	public Column[] getColumns() {
		// Orientation column
		String[] orientationValues = {Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString()};
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Organ column
		String[] organValues = {ORGAN_KIDNEY_PULMON, ORGAN_BRAIN};
		this.orgranColumn = new Column("Organ", organValues);

		// Choose columns to display
		return new Column[]{Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
							Column.STACK_SIZE, orientation, this.orgranColumn};
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		// Check that number of images is correct
		if (selectedImages.size() != 2) throw new WrongNumberImagesException(selectedImages.size(), 2);

		if (selectedImages.get(0).getValue(this.orgranColumn.getName()) == selectedImages.get(1).getValue(
				this.orgranColumn.getName())) throw new WrongColumnException(orgranColumn,
																			 selectedImages.get(0).getRow(),
																			 "expecting " + ORGAN_KIDNEY_PULMON +
																					 " and " + ORGAN_BRAIN);

		// Order selectedImages: 1st KIDNEY-PULMON; 2nd BRAIN
		if (!selectedImages.get(0).getValue(this.orgranColumn.getName()).equals(ORGAN_KIDNEY_PULMON)) {
			Collections.swap(selectedImages, 0, 1);
		}

		// Check orientation
		List<ImageSelection> result = new ArrayList<>();
		for (ImageSelection ims : selectedImages) {
			result.add(Library_Dicom.ensureAntPost(ims));
			ims.close();
		}

		return result;
	}

	@Override
	public String instructions() {
		return "2 images in Ant-Post or Post-Ant orientation";
	}

	private class RadioGroup implements ItemListener {

		private Set<CheckboxMenuItem> items;

		public RadioGroup() {
			this.items = new HashSet<>();
		}

		public void addRadioItem(CheckboxMenuItem item) {
			this.items.add(item);
			item.addItemListener(this);
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Uncheck all
				this.items.forEach(i -> i.setState(false));
			}
			// Activate only source
			((CheckboxMenuItem) e.getSource()).setState(true);
		}
	}
}