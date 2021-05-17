package org.petctviewer.scintigraphy.hepatic.scintivol;

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
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScintivolScintigraphy extends Scintigraphy {


    public static final String STUDY_NAME = "Scintivol scintigraphy";
    private ImageSelection imsRetention;
    private int[] frameDurations;

    public ScintivolScintigraphy() {
        super(STUDY_NAME);
    }

    private void createDocumentation() {
        DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());

        //TODO Complete missing reference
        //doc.addReference(DocumentationDialog.Field.createLinkField("Scintivol protocol", " ",
        //        " "));
        doc.addReference(DocumentationDialog.Field.createLinkField("Ekman formula", "Ekman - Nucl Med Commun. 1996",
              "https://pubmed.ncbi.nlm.nih.gov/8692492/"));
        doc.setYoutube("");
        doc.setOnlineDoc("");
        this.getFenApplication().setDocumentation(doc);
    }

    @Override
    public void start(List<ImageSelection> preparedImages) {
        this.initOverlayOnPreparedImages(preparedImages);
        this.setFenApplication(new FenApplication_Scintivol(preparedImages.get(0), this.getStudyName(), this));
        this.getFenApplication().setController(
                new ControllerWorkflow_Scintivol((FenApplicationWorkflow) this.getFenApplication(),
                        new Model_Scintivol(preparedImages.toArray(new ImageSelection[0]), STUDY_NAME, this.frameDurations, this.imsRetention)));
        this.createDocumentation();
    }

    @Override
    public Column[] getColumns() {
        return Column.getDefaultColumns();
    }

    @Override
    public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
            throws WrongInputException, ReadTagException {
        // Check number of images
        if (selectedImages.size() != 2) throw new WrongNumberImagesException(
                selectedImages.size(), 1, 2);

        selectedImages.sort(new ChronologicalAcquisitionComparator());

        // Check orientations
        List<ImageSelection> selection = new ArrayList<>();

        Orientation[] acceptedOrientations = new Orientation[]{Orientation.DYNAMIC_ANT_POST};
        String hint = "You can also use only 1 dynamic (Ant_Post)";

        // Image 0 must be Dyn Ant or Post
        if (Arrays.stream(selectedImages.toArray(new ImageSelection[0])).noneMatch(o ->
                o.getImageOrientation() == Orientation.DYNAMIC_ANT_POST))
            throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
                    selectedImages.get(0).getImageOrientation(),
                    acceptedOrientations, hint);

        // Set images
        ImageSelection imps = selectedImages.get(0).clone();
        Library_Dicom.normalizeToCountPerSecond(imps);
        imps = Library_Dicom.geomMean(imps);
        this.frameDurations = Library_Dicom.buildFrameDurations(imps.getImagePlus());
        selection.add(imps);

        ImageSelection[] imsTab = Library_Dicom.splitDynamicAntPost(selectedImages.get(1));
        imsTab[1].getImagePlus().getProcessor().flipHorizontal();
        imsTab[0].getImagePlus().getStack().addSlice(imsTab[1].getImagePlus().getProcessor());
        imps = imsTab[0].clone();
        Library_Dicom.normalizeToCountPerSecond(imps);
        this.imsRetention = imps.clone();
        imps = Library_Dicom.project(imps, 1, imps.getImagePlus().getNSlices(), "sum");
        selection.add(imps);


        // Close images
        selectedImages.forEach(ImageSelection::close);

        return selection;
    }


    @Override
    public String instructions() {
        return "1 or 2 images in dynamic A/P";
    }


}
