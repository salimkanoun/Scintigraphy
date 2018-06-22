package org.petctviewer.scintigraphy.statics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.util.DicomTools;

public class ModeleScinStatic  extends ModeleScin{
	
	private HashMap<String, List<Double>> rois;
	private HashMap<String, Integer> roisPixel;
	
	public ModeleScinStatic() {
		this.rois = new HashMap<>();
		this.roisPixel = new HashMap<>();
	}
	
	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		this.setImp(imp);
		//System.out.println("nom de la roi :" + nomRoi.substring(0, nomRoi.lastIndexOf(" ")));
		System.out.println(nomRoi + " : " + getCounts(imp));
		
		// on garde uniquement le nom de la roi sans le tag
		nomRoi = nomRoi.substring(0,nomRoi.lastIndexOf(" "));
		// si la roi n'existe pas, on la crée 
		if(this.rois.get(nomRoi)==null) {
			this.rois.put(nomRoi, new ArrayList<Double>());
		}
		//on ajoute le nombre de coups
		this.rois.get(nomRoi).add(ModeleScin.getCounts(imp));
		
		// pour les pixel des rois
		this.roisPixel.put(nomRoi, imp.getRoi().getStatistics().pixelCount);
	}

	
	@Override
	public void calculerResultats() {
		
	
	}
	
	public Object[][] calculerTableau(int nbSlice) {
		Object[][] res = null;
		if(nbSlice == 2) {
			res= new Object[this.rois.size()][4];
			int i =0;
			for(String s: this.rois.keySet()) {
				res[i][0] = s;//name
				res[i][1] = ModeleScin.round(ModeleScin.moyGeom(this.rois.get(s).get(1), this.rois.get(s).get(2)), 0);
				
				String[] resolution1PixelEnMm = DicomTools.getTag(this.getImp(), "0028,0030").trim().split("\\\\");
				//hauteur pixel en mm * largeur pixel en mm * nb pixel dans la roi
				Double aire = (Double.parseDouble(resolution1PixelEnMm[0])*Double.parseDouble(resolution1PixelEnMm[1]))*this.roisPixel.get(s);
				//moyenne
				res[i][2] = ModeleScin.round(ModeleScin.moyGeom(
						this.rois.get(s).get(1)/aire
						, this.rois.get(s).get(2)/aire),2);

				//ecart
				// a revoir
				res[i][3] = ModeleScin.round(Math.sqrt(Math.pow(this.rois.get(s).get(1),2) + Math.pow(this.rois.get(s).get(2),2)),0);

				i++;
			}
		}else {
			if(nbSlice == 1) {
				res= new Object[this.rois.size()][3];
				int i =0;
				for(String s: this.rois.keySet()) {
					res[i][0] = s;//name
					res[i][1] = ModeleScin.round(this.rois.get(s).get(1),0);
					
					String[] resolution1PixelEnMm = DicomTools.getTag(this.getImp(), "0028,0030").trim().split("\\\\");
					//hauteur pixel en mm * largeur pixel en mm * nb pixel dans la roi
					Double aire = (Double.parseDouble(resolution1PixelEnMm[0])*Double.parseDouble(resolution1PixelEnMm[1]))*this.roisPixel.get(s);
					//moyenne
					res[i][2] = ModeleScin.round(
							this.rois.get(s).get(1)/aire,2);

					i++;
				}
			}
		}
		
		return res;
	}
	
	//pour quand il y quune post ou ant pareil sans ecart type et avg = cout/aire
	
	public void calculerMoyenne() {
		
	}
	
	@Override
	public String toString() {
		return null;
	}

}
