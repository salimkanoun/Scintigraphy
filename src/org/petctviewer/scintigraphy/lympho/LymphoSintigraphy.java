package org.petctviewer.scintigraphy.lympho;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;
import ij.ImageStack;
import ij.util.DicomTools;

public class LymphoSintigraphy extends Scintigraphy {

	public LymphoSintigraphy() {
		super("Lympho Scintigraphy");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		
		selectedImages = Library_Dicom.orderImagesByAcquisitionTime(selectedImages);
		
		System.out.println("----------------------------------------------------");
		System.out.println(DicomTools.getTag(selectedImages[0].getImagePlus(), "0008,0032"));
		System.out.println(DicomTools.getTag(selectedImages[0].getImagePlus(), "0008,0022"));
		System.out.println(DicomTools.getTag(selectedImages[0].getImagePlus(), "0008,0008"));
		System.out.println();
		System.out.println(DicomTools.getTag(selectedImages[1].getImagePlus(), "0008,0032"));
		System.out.println(DicomTools.getTag(selectedImages[1].getImagePlus(), "0008,0022"));
		System.out.println(DicomTools.getTag(selectedImages[1].getImagePlus(), "0008,0008"));
		
		System.out.println("----------------------------------------------------");
		
		
		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[selectedImages.length];
		int DynamicPosition = -1;

		for (int i = 0; i < selectedImages.length; i++) { 
			impSorted = null;
			ImagePlus imp = selectedImages[i].getImagePlus();
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
				impSorted.getStack().getProcessor(2).flipHorizontal();
			} else if (selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				System.out.println("_______________________________ ??? _______________________________");
				impSorted = Library_Dicom.sortImageAntPost(imp);
			} else if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				impSorted = imp.duplicate();
				DynamicPosition = i;
			} else {
				throw new WrongInputException("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT | DYNAMIC_ANT_POST");
			}

			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}
		
		ImagePlus[] impsSortedByTime = new 	ImagePlus[impsSortedAntPost.length];
		System.out.println("DynamicPosition : "+DynamicPosition);
		if(DynamicPosition != -1)  {
			ImagePlus staticImage = impsSortedAntPost[Math.abs((DynamicPosition - 1))];
			ImagePlus dynamicImage = impsSortedAntPost[DynamicPosition];
			int[] timeStatic = Library_Dicom.buildFrameDurations(staticImage);
			int[] timesDynamic = Library_Dicom.buildFrameDurations(dynamicImage);
			int acquisitionTimeDynamic = 0;
			for (int times : timesDynamic) {
				acquisitionTimeDynamic += times;
			}
			
			impsSortedByTime = new 	ImagePlus[impsSortedAntPost.length];
			dynamicImage = dynamicToStaticAntPost(dynamicImage);
			int ratio = timeStatic[0] / acquisitionTimeDynamic;
			
			System.out.println("timeStatic[0] : "+timeStatic[0]);
			System.out.println("acquisitionTimeDynamic : "+acquisitionTimeDynamic);
			System.out.println("ratio : "+ratio);

			dynamicImage.getProcessor().setMinAndMax(0, dynamicImage.getStatistics().max * ratio);
			impsSortedByTime[Math.abs((DynamicPosition - 1))] = staticImage;
			impsSortedByTime[DynamicPosition ] = dynamicImage;

			
		}else {
			impsSortedByTime = impsSortedAntPost;
		}

		// ImagePlus[] impsSortedByTime = Library_Dicom.orderImagesByAcquisitionTime(impsSortedAntPost);

		ImageSelection[] selection = new ImageSelection[impsSortedByTime.length];
		for (int i = 0; i < impsSortedByTime.length; i++) {
			selection[i] = new ImageSelection(impsSortedByTime[i], null, null);
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		this.setFenApplication(new FenApplicationLympho(selectedImages[0].getImagePlus(), this.getStudyName()));
		this.getFenApplication()
				.setControleur(new ControleurLympho(this, this.getFenApplication(), "Lympho Scinti", selectedImages));
		this.getFenApplication().setVisible(true);

	}

	/**
	 * 
	 * This method return the projection of a Dynamic {@link ImagePlus} to a Static
	 * {@link ImagePlus}, using the avg.<br/>
	 * 
	 * @param imp
	 *            : Dynamic ImagePlus you want to transform
	 * @return The static {@link ImagePlus}
	 * 
	 * @see
	 *      <ul>
	 *      <li>{@link Library_Dicom#sortDynamicAntPost(ImagePlus)}</li>
	 *      <li>{@link Library_Dicom#projeter(ImagePlus, int, int, String)}</li>
	 *      </ul>
	 */
	public ImagePlus dynamicToStaticAntPost(ImagePlus imp) {
		ImagePlus[] Ant_Post = Library_Dicom.sortDynamicAntPost(imp);
		
//		Ant_Post[0].show();
		/*
		Ant_Post[1].show();
		*/
		ImagePlus Ant = Library_Dicom.projeter(Ant_Post[0], 0, Ant_Post[0].getStackSize(), "avg");
		ImagePlus Post = Library_Dicom.projeter(Ant_Post[1], 0, Ant_Post[1].getStackSize(), "avg");

//		Ant.show();

//		Post.show();

		
		ImageStack img = new ImageStack(Ant.getWidth(), Ant.getHeight());
		img.addSlice(Ant.getProcessor());
		img.addSlice(Post.getProcessor());
		ImagePlus ImageRetour = new ImagePlus();
		ImageRetour.setStack(img);
		
		ImageRetour.getStack().getProcessor(1).flipHorizontal();
		
		// ImageRetour.show();
		return ImageRetour;
	}

}
