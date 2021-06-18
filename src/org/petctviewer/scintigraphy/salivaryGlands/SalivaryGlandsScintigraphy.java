package org.petctviewer.scintigraphy.salivaryGlands;

import java.util.ArrayList;
import java.util.List;

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

import ij.ImageStack;

public class SalivaryGlandsScintigraphy extends Scintigraphy {

    public static final String STUDY_NAME = "Salivary Glands scintigraphy";
	private ImageSelection impAnt;
	private int[] frameDurations;

    public SalivaryGlandsScintigraphy() {
        super(STUDY_NAME);
    }

    private void createDocumentation() {
        DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
        doc.setDeveloper("Angele Mateos");
        doc.addReference(DocumentationDialog.Field.createLinkField("", "Kaldway - Nucl Med Commun 2019",
                "https://pubmed.ncbi.nlm.nih.gov/30807534/"));
        doc.addReference(DocumentationDialog.Field.createLinkField("", "Aksoy - Clin Rheumatol 2012",
                "https://pubmed.ncbi.nlm.nih.gov/22733368/"));
        doc.setYoutube("");
        doc.setOnlineDoc("");
        this.getFenApplication().setDocumentation(doc);
    }

    @Override
	public void start(List<ImageSelection> preparedImages) {
        this.initOverlayOnPreparedImages(preparedImages, 7);
		this.setFenApplication(new FenApplication_SalivaryGlands(preparedImages.get(0), this.getStudyName(), this));
		this.getFenApplication().setController(
				new ControllerWorkflowSalivaryGlands((FenApplicationWorkflow) this.getFenApplication(),
                                            new ModelSalivaryGlands(this.frameDurations, preparedImages.toArray(new ImageSelection[0]), STUDY_NAME, this.impAnt)));
		this.createDocumentation();
    }
    
    public int[] getFrameDurations() {
		return frameDurations;
	}

	public ImageSelection getImpAnt() {
		return impAnt;
	}

    @Override
    public Column[] getColumns() {
        return Column.getDefaultColumns();
    }

    @Override
    public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
            throws WrongInputException {
        // Check number of images
		if (selectedImages.size() != 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

        // Check orientations
        // With 1 image
        if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_ANT) {
            // Set images
            this.impAnt = selectedImages.get(0).clone();
        } else throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
                selectedImages.get(0).getImageOrientation(), new Orientation[]{Orientation.DYNAMIC_ANT},
                "You can only use a Dynamic ANT");

        // Close images
        selectedImages.forEach(ImageSelection::close);

        // Build frame duration
        this.frameDurations = Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus());

        ImageSelection impAntCountPerSec = this.impAnt.clone();
        Library_Dicom.normalizeToCountPerSecond(impAntCountPerSec);

        ImageSelection impProjetee = Library_Dicom.project(impAntCountPerSec,
                0, impAntCountPerSec.getImagePlus().getStackSize(), "avg");
        ImageStack stack = impProjetee.getImagePlus().getStack();

        // ajout du stack a l'imp
        impProjetee.getImagePlus().setStack(stack);

        List<ImageSelection> selection = new ArrayList<>();
        selection.add(impProjetee);
        selection.add(impAnt);

        return selection;
    }

    @Override
    public String instructions() {
        return "1 dynamic image Ant";
    }

    
}