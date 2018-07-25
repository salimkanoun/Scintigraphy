package org.petctviewer.scintigraphy.esophageus;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class EsophagealTransit extends Scintigraphy {

	private ImagePlus impAnt, impPost, impProjetee, impProjeteeAnt;
	private int[] frameDurations;

	
	public EsophagealTransit() {
		super("Eso");
	}


	@Override
	public void lancerProgramme() {
		
		FenApplication_EsophagealTransit fen = new FenApplication_EsophagealTransit(impAnt);
		this.setFenApplication(fen);
		
		Controleur_EsophagealTransit cet = new Controleur_EsophagealTransit(this);
		this.getFenApplication().setControleur(cet);
		this.getFenApplication().setVisible(true);
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		//entrée : tableau des toutes les images passées envoyé par la selecteur de dicom
		ImagePlus imTest = null;
		if(images != null && images.length>0) {
			
				ArrayList<ImagePlus> imagesAnt = new ArrayList<>();
				for(int i =0; i< images.length; i++) {
					if(Scintigraphy.isAnterieur(images[i]) && 
							(
									!Scintigraphy.isMultiFrame(images[i]) || 
									( 
											Scintigraphy.isMultiFrame(images[i]) && Scintigraphy.isSameCameraMultiFrame(images[i])
									) 
							) 
						) {
						
						
						imagesAnt.add(DynamicScintigraphy.projeter(images[i],0,impAnt.getStackSize(),"max"));
						System.out.println("i:"+i+" is ant  j : "+imagesAnt.size());
					}
				}
					
				 imTest = new ImagePlus("test",ModeleScin.captureToStack(imagesAnt.toArray(new ImagePlus[imagesAnt.size()])));
			
		}
		
		return imTest;
		
	}


	
	public int[] getFrameDurations() {
		return frameDurations;
	}

	

}
