package org.petctviewer.scintigraphy.generic.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.ImagePlus;
import ij.gui.Roi;

public class Model_GeneralDyn extends ModelScinDyn {

	private final ImageSelection impAnt;
	private final ImageSelection impPost;
	private final ImageSelection impProjetee;
	private final ImageSelection impProjeteeAnt;
	private final ImageSelection impProjeteePost;

	private int indexRoi;

	private int nbOrganes;

	public Model_GeneralDyn(ImageSelection[] selectedImages, String studyName, int[] frameDuration) {
		super(selectedImages, studyName, frameDuration);

		this.impProjetee = selectedImages[0];
		this.impAnt = selectedImages[1];
		this.impPost = selectedImages[2];
		this.impProjeteeAnt = selectedImages[3];
		this.impProjeteePost = selectedImages[4];
	}

	@Override
	public void calculateResults() {
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
			System.out.println("Je put : this.getData().put("+k+", this.adjustValues("+data+"))");
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("\n");

		for (String k : this.getData().keySet()) {
			s.append(k);
			for (Double d : this.getData().get(k)) {
				s.append(",").append(d);
			}
			s.append("\n");
		}

		return s.toString();
	}

	void saveValues(ImagePlus imp) {
//		this.selectedImages[0].setImagePlus(imp);
		// this.getScin().setImp(imp);
		this.indexRoi = 0;
		this.nbOrganes = this.getRoiManager().getCount();
		HashMap<String, List<Double>> mapData = new HashMap<>();
		// on copie les roi sur toutes les slices
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			System.out.println(i);
			for (this.indexRoi = 0; this.indexRoi < this.nbOrganes; this.indexRoi++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
				String name = this.getNomOrgane(this.indexRoi);

				// String name = studyName.substring(0, studyName.lastIndexOf(" "));
				// on cree la liste si elle n'existe pas
				mapData.computeIfAbsent(name, k -> new ArrayList<>());
				// on y ajoute le nombre de coups
				mapData.get(name).add(Library_Quantif.getCounts(imp));
			}
		}
		// set data to the model
		this.setData(mapData);
		this.calculateResults();

	}

	public Roi getOrganRoi(int lastRoi) {
		return this.getRoiManager().getRoi(this.indexRoi % this.nbOrganes);
	}

	public String getNomOrgane(int index) {
		return this.getRoiManager().getRoi(index % this.nbOrganes).getName();
	}

	public ImageSelection getImpProjetee() {
		return this.impProjetee;
	}

	public ImageSelection getImpAnt() {
		return this.impAnt;
	}

	public ImageSelection getImpPost() {
		return this.impPost;
	}

	public ImageSelection getImpProjeteeAnt() {
		return this.impProjeteeAnt;
	}

	public ImageSelection getImpProjeteePost() {
		return this.impProjeteePost;
	}

	public String[] getRoiNames() {
		String[] roiNames = new String[this.getRoiManager().getCount()];
		for (int i = 0; i < this.getRoiManager().getCount(); i++) {
			roiNames[i] = this.getRoiManager().getRoi(i).getName();
		}
		return roiNames;
	}

}
