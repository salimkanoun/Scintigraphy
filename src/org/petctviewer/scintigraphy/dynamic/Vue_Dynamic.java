package org.petctviewer.scintigraphy.dynamic;

import java.util.ArrayList;
import java.util.List;
import org.petctviewer.scintigraphy.scin.VueScin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.ZProjector;
import ij.util.DicomTools;

public abstract class Vue_Dynamic extends VueScin {

	private ImagePlus impProjetee, impAnt, impPost;

	private int[] frameDurations;

	public Vue_Dynamic() {
		super("Dynamic scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if (titresFenetres.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		if (titresFenetres.length == 1) { // si il y a qu'un fenetre d'ouverte
			ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
			if (VueScin.isMultiFrame(imp)) { // si l'image est multiframe
				ImagePlus[] imps = VueScin.splitCameraMultiFrame(imp);
				this.impAnt = (ImagePlus) imps[0].clone();
				this.impPost = (ImagePlus) imps[1].clone();
			} else if (VueScin.isAnterieur(imp)) {
				this.impAnt = imp;
			} else {
				IJ.log("Please open the Ant view");
			}

		} else { // si il y a deux fenetres d'ouvertes
			for (String s : titresFenetres) { // pour chaque fenetre
				ImagePlus imp = WindowManager.getImage(s);
				if (VueScin.isAnterieur(imp)) { // si la vue est ant, on choisi cette image
					this.impAnt = (ImagePlus) imp.clone();
				} else {
					this.impPost = (ImagePlus) imp.clone();
				}
			}
		}
		
		for(int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}

		this.impProjetee = ZProjector.run(this.impAnt, "sum");
		this.impProjetee.setProperty("Info", this.impAnt.getInfoProperty());

		this.frameDurations = buildFrameDurations(this.impAnt);		

		this.setImp(this.impProjetee);
		VueScin.setCustomLut(this.getImp());		
	}

	private int[] buildFrameDurations(ImagePlus imp) {
		int[] frameDurations = new int[imp.getStackSize()];
		
		int nbPhase = Integer.parseInt(DicomTools.getTag(imp, "0054,0031").trim());
		if (nbPhase == 1) {
			int duration = Integer.parseInt(DicomTools.getTag(imp, "0018,1242").trim());
			for (int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = duration;
			}
		} else {
			String[] phasesStr = DicomTools.getTag(imp, "0054,0030").trim().split(" ");
			int[] phases = new int[phasesStr.length];
			Integer[] durations = this.getDurations(imp);
			for(int i = 0; i < phases.length; i++) {
				phases[i] = Integer.parseInt(phasesStr[i]);
			}
			
			for(int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = durations[phases[i] - 1];
			}
		}
		
		return frameDurations;
	}

	private Integer[] getDurations(ImagePlus imp) {
		List<Integer> duration = new ArrayList<Integer>();
		String info = imp.getInfoProperty();
		String[] split = info.split("\n");
		for(String s : split) {
			if(s.startsWith("0018,1242")) {
				String[] mots = s.split(" ");
				duration.add(Integer.parseInt(mots[mots.length - 1]));
			}
		}
		return duration.toArray(new Integer[0]);
	}

	public ImagePlus getImpAnt() {
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}
	
	public ImagePlus getImpProjetee() {
		return impProjetee;
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

}
