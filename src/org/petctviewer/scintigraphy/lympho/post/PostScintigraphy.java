package org.petctviewer.scintigraphy.lympho.post;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import ij.IJ;
import ij.ImagePlus;

public class PostScintigraphy extends Scintigraphy {

	TabResult resultTab;

	public PostScintigraphy(String studyName, TabResult tab) {
		super("Post Scintigraphy");

		this.resultTab = tab;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[selectedImages.length];

		for (int i = 0; i < selectedImages.length; i++) {

			impSorted = null;
			ImagePlus imp = selectedImages[i].getImagePlus();
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST) {
				impSorted = imp.duplicate();
				impSorted.getStack().getProcessor(2).flipHorizontal();
			} else if (selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = imp.duplicate();
				IJ.run(impSorted, "Reverse", "");
				impSorted.getStack().getProcessor(2).flipHorizontal();
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT ");
			}
			int ratio = (int) (25000 / impSorted.getStatistics().max);
			System.out.println("Ratio : " + ratio);
			System.out.println("MAX : " + impSorted.getStatistics().max);
			impSorted.getProcessor().setMinAndMax(0, impSorted.getStatistics().max * (1.0d / ratio)); // On augmente le
																										// contraste
																										// (uniquement
																										// visuel,
																										// n'impacte pas
																										// les données)
			System.out.println("MAX : " + impSorted.getStatistics().max);

			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] selection = new ImageSelection[impsSortedAntPost.length];
		for (int i = 0; i < impsSortedAntPost.length; i++) {
			selection[i] = new ImageSelection(impsSortedAntPost[i], null, null);
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationPost(selectedImages[0].getImagePlus(), this.getStudyName()));
		this.getFenApplication().setControleur(
				new ControleurPost(this, this.getFenApplication(), "Lympho Scinti", selectedImages, this.resultTab));
		this.getFenApplication().setVisible(true);

	}

}
