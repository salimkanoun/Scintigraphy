package org.petctviewer.scintigraphy.lympho;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.*;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.Arrays;

public class LymphoSintigraphy extends Scintigraphy {

	public LymphoSintigraphy() {
		super("Lympho Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException, ReadTagException {
		// Check number of images
		if (selectedImages.length != 2)
			throw new WrongNumberImagesException(selectedImages.length, 2);

		Arrays.parallelSort(selectedImages, new ChronologicalAcquisitionComparator());

		ImageSelection impSorted = null;
		ImageSelection[] impsSortedAntPost = new ImageSelection[selectedImages.length];
		int DynamicPosition = -1;

		for (int i = 0; i < selectedImages.length; i++) {

			impSorted = null;
			ImageSelection imp = selectedImages[i];
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST
					|| selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				impSorted = imp.clone();
				DynamicPosition = i;
			} else {
				throw new WrongColumnException.OrientationColumn(selectedImages[i].getRow(),
						selectedImages[i].getImageOrientation(),
						new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.DYNAMIC_ANT_POST});
			}

			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] impsCorrectedByTime = new ImageSelection[impsSortedAntPost.length];
		if (DynamicPosition != -1) {
			ImageSelection staticImage = impsSortedAntPost[Math.abs((DynamicPosition - 1))];
			ImageSelection dynamicImage = impsSortedAntPost[DynamicPosition];
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
			IJ.run(dynamicImage.getImagePlus(), "Multiply...", "value=" + (60000f / acquisitionTimeDynamic) + " " +
					"stack");

			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			dynamicImage.getImagePlus().getProcessor().setMinAndMax(0,
					dynamicImage.getImagePlus().getStatistics().max * ratio);
			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			staticImage.getImagePlus().getProcessor().setMinAndMax(0,
					staticImage.getImagePlus().getStatistics().max * ratio);

			impsCorrectedByTime[Math.abs((DynamicPosition - 1))] = staticImage;
			selectedImages[DynamicPosition] = dynamicImage;

		} else {
			int timeStatic1 = Library_Dicom.getFrameDuration(impsSortedAntPost[0].getImagePlus());
			int timeStatic2 = Library_Dicom.getFrameDuration(impsSortedAntPost[1].getImagePlus());
			double ratio = (timeStatic1 * 1.0D / timeStatic2 * 1.0D);

			// On passe les deux static sur le même temps théorique
			IJ.run(impsSortedAntPost[0].getImagePlus(), "Multiply...", "value=" + (1f / ratio) + " stack");
			// On ramène sur 1 minute
			IJ.run(impsSortedAntPost[0].getImagePlus(), "Multiply...", "value=" + (60000f / timeStatic2) + " stack");
			// On ramène sur 1 minute
			IJ.run(impsSortedAntPost[1].getImagePlus(), "Multiply...", "value=" + (60000f / timeStatic2) + " stack");

			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			impsSortedAntPost[0].getImagePlus().getProcessor().setMinAndMax(0,
					impsSortedAntPost[0].getImagePlus().getStatistics().max * ratio);
			// On augmente le contraste (uniquement visuel, n'impacte pas les données)
			impsSortedAntPost[1].getImagePlus().getProcessor().setMinAndMax(0,
					impsSortedAntPost[1].getImagePlus().getStatistics().max * ratio);

			selectedImages = impsSortedAntPost;
		}

		return selectedImages;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationLympho(selectedImages[0], this.getStudyName()));
		// this.getFenApplication()
		// .setControleur(new ControleurLympho(this, this.getFenApplication(), "Lympho
		// Scinti", selectedImages));
		((FenApplicationWorkflow) this.getFenApplication()).setControleur(new ControllerWorkflowLympho(this,
				(FenApplicationWorkflow) this.getFenApplication(), new ModelLympho(selectedImages, "Lympho Scinti")));
		this.getFenApplication().setVisible(true);

	}

	/**
	 * This method return the projection of a Dynamic {@link ImagePlus} to a Static
	 * {@link ImagePlus}, using the avg.<br/>
	 *
	 * @param imp : Dynamic ImagePlus you want to transform
	 * @return The static {@link ImagePlus}
	 * @throws IllegalArgumentException
	 * @throws WrongOrientationException
	 * @see Library_Dicom#splitDynamicAntPost(ImageSelection)
	 * @see Library_Dicom#project(ImageSelection, int, int, String)
	 */
	public ImageSelection dynamicToStaticAntPost(ImageSelection imp)
			throws WrongOrientationException, IllegalArgumentException, ReadTagException {
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

}
