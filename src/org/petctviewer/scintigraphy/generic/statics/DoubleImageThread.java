package org.petctviewer.scintigraphy.generic.statics;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

public class DoubleImageThread extends Thread{
	
	final Scintigraphy scin;
	private final ModelScin model;
	
	public DoubleImageThread(String name,Scintigraphy scin, ModelScin model) {
		super(name);
		this.scin = scin;
		this.model = model;
	}
	
	public void run() {
		int width = 512;
		double ratioCapture = this.scin.getFenApplication().getImagePlus().getWidth() * 1.0 / this.scin.getFenApplication().getImagePlus().getHeight();
		ImagePlus impRes = new ImagePlus();
		ImageStack capture = new ImageStack(width, (int) (width/ratioCapture));
		
		this.model.getImagePlus().setSlice(1);
		capture.addSlice(Library_Capture_CSV.captureImage(this.model.getImagePlus(), width, 0 ).getProcessor());
		capture.setSliceLabel("ant", 1);
		this.model.getImagePlus().setSlice(2);

		capture.addSlice(Library_Capture_CSV.captureImage(this.model.getImagePlus(), width, 0 ).getProcessor());
		capture.setSliceLabel("post", 2);
		
		impRes.setStack(capture);

		
		MontageMaker montageMaker = new MontageMaker();
		impRes = montageMaker.makeMontage2(impRes,
				2, //columns
				1, //rows 
				0.50, //scaleFactor 
				1, //first slice 
				2, //last slice 
				1, //increment
				3, //border width 
				//12, //font size
				true//label slices
				);
		
		//ouverture de la fenetre de resultat
		FenResults fenResults = new FenResultat_ScinStatic(impRes.getBufferedImage(),
				this.scin.getFenApplication().getController());
		fenResults.setVisible(true);
	}
	
	

}
