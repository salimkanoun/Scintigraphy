package org.petctviewer.scintigraphy.hepatic.dynRefactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.Roi;

public class ModelHepaticDynamic extends ModeleScinDyn {
	
	private TabResult resutlTab;

	private int[] frames;
	
	private int[] times;

	private ImagePlus[] captures;
	
	private ImageSelection[] impSecondMethod;
	
	int indexRoi;
	
	int nbOrganes;

	public ModelHepaticDynamic(ImageSelection[] selectedImages, String studyName, int[] frameDuration) {
		super(selectedImages, studyName, frameDuration);
		System.out.println("STUDY NAME DEPUIS MODEL"+studyName);
		this.frames = new int[3];
		this.times = new int[6];
		this.captures = new ImagePlus[3];
	}

	public void setTimes(int label1, int label2, int label3) {
		this.frames[0] = label1;
		this.frames[1] = label2;
		this.frames[2] = label3;
		
		int frameDuration[] = this.getFrameduration();
		
		int time1 = 0;
		int time2 = 0;
		int time3 = 0;
		for(int i = 0 ; i < label1-1 ; i++)
			time1+=frameDuration[i];
		
		for(int i = 0 ; i < label2-1 ; i++)
			time2+=frameDuration[i];
		
		for(int i = 0 ; i < label3-1 ; i++)
			time3+=frameDuration[i];
		
		
		this.times[0] = time1;
		this.times[1] = time2;
		this.times[2] = time3;
		this.times[3] = time2 - time1;
		this.times[4] = time3 - time2;
		this.times[5] = time3 - time1;
	}

	public int[] getFrames() {
		return this.frames;
	}

