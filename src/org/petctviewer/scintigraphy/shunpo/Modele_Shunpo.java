/**
Copyright (C) 2017 MOHAND Mathis and KANOUN Salim

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

package org.petctviewer.scintigraphy.shunpo;

import ij.Prefs;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map.Entry;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import ij.process.ImageStatistics;
import ij.util.DicomTools;

public class Modele_Shunpo {

	private HashMap<String, Double> coups;
	private HashMap<String, Integer> mgs;
	private String[] abvsMG = { "PD", "PG", "RD", "RG", "C" };
	private String patient;
	private String date;
	private String dateForm;
	private int pixrdp;
	private int pixrgp;
	private int pixrda;
	private int pixrga;
	protected String[] retour;
	protected static double shunt;

	public Modele_Shunpo() {
		coups = new HashMap<>();
		mgs = new HashMap<>();
		Prefs.useNamesAsLabels = true;
	}

	protected static enum Etat {
		PoumonD_Post, PoumonG_Post, ReinD_Post, ReinG_Post, BDF, PoumonD_Ant, PoumonG_Ant, ReinD_Ant, ReinG_Ant, Poumon_valide, Cerveau_Post, Cerveau_Ant, Fin;
		private static Etat[] vals = values();

		public Etat next() {
			return vals[(this.ordinal() + 1) % vals.length];
		}

		public Etat previous() {
			// On ajoute un vals.length car le modulo peut ��tre < 0 en java
			return vals[((this.ordinal() - 1) + vals.length) % vals.length];
		}
	}

	// On recupere le nom du patient, la date et son id pour les resultats
	protected void setPatient(String pat, ImagePlus imp) {
		patient = pat;
		date = DicomTools.getTag(imp, "0008,0020");
		char[] a = date.toCharArray();
		if (date != null && !date.isEmpty())
			date = date.trim();
		dateForm = "" + a[7] + a[8] + "/" + a[5] + a[6] + "/" + a[1] + a[2] + a[3] + a[4];
	}

	// Cree le montage a partir de l'ImageStack
	protected ImagePlus montage(ImageStack stackCapture, String nomProgramme) {
		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Resultats ShunPo -" + patient, stackCapture);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		imp.setTitle("Resultats " + nomProgramme + " -" + patient);
		return imp;
	}

	protected void calculerCoups(String roi, ImagePlus imp) {
		ImageStatistics is = imp.getStatistics();

		if (roi.contains("BDF"))
			coups.put(roi, is.mean);
		else {
			if (roi.contains("R")) {
				if (roi.equals("RDP"))
					pixrdp = is.pixelCount;
				if (roi.equals("RGP"))
					pixrgp = is.pixelCount;
				if (roi.equals("RDA"))
					pixrda = is.pixelCount;
				if (roi.equals("RGA"))
					pixrga = is.pixelCount;
			}
			coups.put(roi, is.pixelCount * is.mean);
		}
		if (Controleur_Shunpo.showLog) {
			IJ.log(roi + "coups= " + String.valueOf(is.pixelCount * is.mean));
		}
	}

	protected double getCoups(String roi) {
		return coups.get(roi);
	}

	private String convertAbrev(String abv) {
		char[] decomp = abv.toCharArray();
		String result = "";
		for (int i = 0; i < decomp.length; i++) {
			switch (decomp[i]) {
			case 'P':
				result += "Poumon ";
				break;
			case 'R':
				result += "Rein ";
				break;
			case 'C':
				result += "Cerveau ";
				break;
			case 'M':
				result += "MG ";
				i++;
				break;
			case 'D':
				result += "Droite ";
				break;
			case 'G':
				result += "Gauche ";
				break;
			}
			if (i == decomp.length - 1)
				result += ": ";
		}
		return result;
	}

	private void mgs() {
		for (String abv : abvsMG)
			moyenneGeo(abv);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(String abv) {
		double[] coupsa = new double[2];
		String[] asuppr = new String[2];
		int index = 0;
		for (Entry<String, Double> entry : coups.entrySet()) {
			if (entry.getKey().contains(abv)) {
				coupsa[index] = entry.getValue();
				asuppr[index] = entry.getKey();
				index++;
			}
		}
		for (String so : asuppr)
			coups.remove(so);
		mgs.put("MG" + abv, moyenneGeometrique(coupsa));
	}

	// Retrait du BDF aux reins
	private void coupsReins() {
		double rdp = coups.get("RDP");
		double rgp = coups.get("RGP");
		double bdfp = coups.get("BDFP");
		coups.put("RDP", rdp - (bdfp * pixrdp));
		coups.put("RGP", rgp - (bdfp * pixrgp));
		double rda = coups.get("RDA");
		double rga = coups.get("RGA");
		double bdfa = coups.get("BDFA");
		coups.put("RDA", rda - (bdfa * pixrda));
		coups.put("RGA", rga - (bdfa * pixrga));
	}

	// Calcule la moyenne g茅om茅trique des nombres en param猫tre
	private int moyenneGeometrique(double[] vals) {
		double result = 1.0;
		for (int i = 0; i < vals.length; i++) {
			result *= vals[i];
		}
		result = Math.sqrt(result);
		return (int) result;
	}

	protected String[] resultats() {
		retour = new String[9];
		coupsReins();
		int index = 0;
		// Les 5 MGs
		mgs();
		for (Entry<String, Integer> entry : mgs.entrySet()) {
			retour[index] = convertAbrev(entry.getKey()) + entry.getValue();
			index++;
		}
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		// Calculs
		double percPD = (mgs.get("MGPD") / (1.0 * mgs.get("MGPD") + mgs.get("MGPG"))) * 100;
		retour[3] += " (" + us.format(percPD) + "%)";
		double percPG = (mgs.get("MGPG") / (1.0 * mgs.get("MGPD") + mgs.get("MGPG"))) * 100;
		retour[0] += " (" + us.format(percPG) + "%)";
		int totmg = mgs.get("MGPD") + mgs.get("MGPG");
		retour[index] = "Total MG : " + totmg;
		index++;
		int totshunt = mgs.get("MGRD") + mgs.get("MGRG") + mgs.get("MGC");
		retour[index] = "Total Shunt : " + totshunt;
		index++;
		double percSyst = (100.0 * totshunt) / totmg;
		retour[index] = "% Systemic : " + us.format(percSyst) + "%";
		index++;
		Modele_Shunpo.shunt = ((totshunt * 100.0) / (totmg * 0.38));
		retour[index] = "Pulmonary Shunt : " + us.format(Modele_Shunpo.shunt) + "% (total blood Flow)";

		String[] clone = new String[10];
		clone[0] = retour[3];
		clone[1] = retour[5];
		clone[2] = retour[0];
		clone[3] = retour[6];
		clone[4] = retour[2];
		clone[5] = retour[7];
		clone[6] = retour[1];
		clone[7] = retour[8];
		clone[8] = retour[4];
		clone[9] = patient + " " + dateForm;
		return clone;
	}

	protected String[] buildCSVResultats() {

		String[] res2 = retour.clone();
		String[] res3 = new String[(res2.length) * 2];
		for (int i = 0, j = 0; i < res2.length; i++, j++) {
			res3[j] = res2[i].split(":")[0];
			res3[j] = res3[j].trim();
			j++;
			res3[j] = res2[i].split(":")[1];
			res3[j] = res3[j].trim();
		}
		return res3;
	}

}
