package org.petctviewer.scintigraphy.gastric;

import org.petctviewer.scintigraphy.esophageus.Condense_Dynamique;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

import ij.ImagePlus;
import ij.WindowManager;

public class FenetreSelection extends FenSelectionDicom {
	
	Vue_VG_Roi vue;
	Vue_VG_Dynamique vue2;
	Condense_Dynamique vue3;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FenetreSelection(Vue_VG_Roi vue) {
		super("", null); 
		this.vue=vue;
	}
	
	public FenetreSelection(Vue_VG_Dynamique vue) {
		super("", null); 
		this.vue2=vue;
	}
	
	public FenetreSelection(Condense_Dynamique vue) {
		super("", null); 
		this.vue3=vue;
	}

	
	@Override
	public void startExam() {
		int[] rows = this.table.getSelectedRows();
		ImagePlus[] images = new ImagePlus[rows.length];
		
		for (int i = 0; i < rows.length; i++) {;
			images[i] = WindowManager.getImage(id[rows[i]]);
		}
		
		try {

			ImagePlus.removeImageListener(this);
			
		} catch (Exception e) {
			System.err.println("The selected DICOM are not fit for this exam");
			e.printStackTrace();
		}
		
			if (vue!=null) vue.ouvertureImage(images);
			if (vue2!=null) vue2.ouvertureImage(images);
			if (vue3!=null) vue3.ouvertureImage(images);

			this.dispose();
		
	}
	

}
