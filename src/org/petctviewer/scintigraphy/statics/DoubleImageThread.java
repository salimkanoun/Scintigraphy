package org.petctviewer.scintigraphy.statics;

import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

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
		
		ImagePlus impRes = new ImagePlus();
		ImageStack capture = new ImageStack(300,300);
		
		capture.addSlice( ModeleScinStatic.captureImage(this.scin.getImp(), 300, 300).getProcessor());
		capture.setSliceLabel("ant", 1);
		this.scin.getImp().setSlice(2);

		capture.addSlice( ModeleScinStatic.captureImage(this.scin.getImp(), 300, 300).getProcessor());
		capture.setSliceLabel("post", 2);
		

		System.out.println(SwingUtilities.isEventDispatchThread()	);

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
