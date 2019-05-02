package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class DoubleImageThread extends Thread{
	
	Scintigraphy scin;
	private ModeleScin model;
	
	public DoubleImageThread(String name,Scintigraphy scin, ModeleScin model) {
		super(name);
		this.scin = scin;
		this.model = model;
	}
	
	public void run() {
		int width = 512;
		double ratioCapture = this.scin.getFenApplication().getImagePlus().getWidth()*1.0 / this.scin.getFenApplication().getImagePlus().getHeight()*1.0;
		ImagePlus impRes = new ImagePlus();
		ImageStack capture = new ImageStack(width, (int) (width/ratioCapture));
		
		this.model.getImagePlus().setSlice(1);
		capture.addSlice( Library_Capture_CSV.captureImage(this.model.getImagePlus(), width, 0 ).getProcessor());
		capture.setSliceLabel("ant", 1);
		this.model.getImagePlus().setSlice(2);

		capture.addSlice( Library_Capture_CSV.captureImage(this.model.getImagePlus(), width, 0 ).getProcessor());
		capture.setSliceLabel("post", 2);
		
		impRes.setStack(capture);

		
		MontageMaker montageMaker = new MontageMaker();
		impRes = montageMaker.makeMontage2(impRes,
				1, //columns
				2, //rows 
				0.50, //scaleFactor 
				1, //first slice 
				2, //last slice 
				1, //increment
				3, //border width 
				//12, //font size
				true//label slices
				);
		
		//ouverture de la fenetre de resultat
		new FenResultat_ScinStatic(impRes.getBufferedImage(), this.model);
		
	}
	
	

}
