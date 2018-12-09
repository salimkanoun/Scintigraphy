package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class DoubleImageThread extends Thread{
	
	Scintigraphy scin;
	
	public DoubleImageThread(String name,Scintigraphy scin) {
		super(name);
		this.scin = scin;
	}
	
	public void run() {
		double ratioCapture = this.scin.getFenApplication().getImagePlus().getWidth()*1.0 / this.scin.getFenApplication().getImagePlus().getHeight()*1.0;
		ImagePlus impRes = new ImagePlus();
		ImageStack capture = new ImageStack(200, (int) (200/ratioCapture));
		
		capture.addSlice( Library_Capture_CSV.captureImage(this.scin.getImp(), 200, (int) (200/ratioCapture) ).getProcessor());
		capture.setSliceLabel("ant", 1);
		this.scin.getImp().setSlice(2);

		capture.addSlice( Library_Capture_CSV.captureImage(this.scin.getImp(), 200, (int) (200/ratioCapture) ).getProcessor());
		capture.setSliceLabel("post", 2);
		
		impRes.setStack(capture);

		
		MontageMaker montageMaker = new MontageMaker();
		impRes = montageMaker.makeMontage2(impRes,
				2, //columns
				1, //rows 
				1.00, //scaleFactor 
				1, //first slice 
				2, //last slice 
				1, //increment
				3, //border width 
				//12, //font size
				true//label slices
				);
		
		//ouverture de la fenetre de resultat
		FenResultat_ScinStatic fen = new FenResultat_ScinStatic(scin, impRes.getBufferedImage() );
		//ajout du tableau 
		fen.addAntTab(((ModeleScinStatic)scin.getModele()).calculerTableauAnt());
		fen.addPostTab(((ModeleScinStatic)scin.getModele()).calculerTableauPost());
		fen.addMoyGeomTab(((ModeleScinStatic)scin.getModele()).calculerTaleauMayGeom());
		fen.addCaptureButton();

		fen.pack();
		
	}
	
	

}
