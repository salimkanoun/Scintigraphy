package org.petctviewer.scintigraphy.renal;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import org.apache.commons.lang.ArrayUtils;
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
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenalScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Renogram scintigraphy";
	private ImageSelection impAnt;
	private ImageSelection impPost;
	private int[] frameDurations;

	public RenalScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Taylor Semin Nucl Med 2018",
																   "https://www.ncbi.nlm.nih.gov/pubmed/29852947"));
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Blaufox Eur J Nucl Med Mol Imaging. 2018",
																   "https://www.ncbi.nlm.nih.gov/pubmed/30167801"));
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Gordon I  Eur J Nucl Med Mol Imaging. 2011",
																   "https://www.ncbi.nlm.nih.gov/pubmed/21503762"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.initOverlayOnPreparedImages(preparedImages, 7);
		this.setFenApplication(new FenApplication_Renal(preparedImages.get(0), this.getStudyName(), this));
		this.getFenApplication().setController(
				new ControllerWorkflowRenal((FenApplicationWorkflow) this.getFenApplication(),
											new Model_Renal(this.frameDurations,
															preparedImages.toArray(new ImageSelection[0]),
															STUDY_NAME)));
		this.createDocumentation();
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	public ImageSelection getImpAnt() {
		return impAnt;
	}

	public ImageSelection getImpPost() {
		return impPost;
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number of images
		if (selectedImages.size() != 1 && selectedImages.size() != 2) throw new WrongNumberImagesException(
				selectedImages.size(), 1, 2);

		// Check orientations
		// With 1 image
		if (selectedImages.size() == 1) {
			if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				// Set images
				ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(selectedImages.get(0));
				this.impAnt = imps[0];
				this.impPost = imps[1];
			} else if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_POST) {
				// Only Dyn Post
				this.impPost = selectedImages.get(0).clone();
			} else throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
																	selectedImages.get(0).getImageOrientation(),
																	new Orientation[]{Orientation.DYNAMIC_POST,
																					  Orientation.DYNAMIC_ANT_POST},
																	"You can also use 2 dynamics (Ant and Post)");
		}
		// With 2 images
		else {
			Orientation[] acceptedOrientations = new Orientation[]{Orientation.DYNAMIC_POST, Orientation.DYNAMIC_ANT};
			String hint = "You can also use only 1 dynamic (Ant_Post)";

			// Image 0 must be Dyn Ant or Post
			if (Arrays.stream(acceptedOrientations).noneMatch(o -> o == selectedImages.get(0).getImageOrientation()))
				throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
																 selectedImages.get(0).getImageOrientation(),
																 acceptedOrientations, hint);
			// Image 1 must be the invert of image 0
			if (selectedImages.get(1).getImageOrientation() != selectedImages.get(0).getImageOrientation().invert())
				throw new WrongColumnException.OrientationColumn(selectedImages.get(1).getRow(),
																 selectedImages.get(1).getImageOrientation(),
																 new Orientation[]{selectedImages.get(
																		 0).getImageOrientation().invert()}, hint);

			// Set images
			if (selectedImages.get(0).getImageOrientation() == Orientation.DYNAMIC_ANT) {
				this.impAnt = selectedImages.get(0).clone();
				this.impPost = selectedImages.get(1).clone();
			} else {
				this.impAnt = selectedImages.get(1).clone();
				this.impPost = selectedImages.get(0).clone();
			}
		}

		// Close images
		selectedImages.forEach(ImageSelection::close);

		// Build frame duration
		this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost.getImagePlus());

		// Ant processing
		if (this.impAnt != null) {
			// Check frame duration identical
			if (!ArrayUtils.isEquals(this.frameDurations,
									 Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus())))
				throw new WrongInputException("Frame durations are not the same for Ant and Post!");

			// Flip Ant
			for (int i = 1; i <= this.impAnt.getImagePlus().getStackSize(); i++) {
				this.impAnt.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
		}

		// TODO: from this part, still no refactored @noa
		// \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/


		ImageSelection impPostCountPerSec = this.impPost.clone();
		Library_Dicom.normalizeToCountPerSecond(impPostCountPerSec);

		ImageSelection impProjetee = Library_Dicom.project(impPostCountPerSec, 0,
														   impPostCountPerSec.getImagePlus().getStackSize(), "avg");
		ImageStack stack = impProjetee.getImagePlus().getStack();

		// deux premieres minutes
		int fin = ModelScinDyn.getSliceIndexByTime(2 * 60 * 1000, frameDurations);
		ImageSelection impPostFirstMin = Library_Dicom.project(impPostCountPerSec, 0, fin, "avg");
		stack.addSlice(impPostFirstMin.getImagePlus().getProcessor());

		// MIP
		ImagePlus pj = ZProjector.run(impPostCountPerSec.getImagePlus(), "max", 0,
									  impPostCountPerSec.getImagePlus().getNSlices());
		stack.addSlice(pj.getProcessor());

		// ajout de la prise ant si elle existe
		if (this.impAnt != null) {
			ImageSelection impAntCountPerSec = this.impAnt.clone();
			Library_Dicom.normalizeToCountPerSecond(impAntCountPerSec);

			ImageSelection impProjAnt = Library_Dicom.project(impAntCountPerSec, 0,
															  impAntCountPerSec.getImagePlus().getStackSize(), "avg");
			impProjAnt.getImagePlus().getProcessor().flipHorizontal();
			impAnt = impProjAnt;
			stack.addSlice(impProjAnt.getImagePlus().getProcessor());
		}

		// ajout du stack a l'imp
		impProjetee.getImagePlus().setStack(stack);
		Orientation o = impProjetee.getImageOrientation();

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(impProjetee);
		selection.add(impPost);
		selection.add(impAnt);

		return selection;
	}

	@Override
	public String instructions() {
		return "1 dynamic image Ant-Post or Post or 2 static images Ant and Post";
	}

}