	public void setCapture(ImagePlus imp, int i) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, 512, 0);
	}

	public void setCapture(ImagePlus imp, int i, int captureWidht) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, captureWidht, 0);
	}

	public void setCapture(ImagePlus imp, int i, int captureWidht, int captureHeight) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, captureWidht, captureHeight);
	}
	
	public ImagePlus[] getCaptures(){
		return this.captures;
	}

	public String[] getResult() {
		String[] retour = new String[6];
		
		retour[0] = "Temps avant première frame (frame n°"+this.frames[0]+") : "+(this.times[0]/1000)+" secondes";
		retour[1] = "Temps avant première frame (frame n°"+this.frames[1]+") : "+(this.times[1]/1000)+" secondes";
		retour[2] = "Temps avant première frame (frame n°"+this.frames[2]+") : "+(this.times[2]/1000)+" secondes";
		
		retour[3] = "Différence de temps frame2 - frame1 : "+(this.times[3]/1000/60)+" minutes et "+((this.times[3]/1000)-((this.times[3]/1000/60)*60))+" secondes";
		retour[4] = "Différence de temps frame3 - frame2 : "+(this.times[4]/1000/60)+" minutes et "+((this.times[4]/1000)-((this.times[4]/1000/60)*60))+" secondes";
		retour[5] = "Différence de temps frame3 - frame1 : "+(this.times[5]/1000/60)+" minutes et "+((this.times[5]/1000)-((this.times[5]/1000/60)*60))+" secondes";

		return retour;
	}
	
	public void setResultTab(TabResult resultTab) {
		this.resutlTab = resultTab;
	}
	
	public void setImpSecondMethod(ImageSelection[] selectedImages) {
		this.impSecondMethod = selectedImages;
	}
	
	public ImageSelection[] getImpSecondMethod() {
		return this.impSecondMethod;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * ****************************************************Importe à la deuxième méthode*********************************************************************************
	 */
	private Double tDemiFoieDFit, tDemiFoieGFit, tDemiVascFit, tDemiFoieDObs, tDemiFoieGObs, tDemiVascObs;
	private Double maxFoieD, maxFoieG, finPicD, finPicG, pctVasc;
	
	@Override
	public void calculerResultats() {
		for(String s : this.getData().keySet())
			System.out.println(s);
		XYSeries liverL = this.getSerie("Left Liver");
		XYSeries liverR = this.getSerie("Right Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");

		this.maxFoieD = Library_JFreeChart.getAbsMaxY(liverR);
		// this.tDemiFoieDFit = Modele_HepaticDyn.getTDemiFit(liverR, (this.maxFoieD +
		// 2)*1.0);
		this.tDemiFoieDObs = Library_JFreeChart.getTDemiObs(liverR, this.maxFoieD + 2);
		this.finPicD = liverR.getY(liverR.getItemCount() - 1).doubleValue() / liverR.getMaxY();

		this.maxFoieG = Library_JFreeChart.getAbsMaxY(liverL);
		// this.tDemiFoieGFit = Modele_HepaticDyn.getTDemiFit(liverL, this.maxFoieG +
		// 2);
		this.tDemiFoieGObs = Library_JFreeChart.getTDemiObs(liverL, this.maxFoieG + 2);
		this.finPicG = liverL.getY(liverL.getItemCount() - 1).doubleValue() / liverL.getMaxY();

		this.pctVasc = Library_JFreeChart.getY(bloodPool, 20.0) / Library_JFreeChart.getY(bloodPool, 5.0);
		// this.tDemiVascFit = Modele_HepaticDyn.getTDemiFit(bloodPool, 20.0);
		this.tDemiVascObs = Library_JFreeChart.getTDemiObs(bloodPool, 20.0);

	}
	
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> hm = new HashMap<>();

		// foie droit
		hm.put("T1/2 Righ Liver", this.tDemiFoieDObs + "mn");
		hm.put("T1/2 Righ Liver *", this.tDemiFoieDFit + "mn");
		hm.put("Maximum Right Liver", Library_Quantif.round(this.maxFoieD, 1) + "mn");
		hm.put("end/max Ratio Right", (int) (this.finPicD * 100) + "%");

		// foie gauche
		hm.put("T1/2 Left Liver", this.tDemiFoieGObs + "mn");
		hm.put("T1/2 Left Liver *", this.tDemiFoieGFit + "mn");
		hm.put("Maximum Left Liver", Library_Quantif.round(this.maxFoieG, 1) + "mn");
		hm.put("end/max Ratio Left", (int) (this.finPicG * 100) + "%");

		// vasculaire
		hm.put("Blood pool ratio 20mn/5mn", (int) (this.pctVasc * 100) + "%");
		hm.put("T1/2 Blood pool", this.tDemiVascObs + "mn");
		hm.put("T1/2 Blood pool *", this.tDemiVascFit + "mn");

		return hm;
	}
	
	
	
		public void enregistrerMesure(String nomRoi, ImagePlus imp) {
			// si le modele n'est pas bloque
			if (!this.isLocked()) {
				//recupere la phrase sans le dernier mot (tag)
				
				String name = nomRoi.substring(nomRoi.lastIndexOf("_"), nomRoi.length()-1);
				System.out.println("Name : "+name);
				// on cree la liste si elle n'existe pas
				if (this.getData().get(name) == null) {
					this.getData().put(name, new ArrayList<Double>());
				}
				// on y ajoute le nombre de coups
				this.getData().get(name).add(Library_Quantif.getCounts(imp));
			}
		}
	
	
		public void saveValues() {
//			this.selectedImages[0].setImagePlus(imp);
			ImagePlus imp = impSecondMethod[1].getImagePlus();
			// this.getScin().setImp(imp);
			this.indexRoi = 0;
			this.nbOrganes = this.getRoiManager().getCount();
			HashMap<String, List<Double>> mapData = new HashMap<String, List<Double>>();
			// on copie les roi sur toutes les slices
			for (int i = 1; i <= imp.getStackSize(); i++) {
				imp.setSlice(i);
				for (this.indexRoi = 0; this.indexRoi < this.nbOrganes; this.indexRoi++) {
					imp.setRoi(getOrganRoi(this.indexRoi));
//					String name = this.getNomOrgane(this.indexRoi);
					String name = this.getNomOrgane(this.indexRoi).substring(this.getNomOrgane(this.indexRoi).lastIndexOf("_")+1, this.getNomOrgane(this.indexRoi).length()-1);
					// on cree la liste si elle n'existe pas
					if (mapData.get(name) == null) {
						mapData.put(name, new ArrayList<Double>());
					}
					// on y ajoute le nombre de coups
					mapData.get(name).add(Library_Quantif.getCounts(imp));
				}
			}
			System.out.println("Size Left Liver :"+mapData.get("Left Liver").size());
			System.out.println("Size Hilium :"+mapData.get("Hilium").size());
			System.out.println("Size Blood pool :"+mapData.get("Blood pool").size());
			System.out.println("Size Right Liver :"+mapData.get("Right Liver").size());
			System.out.println("Size CBD :"+mapData.get("CBD").size());
			System.out.println("Duodenom :"+mapData.get("Duodenom").size());
			// set data to the model
			this.setData(mapData);
			this.calculerResultats();

		}
	
	
	
		public Roi getOrganRoi(int lastRoi) {
			return this.getRoiManager().getRoi(this.indexRoi % this.nbOrganes);
		}

		public String getNomOrgane(int index) {
			return this.getRoiManager().getRoi(index % this.nbOrganes).getName();
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public String toString() {
		XYSeries liverL = this.getSerie("L. Liver");
		XYSeries liverR = this.getSerie("R. Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");

		String s = "";
		s += "Time (mn),";
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s += Library_Quantif.round(bloodPool.getX(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Blood Pool (counts/sec),";
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s += Library_Quantif.round(bloodPool.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Right Liver (counts/sec),";
		for (int i = 0; i < liverR.getItemCount(); i++) {
			s += Library_Quantif.round(liverR.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Left Liver (counts/sec),";
		for (int i = 0; i < liverL.getItemCount(); i++) {
			s += Library_Quantif.round(liverL.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "T1/2 Right Liver Obs," + this.tDemiFoieDObs + "mn" + "\n";
		s += "T1/2 Right Liver Fit," + this.tDemiFoieDFit + "mn" + "\n";
		s += "Maximum Right Liver," + this.maxFoieD + "mn" + "\n";
		s += "END/MAX Ratio Right," + (int) (this.finPicD * 100) + "%" + "\n";

		s += "T1/2 Left Liver Obs," + this.tDemiFoieGObs + "mn" + "\n";
		s += "T1/2 Left Liver Fit," + this.tDemiFoieGFit + "mn" + "\n";
		s += "Maximum Left Liver," + this.maxFoieG + "mn" + "\n";
		s += "END/MAX Ratio Left," + (int) (this.finPicG * 100) + "%" + "\n";

		s += "Blood pool ratio 20mn/5mn," + (int) (this.pctVasc * 100) + "%" + "\n";
		s += "T1/2 Blood pool Obs," + this.tDemiVascObs + "mn" + "\n";
		s += "T1/2 Blood pool Fit," + this.tDemiVascFit + "mn" + "\n";

		return s;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
