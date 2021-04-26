package org.petctviewer.scintigraphy.generic.statics;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;

public class ModelScinStatic extends ModelScin {

	private boolean isSingleSlice;
	private boolean isAnt;

	private HashMap<String, Object[]> roisAnt;
	private HashMap<String, Object[]> roisPost;

	public ModelScinStatic(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		this.roisAnt = new HashMap<>();
		this.roisPost = new HashMap<>();

	}

	public void enregistrerMesureAnt(String nomRoi, ImagePlus imp) {
		// on garde uniquement le studyName de la roi sans le tag
		// nomRoi = nomRoi.substring(0,nomRoi.lastIndexOf(" "));
		// si la roi n'existe pas, on la crée

		Object[] o = { Library_Quantif.round(Library_Quantif.getCounts(imp), 2),
				Library_Quantif.round(imp.getRoi().getStatistics().mean, 2),
				Library_Quantif.round(imp.getRoi().getStatistics().stdDev, 2) };
		if (this.roisAnt.get(nomRoi) == null)
			this.roisAnt.put(nomRoi, o);
		else
			this.roisAnt.replace(nomRoi, o);

	}

	public void enregistrerMesurePost(String nomRoi, ImagePlus imp) {
		// on garde uniquement le studyName de la roi sans le tag
		// nomRoi = nomRoi.substring(0,nomRoi.lastIndexOf(" "));
		// si la roi n'existe pas, on la crée

		Object[] o = { Library_Quantif.round(Library_Quantif.getCounts(imp), 3),
				Library_Quantif.round(imp.getRoi().getStatistics().mean, 3),
				Library_Quantif.round(imp.getRoi().getStatistics().stdDev, 3) };
		if (this.roisPost.get(nomRoi) == null)
			this.roisPost.put(nomRoi, o);
		else
			this.roisPost.replace(nomRoi, o);

	}

	@Override
	public void calculateResults() {

	}

	public Object[][] calculerTableauAnt() {
		Object[][] res = new Object[this.roisAnt.size()][4];

		int i = 0;
		for (String s : this.roisAnt.keySet()) {
			res[i][0] = s;
			res[i][1] = this.roisAnt.get(s)[0];
			res[i][2] = this.roisAnt.get(s)[1];
			res[i][3] = this.roisAnt.get(s)[2];
			i++;
		}
		return res;

	}

	public Object[][] calculerTableauPost() {
		Object[][] res = new Object[this.roisPost.size()][4];

		int i = 0;
		for (String s : this.roisPost.keySet()) {
			res[i][0] = s;
			res[i][1] = this.roisPost.get(s)[0];
			res[i][2] = this.roisPost.get(s)[1];
			res[i][3] = this.roisPost.get(s)[2];
			i++;
		}
		return res;
	}

	public Object[][] calculerTaleauMoyGeom() {

		Object[][] res = new Object[this.roisPost.size()][2];

		// Multiple Slice (ANT/POST)
		if (!this.isSingleSlice()) {
			int i = 0;
			for (String s : this.roisPost.keySet()) {
				res[i][0] = s;
				res[i][1] = Library_Quantif.round(
						Library_Quantif.moyGeom((Double) this.roisAnt.get(s)[0], (Double) this.roisPost.get(s)[0]), 3);
				i++;
			}
		}
		// Only ANT
		else if (this.isAnt()) {
			int i = 0;
			for (String s : this.roisPost.keySet()) {
				res[i][0] = s;
				res[i][1] = Library_Quantif.round((Double) this.roisAnt.get(s)[0], 3);
				i++;
			}
		}
		// Only POST
		else {
			int i = 0;
			for (String s : this.roisPost.keySet()) {
				res[i][0] = s;
				res[i][1] = Library_Quantif.round((Double) this.roisPost.get(s)[0], 3);
				i++;
			}
		}

		return res;
	}

	@Override
	public String toString() {
		String res;
		// Multiple Slice (ANT/POST)
		if (!this.isSingleSlice()) {
			res = "name, count ant, avg ant , std ant, count post, avg post, std post, geom mean \n";
			for (String s : this.roisAnt.keySet()) {
				res += s + ", " + Library_Quantif.round((Double) roisAnt.get(s)[0], 2)
						+ "," + Library_Quantif.round((Double) roisAnt.get(s)[1], 2)
						+ "," + Library_Quantif.round((Double) roisAnt.get(s)[2], 2)
						+ "," + Library_Quantif.round((Double) roisPost.get(s)[0], 2)
						+ "," + Library_Quantif.round((Double) roisPost.get(s)[1], 2)
						+ "," + Library_Quantif.round((Double) roisPost.get(s)[2], 2)
						+ "," + Library_Quantif.round(Library_Quantif.moyGeom((Double) this.roisAnt.get(s)[0],
								(Double) this.roisPost.get(s)[0]), 2)
						+ "\n";
			}
			// round
			// taille fenetre
		}
		// Only ANT
		else if (this.isAnt()) {
			res = "name, count ant, avg ant , std ant,\n";
			for (String s : this.roisAnt.keySet()) {
				res += s + ", " + Library_Quantif.round((Double) roisAnt.get(s)[0], 2)
						+ "," + Library_Quantif.round((Double) roisAnt.get(s)[1], 2)
						+ "," + Library_Quantif.round((Double) roisAnt.get(s)[2], 2) + "\n";
			}
		}
		// Only POST
		else {
			res = "name, count post, avg post, std post\n";
			for (String s : this.roisAnt.keySet()) {
				res += s + ", " + Library_Quantif.round((Double) roisPost.get(s)[0], 2)
						+ "," + Library_Quantif.round((Double) roisPost.get(s)[1], 2)
						+ "," + Library_Quantif.round((Double) roisPost.get(s)[2], 2) + "\n";
			}
		}

		return res; // csv format
	}

	public void setIsSingleSlide(boolean isSingleSlice) {
		this.isSingleSlice = isSingleSlice;
	}

	public void setIsAnt(boolean isAnt) {
		this.isAnt = isAnt;
	}

	public boolean isSingleSlice() {
		return isSingleSlice;
	}

	public boolean isAnt() {
		return isAnt;
	}

}
