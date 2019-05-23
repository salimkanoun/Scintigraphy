package org.petctviewer.scintigraphy.lympho.Test;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;

import ij.IJ;
import ij.ImagePlus;

public class TestScintyy extends Scintigraphy {

	public TestScintyy() {
		super("Test Scinti");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {

			impSorted = null;
			ImagePlus imp = openedImages[i].getImagePlus();
			if (openedImages[i].getImageOrientation() == Orientation.ANT_POST) {
				System.out.println("---------------------!!!---------------------");
				impSorted = imp.duplicate();
				impSorted.getStack().getProcessor(2).flipHorizontal();
			} else if (openedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = imp.duplicate();
				IJ.run(impSorted, "Reverse", "");
				impSorted.getStack().getProcessor(2).flipHorizontal();
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT");
			}

			impsSortedAntPost[i] = impSorted;
			openedImages[i].getImagePlus().close();
		}
		ImageSelection[] selection = new ImageSelection[impsSortedAntPost.length];
		for (int i = 0; i < impsSortedAntPost.length; i++) {
			selection[i] = new ImageSelection(impsSortedAntPost[i], null, null);
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		TestFenApp test = new TestFenApp(selectedImages[0], this.getStudyName());
		// this.getFenApplication()
		// .setControleur(new ControleurLympho(this, this.getFenApplication(), "Lympho
		// Scinti", selectedImages));
		// test.setControleur(new ControllerWorkflowLympho(this,
		// this.getFenApplication(), new ModelLympho(selectedImages, "Test Scinti")));
		test.setVisible(true);

	}

}
