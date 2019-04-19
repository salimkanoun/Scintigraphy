package org.petctviewer.scintigraphy.lympho;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.shunpo.ModeleShunpo;

import ij.ImagePlus;

public class ModeleLympho extends ModeleScin{

	private boolean locked;
	
	private Map<Integer, Double> coups;

	public ModeleLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		
		this.coups = new HashMap<>();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculerResultats() {
		// TODO Auto-generated method stub
		
	}
	
	
	public boolean isLocked() {
		return locked;
	}
	
	/************** Getter *************/

	/************** Setter *************/	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String[] getResult() {
		// Permet de definir le nombre de chiffre apres la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		
		
		
		
		
		
		return new String[] {"Test","OK"};
	}

	protected void calculerCoups(int organ, ImagePlus imp) {
		double counts = Library_Quantif.getCounts(imp);
		this.coups.put(organ, counts);
		System.out.println("Calculations for " + organ + "[" + ModeleShunpo.convertOrgan(organ) + "] -> " + counts);
	}

}
