/**
Copyright (C) 2017 PING Xie and KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

import ij.Prefs;
import ij.WindowManager;
import java.awt.Font;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageStatistics;

public class Modele_VG_Dynamique {

	public static Font italic = new Font("Arial", Font.ITALIC, 8);

	private int[][][] coupsBrut;//pour enregistrer le nombre de coups de chaque organe pour chaque image
	private int[][][] coupsCorrigeBDF;//pour enregistrer le nombre de coups apre la premiere correction de chaque organe pour chaque image
	private int[] coupsOeufs;//pour enregistrer le nombre de coups de chaque oeuf
	protected static double[] oeufPourc;//pour enregistrer le % de chaque oeuf
	private double[] oeufNonIngerePourc;//pour enregistrer le % des oeufs non ingeres
	private double[][] pourcCorrigeOeuf;//pour enregistrer le % apre la deuxieme correction de chaque organe pour chaque image
	private String[] tempsAcquisition;//pour enregistrer le temps d'acquisition de chaque serie
	private int[] temps;//pour enregistrer l'heure d'acquisition de chaque serie par rapport a l'heure ou le patient commence a manger
	private String[] nomSerie;
	private int finBDFAntre;//pour enregistrer l'index de serie depuis laquelle la region de l'antre est bruit de fond
	private int finBDFIntestin;//pour enregistrer l'index de serie depuis laquelle la region de l'intestin est bruit de fond
	protected static boolean logOn;//signifie si le log est ouvert

	public Modele_VG_Dynamique() {

		Prefs.useNamesAsLabels = true;
		this.finBDFAntre = 0;
		this.finBDFIntestin = 0;
		logOn=false;
	}

	public enum Etat {
		ESTOMAC_ANT, INTESTIN_ANT, ESTOMAC_POS, INTESTIN_POS, CIR_ESTOMAC_ANT, CIR_INTESTIN_ANT, CIR_ESTOMAC_POS, CIR_INTESTIN_POS, OEUFOUVERT, ROIDEFAULT, OEUFS, CORRIGER,FIN;
		private static Etat[] vals = values();
		
		public Etat next() {
			return vals[(this.ordinal() + 1) % vals.length];
		}

		public Etat previous() {
			// On ajoute un vals.length car le modulo peut ¨ºtre < 0 en java
			return vals[((this.ordinal() - 1) + vals.length) % vals.length];
		}
	}

	// calcule le coups de chaque organe pour chaque image
	// i: l'index de serie, j: 0 (image ANT de la serie), 1 (image POS de la serie), k: 0 estomac, 1 intestin, 2 antre, 3 fundus, 4 total
	public void calculerCoupsBrut(int i, int j, int k, ImagePlus imp) {
		// Ancienne methode mathis nombre de pixel * moyenne
		ImageStatistics is = imp.getStatistics();
		this.coupsBrut[i - 1][j][k] = (int) (is.pixelCount * is.mean);
		if (Modele_VG_Dynamique.logOn) IJ.log(""+this.coupsBrut[i - 1][j][k]);
	}

	// calcule le coups de chaque oeuf
	public void calculerCoupsOeufs(int i, ImagePlus imp) {
		ImageStatistics is = imp.getStatistics();
		this.coupsOeufs[i - 1] = (int) (is.pixelCount * is.mean);
	}
	
	// calcule le % de chaque oeuf par rapport au repas total
	public void calculerOeufsPourcent() {
		int coupsOeufsTotal = 0;
		for (int i = 0; i < oeufPourc.length; i++) {
			coupsOeufsTotal = coupsOeufsTotal + this.coupsOeufs[i];
		}

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		if (Modele_VG_Dynamique.logOn) IJ.log("pourcentage des oeufs calcule");
		for (int i = 0; i < oeufPourc.length; i++) {
			double pourOeuf = ((double) this.coupsOeufs[i] / (double) coupsOeufsTotal) * 100;
			oeufPourc[i] = Double.parseDouble(us.format(pourOeuf));
			if (Modele_VG_Dynamique.logOn) IJ.log("egg"+(i+1)+" : "+oeufPourc[i]);
		}
	}

	public int getCoupsBrut(int i, int j, int k) {
		return this.coupsBrut[i - 1][j][k];
	}

	public void setCoupsBrut(int i, int j, int k, int valeur) {
		this.coupsBrut[i - 1][j][k] = valeur;
	}

	public void calculerTempsImages() {
		for (int i = 0; i < this.tempsAcquisition.length; i++) {
			try {
				this.tempsImage(i);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	//calculer l'heure d'acquisition de chaque serie par rapport a l'heure ou le patient commence a manger
	private void tempsImage(int index) throws ParseException {
		DateFormat df = new SimpleDateFormat("HHmmss");
		Date d1 = df.parse(this.tempsAcquisition[index].substring(1, 7));
		Date d2 = df.parse(this.tempsAcquisition[0].substring(1, 7));
		long diff = d1.getTime() - d2.getTime();
		long day = diff / (24 * 60 * 60 * 1000);
		long hour = (diff / (60 * 60 * 1000) - day * 24);
		long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		this.temps[index] = (int) (day * 24 * 60 + hour * 60 + min);
	}

	// initialisation des tables de resultats
	public void initModele(ImagePlus imp) {
		this.coupsBrut = new int[imp.getStackSize() / 2][2][5];
		this.coupsCorrigeBDF = new int[imp.getStackSize() / 2][3][5];
		this.coupsOeufs = new int[imp.getStackSize() / 2];
		this.temps = new int[imp.getStackSize() / 2];
		this.tempsAcquisition = new String[imp.getStackSize() / 2];
		this.nomSerie = new String[imp.getStackSize() / 2];
		oeufPourc = new double[imp.getStackSize() / 2];
		this.oeufNonIngerePourc = new double[imp.getStackSize() / 2+1];
		this.pourcCorrigeOeuf=new double[imp.getStackSize() / 2][4];
		for(int i=0; i<oeufPourc.length;i++){
			oeufPourc[i]=(1.0/(double)oeufPourc.length)*100;
		}
	}

	public void corrigerParBDF() {
		for (int i = 0; i < WindowManager.getCurrentImage().getStackSize() / 2; i++) {
			// Modification des valeurs du fundus
			this.coupsCorrigeBDF[i][0][0] = Math.max(this.coupsBrut[i][0][0] - this.coupsBrut[0][0][0], 0);
			this.coupsCorrigeBDF[i][1][0] = Math.max(this.coupsBrut[i][1][0] - this.coupsBrut[0][1][0], 0);

			// Modification des valeurs de l'antre
			if (this.finBDFAntre == 0) {
				this.coupsCorrigeBDF[i][0][1] = this.coupsBrut[i][0][1];
				this.coupsCorrigeBDF[i][1][1] = this.coupsBrut[i][1][1];
			} else if (i >= this.finBDFAntre) {
				this.coupsCorrigeBDF[i][0][1] = Math
						.max(this.coupsBrut[i][0][1] - this.coupsBrut[this.finBDFAntre - 1][0][1], 0);
				this.coupsCorrigeBDF[i][1][1] = Math
						.max(this.coupsBrut[i][1][1] - this.coupsBrut[this.finBDFAntre - 1][1][1], 0);
			} else {
				this.coupsCorrigeBDF[i][0][1] = 0;
				this.coupsCorrigeBDF[i][1][1] = 0;
			}

			// modification des valeurs de l'intestin
			if (this.finBDFIntestin == 0) {
				this.coupsCorrigeBDF[i][0][2] = this.coupsBrut[i][0][2];
				this.coupsCorrigeBDF[i][1][2] = this.coupsBrut[i][1][2];
			} else if (i >= this.finBDFIntestin) {
				this.coupsCorrigeBDF[i][0][2] = Math
						.max(this.coupsBrut[i][0][2] - this.coupsBrut[this.finBDFIntestin - 1][0][2], 0);
				this.coupsCorrigeBDF[i][1][2] = Math
						.max(this.coupsBrut[i][1][2] - this.coupsBrut[this.finBDFIntestin - 1][1][2], 0);
			} else {
				this.coupsCorrigeBDF[i][0][2] = 0;
				this.coupsCorrigeBDF[i][1][2] = 0;
			}

			// mis a jour des valeur de l'estomac et le total
			for (int j = 0; j < 2; j++) {
				this.coupsCorrigeBDF[i][j][3] = this.coupsCorrigeBDF[i][j][0] + this.coupsCorrigeBDF[i][j][1];
				this.coupsCorrigeBDF[i][j][4] = this.coupsCorrigeBDF[i][j][2] + this.coupsCorrigeBDF[i][j][3];
			}

			// calcul des Moyennes geometriques
			for (int k = 0; k < 3; k++) {
				this.coupsCorrigeBDF[i][2][k] = (int) Math
						.sqrt((double) this.coupsCorrigeBDF[i][0][k] * (double) this.coupsCorrigeBDF[i][1][k]);
			}
			this.coupsCorrigeBDF[i][2][3] = this.coupsCorrigeBDF[i][2][0] + this.coupsCorrigeBDF[i][2][1];
			this.coupsCorrigeBDF[i][2][4] = this.coupsCorrigeBDF[i][2][2] + this.coupsCorrigeBDF[i][2][3];
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 5; k++) {
					if (Modele_VG_Dynamique.logOn) IJ.log(i + " " + j + " " + k + " " + this.coupsBrut[i][j][k] + "------" + this.coupsCorrigeBDF[i][j][k]);
				}
			}
			for (int k = 0; k < 5; k++) {
				if (Modele_VG_Dynamique.logOn) IJ.log(i + " " + 2 + " " + k + " " + this.coupsCorrigeBDF[i][2][k]);
			}
		}
	}
	
	// Pour corriger les resultats par les oeufs non ingeres
	public void corrigerParOeuf(){

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		this.oeufNonIngerePourc[0]=Double.parseDouble(us.format(100.00));
		if (Modele_VG_Dynamique.logOn) IJ.log("resultats apres corrige par oeufs");
		for (int i = 0; i < oeufPourc.length; i++) {
			this.oeufNonIngerePourc[i+1]=Double.parseDouble(us.format(this.oeufNonIngerePourc[i]-oeufPourc[i]));
			if (Modele_VG_Dynamique.logOn) IJ.log("egg"+(i+1)+" : "+us.format(oeufPourc[i])+"non ingere :"+this.oeufNonIngerePourc[i]);
			this.pourcCorrigeOeuf[i][0]=Double.parseDouble(us.format(this.oeufNonIngerePourc[i]+((double)this.coupsCorrigeBDF[i][2][0]/((double)this.coupsCorrigeBDF[i][2][4]+0.00000000001))*(100.00-this.oeufNonIngerePourc[i])));
			this.pourcCorrigeOeuf[i][1]=Double.parseDouble(us.format(((double)this.coupsCorrigeBDF[i][2][1]/((double)this.coupsCorrigeBDF[i][2][4]+0.00000000001))*(100.00-this.oeufNonIngerePourc[i])));
			this.pourcCorrigeOeuf[i][3]=Double.parseDouble(us.format(this.pourcCorrigeOeuf[i][0]+this.pourcCorrigeOeuf[i][1]));
			this.pourcCorrigeOeuf[i][2]=Double.parseDouble(us.format(100.00-this.pourcCorrigeOeuf[i][3]));
			if (Modele_VG_Dynamique.logOn) IJ.log("coups avant corrige: "+"   pourcentage apres corrige");
			if (Modele_VG_Dynamique.logOn) IJ.log("fundus "+this.coupsCorrigeBDF[i][2][0]+"              "+this.pourcCorrigeOeuf[i][0]);
			if (Modele_VG_Dynamique.logOn) IJ.log("antre "+this.coupsCorrigeBDF[i][2][1]+"              "+this.pourcCorrigeOeuf[i][1]);
			if (Modele_VG_Dynamique.logOn) IJ.log("intestin "+this.coupsCorrigeBDF[i][2][2]+"              "+this.pourcCorrigeOeuf[i][2]);
			if (Modele_VG_Dynamique.logOn) IJ.log("estomac "+this.coupsCorrigeBDF[i][2][3]+"              "+this.pourcCorrigeOeuf[i][3]);
		}
	}
	
	public void setAcquisitionTime(int index, String time) {
		this.tempsAcquisition[index - 1] = time;
	}

	public void setNomSerie(int index, String nomSerie) {
		this.nomSerie[index - 1] = nomSerie;
	}

	public void setFinBDFAntre(int finBDFAntre) {
		this.finBDFAntre = finBDFAntre;
	}

	public void setFinBDFIntestin(int finBDFIntestin) {
		this.finBDFIntestin = finBDFIntestin;
	}

	public int getFinBDFAntre() {
		return finBDFAntre;
	}

	public int getFinBDFIntestin() {
		return finBDFIntestin;
	}

	//permet de mettre les resultats en forme String pour transmettre au application VG_Ingestin_Statique
	public String getResultat() {
		String resultat=this.tempsAcquisition[0].substring(1,7)+";";
		for(int i=0; i<this.temps.length;i++){
			resultat=resultat+this.temps[i]+";";
			resultat=resultat+this.pourcCorrigeOeuf[i][3]+";";
			resultat=resultat+this.pourcCorrigeOeuf[i][0]+";";
			resultat=resultat+this.pourcCorrigeOeuf[i][1]+";";
		}
		
		return resultat;
	}

}