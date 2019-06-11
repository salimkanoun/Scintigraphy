package org.petctviewer.scintigraphy.lympho;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.*;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class LymphoScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Lympho Scintigraphy";

	public LymphoScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationLympho(selectedImages[0], this.getStudyName()));
		// this.getFenApplication()
		// .setController(new ControleurLympho(this, this.getFenApplication(), "Lympho
		// Scinti", selectedImages));
		this.getFenApplication().setController(
				new ControllerWorkflowLympho(this, (FenApplicationWorkflow) this.getFenApplication(),
											 new ModelLympho(selectedImages, "Lympho Scinti")));
		this.getFenApplication().setVisible(true);

	}

	/**
	 * This method return the projection of a Dynamic {@link ImagePlus} to a Static {@link ImagePlus}, using the
	 * avg.<br/>
	 *
	 * @param imp : Dynamic ImagePlus you want to transform
	 * @return The static {@link ImagePlus}
	 * @throws WrongOrientationException if {@link Orientation#isDynamic()} returns FALSE
	 * @throws ReadTagException          if a DICOM tag could not be retrieved and was necessary to split the image
	 * @see Library_Dicom#splitDynamicAntPost(ImageSelection)
	 * @see Library_Dicom#project(ImageSelection, int, int, String)
	 */
	public ImageSelection dynamicToStaticAntPost(ImageSelection imp) throws WrongOrientationException,
			ReadTagException {
		ImageSelection[] Ant_Post = Library_Dicom.splitDynamicAntPost(imp);

		ImageSelection Ant = Library_Dicom.project(Ant_Post[0], 1, Ant_Post[0].getImagePlus().getStackSize(), "sum");
		ImageSelection Post = Library_Dicom.project(Ant_Post[1], 1, Ant_Post[1].getImagePlus().getStackSize(), "sum");

		ImageStack img = new ImageStack(Ant.getImagePlus().getWidth(), Ant.getImagePlus().getHeight());
		img.addSlice(Ant.getImagePlus().getProcessor());
		img.addSlice(Post.getImagePlus().getProcessor());
		ImagePlus ImageRetour = new ImagePlus();
		ImageRetour.setStack(img);

		ImageRetour.getStack().getProcessor(1).flipHorizontal();
		ImageRetour.setProperty("Info", imp.getImagePlus().getInfoProperty());

		ImageSelection imageSelectionRetour = imp.clone(Orientation.ANT_POST);
		imageSelectionRetour.setImagePlus(ImageRetour);

		return imageSelectionRetour;
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number of images
		if (selectedImages.size() != 2) throw new WrongNumberImagesException(selectedImages.size(), 2);

		selectedImages.sort(new ChronologicalAcquisitionComparator());

		ImageSelection impSorted;
		List<ImageSelection> impsSortedAntPost = new ArrayList<>();
		int DynamicPosition = -1;

		for (int i = 0; i < selectedImages.size(); i++) {

			ImageSelection imp = selectedImages.get(i);
			if (selectedImages.get(i).getImageOrientation() == Orientation.ANT_POST || selectedImages.get(i)
					.getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else if (selectedImages.get(i).getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				impSorted = imp.clone();
				DynamicPosition = i;
			} else {
				throw new WrongColumnException.OrientationColumn(selectedImages.get(i).getRow(), selectedImages.get(i).getImageOrientation(),
						new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.DYNAMIC_ANT_POST});
			}

			impsSortedAntPost.add(impSorted);
		}

		for(ImageSelection selected : selectedImages)
			selected.close();

		List<ImageSelection> impsCorrectedByTime = new ArrayList<>();
		if (DynamicPosition != -1) {
			ImageSelection staticImage = impsSortedAntPost.get(Math.abs((DynamicPosition - 1)));
			ImageSelection dynamicImage = impsSortedAntPost.get(DynamicPosition);
			int timeStatic = Library_Dicom.getFrameDuration(staticImage.getImagePlus());
			int[] timesDynamic = Library_Dicom.buildFrameDurations(dynamicImage.getImagePlus());
			int acquisitionTimeDynamic = 0;
			for (int times = 0; times < timesDynamic.length / 2; times++) {
				acquisitionTimeDynamic += timesDynamic[times];
			}

			// Create a projected image, from a dynamic Ant/Post image to static Ant/Post
			// image
			dynamicImage = dynamicToStaticAntPost(dynamicImage);
			// On calcule le ration de temps pour égaliser le nombre de coup/miliseconde
			double ratio = (timeStatic * 1.0D / acquisitionTimeDynamic * 1.0D);

			// On passe la static sur le même temps théorique que la dynamic
			IJ.run(staticImage.getImagePlus(), "Multiply...", "value=" + (1f / ratio) + " stack");
			// On ramène sur 1 minute
			IJ.run(staticImage.getImagePlus(), "Multiply...", "value=" + (60000f / acquisitionTimeDynamic) + " stack");
			// On ramène sur 1 minute
			IJ.run(dynamicImage.getImagePlus(), "Multiply...",
					"value=" + (60000f / acquisitionTimeDynamic) + " " + "stack");

			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			dynamicImage.getImagePlus().getProcessor()
					.setMinAndMax(0, dynamicImage.getImagePlus().getStatistics().max * ratio);
			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			staticImage.getImagePlus().getProcessor()
					.setMinAndMax(0, staticImage.getImagePlus().getStatistics().max * ratio);

			impsCorrectedByTime.set(Math.abs((DynamicPosition - 1)), staticImage);
			selectedImages.set(DynamicPosition, dynamicImage);

		} else {
			int timeStatic1 = Library_Dicom.getFrameDuration(impsSortedAntPost.get(0).getImagePlus());
			int timeStatic2 = Library_Dicom.getFrameDuration(impsSortedAntPost.get(1).getImagePlus());
			double ratio = (timeStatic1 * 1.0D / timeStatic2 * 1.0D);

			// On passe les deux static sur le même temps théorique
			IJ.run(impsSortedAntPost.get(0).getImagePlus(), "Multiply...", "value=" + (1f / ratio) + " stack");
			// On ramène sur 1 minute
			IJ.run(impsSortedAntPost.get(0).getImagePlus(), "Multiply...", "value=" + (60000f / timeStatic2) +
					" stack");
			// On ramène sur 1 minute
			IJ.run(impsSortedAntPost.get(1).getImagePlus(), "Multiply...", "value=" + (60000f / timeStatic2) + " stack");

			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			impsSortedAntPost.get(0).getImagePlus().getProcessor().setMinAndMax(0, impsSortedAntPost.get(
					0).getImagePlus().getStatistics().max * ratio);
			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			impsSortedAntPost.get(1).getImagePlus().getProcessor().setMinAndMax(0, impsSortedAntPost.get(
					1).getImagePlus().getStatistics().max * ratio);

			selectedImages = impsSortedAntPost;
		}

		return selectedImages;
	}

	@Override
	public String instructions() {
		return "2 images Ant-Post or Post-Ant or dynamic Ant-Post";
	}

}
