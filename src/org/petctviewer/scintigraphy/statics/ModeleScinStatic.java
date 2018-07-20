package org.petctviewer.scintigraphy.statics;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class ModeleScinStatic  extends ModeleScin{
	
	private HashMap<String, Object[]> roisAnt;
	private HashMap<String, Object[]> roisPost;
	
	public ModeleScinStatic() {
		this.roisAnt = new HashMap<>();
		this.roisPost = new HashMap<>();

	}
	
	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {

	}

	public void enregistrerMesureAnt(String nomRoi, ImagePlus imp) {
			// on garde uniquement le nom de la roi sans le tag
			nomRoi = nomRoi.substring(0,nomRoi.lastIndexOf(" "));
			// si la roi n'existe pas, on la crée 
			if(this.roisAnt.get(nomRoi)==null) {
				Object[] o = { 	ModeleScin.round(ModeleScin.getCounts(imp),3),
						ModeleScin.round(imp.getRoi().getStatistics().mean,3),
						ModeleScin.round(imp.getRoi().getStatistics().stdDev,3)};
				this.roisAnt.put(nomRoi,o);
			}
			
	}
	
	public void enregistrerMesurePost(String nomRoi, ImagePlus imp) {
		// on garde uniquement le nom de la roi sans le tag
		nomRoi = nomRoi.substring(0,nomRoi.lastIndexOf(" "));
		// si la roi n'existe pas, on la crée 
		if(this.roisPost.get(nomRoi)==null) {
			Object[] o = { 	ModeleScin.round(ModeleScin.getCounts(imp),3),
							ModeleScin.round(imp.getRoi().getStatistics().mean,3),
							ModeleScin.round(imp.getRoi().getStatistics().stdDev,3)};
			this.roisPost.put(nomRoi,o);
		}
		
}
	
	@Override
	public void calculerResultats() {
		
	
	}
	
	public Object[][] calculerTableauAnt(){
		Object[][] res = new Object[this.roisAnt.size()][4];

			int i =0;
			for(String s: this.roisAnt.keySet()) {
				res[i][0] = s;
				res[i][1] = this.roisAnt.get(s)[0];
				res[i][2] = this.roisAnt.get(s)[1];
				res[i][3] = this.roisAnt.get(s)[2];
				i++;
			}
			return res;

	}
	
	public Object[][] calculerTableauPost(){
		Object[][] res = new Object[this.roisPost.size()][4];

		int i =0;
		for(String s: this.roisPost.keySet()) {
			res[i][0] = s;
			res[i][1] = this.roisPost.get(s)[0];
			res[i][2] = this.roisPost.get(s)[1];
			res[i][3] = this.roisPost.get(s)[2];
			i++;
		}
		return res;
	}	
	
	public Object[][] calculerTaleauMayGeom(){
		
		Object[][] res = new Object[this.roisPost.size()][4];

		int i =0;
		for(String s: this.roisPost.keySet()) {
			res[i][0] = s;
			res[i][1] = ModeleScin.round(ModeleScin.moyGeom((Double)this.roisAnt.get(s)[0],(Double)this.roisPost.get(s)[0] ),3);
			i++;
		}
		return res;
	}
	
	@Override
	public String toString() {
		String res = "name, count ant, avg ant , std ant, count post, avg post, std post, geom mean \n";
				for(String s: this.roisAnt.keySet()) {
					res+= s+", "+roisAnt.get(s)[0]+","+roisAnt.get(s)[1]+","+roisAnt.get(s)[2]+","
							+roisPost.get(s)[0]+","+roisPost.get(s)[1]+","+roisPost.get(s)[2]+","
							+ModeleScin.round(ModeleScin.moyGeom((Double)this.roisAnt.get(s)[0],(Double)this.roisPost.get(s)[0] ),3)
							+"\n";
				}
				//round 
				//taille fenetre
				
				
		return res;//csv
	}

}
