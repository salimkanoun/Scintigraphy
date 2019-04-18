package org.petctviewer.scintigraphy.lympho;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;


public class LymphoSintigraphy extends Scintigraphy{

	public LymphoSintigraphy() {
		super("Lympho Scintigraphy");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws Exception {
		
		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[selectedImages.length];
		int DynamicPosition = -1;
		
		
		for (int i=0 ; i<selectedImages.length; i++) {																// Modifie l'ImagePlus pour mettre ANT en slice 1 et POST en slice 2
			impSorted = null;
			ImagePlus imp = selectedImages[i].getImagePlus();
			if(selectedImages[i].getImageOrientation()==Orientation.ANT_POST) {
				System.out.println("------------------------ Avant Analyse ------------------------");
				impSorted = Library_Dicom.sortImageAntPost(imp);
			}else if(selectedImages[i].getImageOrientation()==Orientation.POST_ANT){
				System.out.println("_______________________________ ??? _______________________________");
				impSorted = Library_Dicom.sortImageAntPost(imp);
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT_POST) {
				System.out.println("-_-_-_-_-_-_-_-_-_-_-_- Avant transformation -_-_-_-_-_-_-_-_-_-_-_-");
				impSorted=dynamicToStaticAntPost(imp);
				System.out.println("-_-_-_-_-_-_-_-_-_-_-_- Après transformation -_-_-_-_-_-_-_-_-_-_-_-");
				DynamicPosition = i;
			}else {
				throw new Exception("Unexpected Image type.\n Accepted : ANT/POST | POST/ANT | DYNAMIC_ANT_POST");
			}
			
			impsSortedAntPost[i]=impSorted;
			selectedImages[i].getImagePlus().close();
		}
		
		if(DynamicPosition != -1) {
			ImagePlus staticImage = impsSortedAntPost[Math.abs((DynamicPosition-1))];
			ImagePlus dynamicImage = impsSortedAntPost[DynamicPosition];
			int[] timeStatic = Library_Dicom.buildFrameDurations(staticImage);
			int[] timesDynamic = Library_Dicom.buildFrameDurations(dynamicImage);
			int acquisitionTimeDynamic = 0;
			for(int times : timesDynamic) {
				acquisitionTimeDynamic += times;
			}
			
			int ratio = timeStatic[0]/acquisitionTimeDynamic;
			dynamicImage.getProcessor().setMinAndMax(0,dynamicImage.getStatistics().max*ratio);
			impsSortedAntPost[0] = staticImage;
			impsSortedAntPost[1] = dynamicImage;
			System.out.println("show 1");
			impsSortedAntPost[0].show();
			System.out.println("show 2");
			impsSortedAntPost[1].show();
			System.out.println("show 3");
		}
		
		ImagePlus[] impsSortedByTime=Library_Dicom.orderImagesByAcquisitionTime(impsSortedAntPost);
		
		
        ImageSelection[] selection = new ImageSelection[impsSortedByTime.length];
        for (int i = 0 ; i < impsSortedByTime.length ; i ++ ) {
        	selection[i]= new ImageSelection(impsSortedByTime[i],null, null);
        }
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		
		this.setFenApplication(new FenApplicationLympho(selectedImages[0].getImagePlus(), this.getExamType()));
		this.getFenApplication().setControleur(new ControleurLympho(this, this.getFenApplication(), "Lympho Scinti", selectedImages));
		this.getFenApplication().setVisible(true);
		
	}
	
	
	
	/**
	 * 
	 * This method return the projection of a Dynamic {@link ImagePlus} to a Static {@link ImagePlus}, using the avg.<br/>
	 * 
	 * @param imp
	 *             : Dynamic ImagePlus you want to transform
	 * @return
	 *              The static {@link ImagePlus}
	 *              
	 *@see
	 *          <ul>       
	 * 				<li>{@link Library_Dicom#sortDynamicAntPost(ImagePlus)}</li>
	 *				<li> {@link Library_Dicom#projeter(ImagePlus, int, int, String)}</li>
	 * 			</ul>    
	 */
	public ImagePlus dynamicToStaticAntPost(ImagePlus imp) {
		ImagePlus[] Ant_Post = Library_Dicom.sortDynamicAntPost(imp);
		ImagePlus Ant = Library_Dicom.projeter(Ant_Post[0],0,Ant_Post[0].getStackSize(),"avg");
		ImagePlus Post = Library_Dicom.projeter(Ant_Post[1],0,Ant_Post[1].getStackSize(),"avg");
		ImagePlus ImageRetour = Ant;
		ImageRetour.getStack().addSlice(Post.getProcessor());
		return ImageRetour;
	}

}
