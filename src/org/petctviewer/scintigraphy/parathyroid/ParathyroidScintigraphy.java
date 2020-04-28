package org.petctviewer.scintigraphy.parathyroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.CheckboxMenuItem;

import org.petctviewer.scintigraphy.parathyroid.ControllerWorkflowParathyroid.DisplayState;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import java.awt.Menu;

public class ParathyroidScintigraphy extends Scintigraphy {

    public static final String STUDY_NAME = "Parathyroid";
    private static final String ORGAN_PARATHYROID = "PARATHYROID", ORGAN_THYROID = "THYROID";

    private Column organColumn;

    public ParathyroidScintigraphy(){
        super(STUDY_NAME);
    }

    private void createDocumentation() {
		final DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.setDeveloper("Angele Mateos");
		doc.addReference(DocumentationDialog.Field.createTextField("Parathyroid",
				"VILLANEUEVA-MEYER Clinical " + "Nuclear Medecine 1986"));
		// reference link ?
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

    @Override
    public Column[] getColumns() {
        //Orientation column
		final String[] orientationValues= {Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString()};
		final Column orientation = new Column (Column.ORIENTATION.getName(), orientationValues);
		
		//Organ column
		final String[] organValues = {ORGAN_PARATHYROID};
		this.organColumn = new Column("Organ", organValues);
		
		//Choose columns to display
		return new Column[] {Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
			Column.STACK_SIZE, orientation, this.organColumn};
    }

    @Override
    public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
            throws WrongInputException, ReadTagException {
        // Check that number of images is correct
        if(selectedImages.size() != 2) throw new WrongNumberImagesException(selectedImages.size(), 2);
        if (selectedImages.get(0).getValue(this.organColumn.getName()) == selectedImages.get(1).getValue(
				this.organColumn.getName())) throw new WrongColumnException(organColumn,
																			 selectedImages.get(0).getRow(),
																			 "expecting " + ORGAN_THYROID +
																					 " and " + ORGAN_PARATHYROID);

        // Order selectedImages: 1st THYROID; 2nd PARATHYROID
		if (!selectedImages.get(0).getValue(this.organColumn.getName()).equals(ORGAN_THYROID)) {
			Collections.swap(selectedImages, 0, 1);
		}
        //Check orientation
        final List<ImageSelection> result = new ArrayList<>();
        for(final ImageSelection ims : selectedImages){
            result.add(Library_Dicom.ensureAntPost(ims));
            ims.close();
        }
        return result;
    }

    @Override
    public String instructions() {
        return "Two pictures in Ant orientation";
    }

    @Override
    public void start(List<ImageSelection> preparedImages) {
        this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
            new ControllerWorkflowParathyroid(
				(FenApplicationWorkflow) getFenApplication(), preparedImages.toArray(new ImageSelection[0])));
		this.createDocumentation();
		this.inflateMenuBar((ControllerWorkflowParathyroid) this.getFenApplication().getController());
		
		this.getFenApplication().setVisible(true);
    }

    private void inflateMenuBar(final ItemListener listener) {
		final Menu menu = new Menu("Display");
		final RadioGroup group = new RadioGroup();

		final CheckboxMenuItem itemChangeLabelRL = new CheckboxMenuItem(DisplayState.RIGHT_LEFT.label);
		group.addRadioItem(itemChangeLabelRL);
		menu.add(itemChangeLabelRL);

		final CheckboxMenuItem itemChangeLabelLR = new CheckboxMenuItem(DisplayState.LEFT_RIGHT.label);
		group.addRadioItem(itemChangeLabelLR);
		menu.add(itemChangeLabelLR);

		final CheckboxMenuItem itemChangeLabelAP = new CheckboxMenuItem(DisplayState.ANT_POST.label, true);
		group.addRadioItem(itemChangeLabelAP);
		menu.add(itemChangeLabelAP);

		itemChangeLabelRL.addItemListener(listener);
		itemChangeLabelLR.addItemListener(listener);
		itemChangeLabelAP.addItemListener(listener);

		this.getFenApplication().getMenuBar().add(menu);
    }
    
    private class RadioGroup implements ItemListener{
        private final Set<CheckboxMenuItem> items;

        public RadioGroup(){
            this.items = new HashSet<>();
        }

        public void addRadioItem(final CheckboxMenuItem item){
            this.items.add(item);
            item.addItemListener(this);
        }

        @Override
        public void itemStateChanged(final ItemEvent e){
            if(e.getStateChange() == ItemEvent.SELECTED){
                //Uncheck all
                this.items.forEach(i -> i.setState(false));
            }
            //Activate only source
            ((CheckboxMenuItem) e.getSource()).setState(true);
        }
    }    
    
}