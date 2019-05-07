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

		ImageSelection impSorted = null;
		ImageSelection[] impsSortedAntPost = new ImageSelection[selectedImages.length];

		for (int i = 0; i < selectedImages.length; i++) {

			impSorted = null;
			ImageSelection imp = selectedImages[i];
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST) {
				impSorted = imp.clone();
				impSorted.getImagePlus().getStack().getProcessor(2).flipHorizontal();
			} else if (selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = imp.clone();
				IJ.run(impSorted.getImagePlus(), "Reverse", "");
				impSorted.getImagePlus().getStack().getProcessor(2).flipHorizontal();
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT ");
			}
			int ratio = (int) (25000 / impSorted.getImagePlus().getStatistics().max);
			System.out.println("Ratio : " + ratio);
			System.out.println("MAX : " + impSorted.getImagePlus().getStatistics().max);
			// On augmente le contraste(uniquement visuel, n'impacte pas les données)
			impSorted.getImagePlus().getProcessor().setMinAndMax(0, impSorted.getImagePlus().getStatistics().max * (1.0d / ratio)); 
			System.out.println("MAX : " + impSorted.getImagePlus().getStatistics().max);

			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] selection = new ImageSelection[impsSortedAntPost.length];
		for (int i = 0; i < impsSortedAntPost.length; i++) {
			selection[i] = impsSortedAntPost[i].clone();
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationPost(selectedImages[0].getImagePlus(), this.getStudyName()));
//		this.getFenApplication().setControleur(
//				new ControleurPost(this, this.getFenApplication(), "Lympho Scinti", selectedImages, this.resultTab));
		this.getFenApplication().setControleur(
				new ControllerWorkflowPelvis(this, this.getFenApplication(), new ModelePost(selectedImages, "Pelvis Scinty", this.resultTab), this.resultTab));
		this.getFenApplication().setVisible(true);

	}

}
