package org.petctviewer.scintigraphy.statics;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.StaticMethod;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class DoubleImageThread extends Thread{
	
	Scintigraphy scin;
	ModeleScinStatic modele;
	
	public DoubleImageThread(String name,Scintigraphy scin, ModeleScinStatic modele) {
		super(name);
		this.scin = scin;
		this.modele = modele;
	}
	
	public void run() {
		double ratioCapture = this.scin.getFenApplication().getImagePlus().getWidth()*1.0 / this.scin.getFenApplication().getImagePlus().getHeight()*1.0;
		ImagePlus impRes = new ImagePlus();
		ImageStack capture = new ImageStack(200, (int) (200/ratioCapture));
		System.out.println((int) (300/ratioCapture));
		
		capture.addSlice( StaticMethod.captureImage(this.scin.getImp(), 200, (int) (200/ratioCapture) ).getProcessor());
		capture.setSliceLabel("ant", 1);
		this.scin.getImp().setSlice(2);

		capture.addSlice( StaticMethod.captureImage(this.scin.getImp(), 200, (int) (200/ratioCapture) ).getProcessor());
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
		FenResultat_ScinStatic fen = new FenResultat_ScinStatic( 
						this.scin, 
						
						impRes.getBufferedImage(), 

						(ModeleScinStatic) this.modele );
		//ajout du tableau 
		fen.addAntTab(((ModeleScinStatic)this.modele).calculerTableauAnt());
		fen.addPostTab(((ModeleScinStatic)this.modele).calculerTableauPost());
		fen.addMoyGeomTab(((ModeleScinStatic)this.modele).calculerTaleauMayGeom());
		fen.addCaptureButton();

		fen.pack();
		
		
		
		
	}
	
	

}
