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

import java.awt.AWTException;

import ij.Prefs;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
//import ij.measure.Measurements;
//import ij.measure.ResultsTable;
import ij.plugin.MontageMaker;
//import ij.plugin.filter.Analyzer;
//import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
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
			// On ajoute un vals.length car le modulo peut ¨ºtre < 0 en java
			return vals[((this.ordinal() - 1) + vals.length) % vals.length];
		}
	}

	// On recupere le nom du patient, la date et son id pour les resultats
	protected void setPatient(String pat, ImagePlus imp) {
		patient = pat;
		date = DicomTools.getTag(imp, "0008,0020");
		char[] a = date.toCharArray();
		date = date.trim();
		dateForm = "" + a[7] + a[8] + "/" + a[5] + a[6] + "/" + a[1] + a[2] + a[3] + a[4];
	}

	/**
	 * Permet de creer un stack a partir d'un tableau d'ImagePlus
	 * @param tableauImagePlus : Tableau contenant les ImagePlus a mettre dans le stack (toutes les images doivent avoir la même taille)
	 * @return Renvoie le stack d'image produit
	 */
	public static ImageStack captureToStack(ImagePlus[] tableauImagePlus) {
		// On verifie que toutes les images ont la meme taille
		int[][] dimensionCapture = new int[4][2];
		for (int i = 0; i < tableauImagePlus.length; i++) {

			dimensionCapture[i] = tableauImagePlus[i].getDimensions();
			if (!Arrays.equals(dimensionCapture[i], tableauImagePlus[0].getDimensions())) {
				IJ.showMessage("Error Capture have different dimension");
			}
		}
		// On cree de stack de taille adhoc
		ImageStack stackCapture = new ImageStack(dimensionCapture[0][0], dimensionCapture[0][1]);
		// On rajoute les images dans le stack
		for (int i = 0; i < tableauImagePlus.length; i++) {

			stackCapture.addSlice(tableauImagePlus[i].getProcessor());
		}
		// On retourne le stack de Capture
		return stackCapture;
	}

	// Cree le montage a partir de l'ImageStack
	protected ImagePlus montage(ImageStack stackCapture, String nomProgramme) {
		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Resultats ShunPo -" + patient, stackCapture);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		imp.setTitle("Resultats " + nomProgramme + " -" + patient);
		return imp;
	}

	/**
	 * Capture secondaire de l'image sans l'interface et la redimmensionner à la taille voulue
	 * @param imp : ImagePlus a capturer
	 * @param largeur : largeur de la capture finale (si hauteur et largeur = 0 : pas de redimensionnement)
	 * @param hauteur : hauteur de la capture finale (si hauteur =0 on ne redimensionne que la largeur en gardant le ratio)
	 * @return Renvoie l'ImagePlus contenant la capture secondaire
	 */
	public static ImagePlus captureImage(ImagePlus imp, int largeur, int hauteur) {
		// Cette methode capture la partie image seule d'une fenetre
		ImageWindow win = imp.getWindow();
		win.toFront();
		IJ.wait(500);
		Point loc = win.getLocation();
		ImageCanvas ic = win.getCanvas();
		Rectangle bounds = ic.getBounds();
		loc.y += bounds.y;
		loc.x += bounds.x;
		BufferedImage buff = null;
		// efface le zoom indicateur : carre bleu en haut a gauche quand zoom inf a 1
		boolean wasHidden = ic.hideZoomIndicator(true);
		ic.repaint();
		try {
			Rectangle r = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
			buff = new Robot().createScreenCapture(r);

		} catch (AWTException e) {
		}
		ic.hideZoomIndicator(wasHidden);
		// On resize la capture aux dimensions choisies pour rentrer dans le
		// stack
		ImagePlus imp2 = new ImagePlus("Capture", buff);
		ImageProcessor ip = imp2.getProcessor();
		ip.setInterpolate(true);
		ip.setInterpolationMethod(ImageProcessor.BICUBIC);
		ImageProcessor ip2;
		if (hauteur == 0 && largeur !=0) {
			ip2 = ip.resize(largeur);
		} 
		else if (hauteur == 0 && largeur==0) {
			ip2 = ip;
		}
		else {
			ip2 = ip.resize(largeur, hauteur, true);
		}
		imp2.setProcessor(ip2);
		// On renvoie l'ImagePlus contenant la capture
		return imp2;
	}

	
	/**
	 * Permet de capturer la fenetre entiere et de choisir la taille de l'image finale
	 * @param imp : l'ImagePlus de la fenetre à capturer
	 * @param largeur : largeur de l'image finale si largeur et hauteur =0 pas de resize on a la meme resolution que l'ecran
	 * @param hauteur : hauteur de l'image finale si hauteur =0 on ne resize que la largeur en gardant le même ratio
	 * @return Resultat de la capture dans une ImagePlus
	 */
	public static ImagePlus captureFenetre(ImagePlus imp, int largeur, int hauteur) {
		ImageWindow win = imp.getWindow();
		Point loc = win.getLocation();
		Rectangle bounds = win.getBounds();
		bounds.height -= 66;
		bounds.width -= 20;
		loc.y += 58;
		loc.x += 9;
		win.toFront();
		ImageCanvas ic = win.getCanvas();
		boolean wasHidden = ic.hideZoomIndicator(true);
		IJ.wait(500);
		BufferedImage buff = null;
		try {
			Rectangle rec = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
			buff = new Robot().createScreenCapture(rec);
		} catch (AWTException e) {
		}
		ic.hideZoomIndicator(wasHidden);
		// On ferme le ROI manage et fenetre resultat
		ImagePlus imp2 = new ImagePlus("Results Capture", buff);
		ImageProcessor ip = imp2.getProcessor();
		ip.setInterpolate(true);
		ip.setInterpolationMethod(ImageProcessor.BICUBIC);
		// Si hauteur=0 on resize que la largeur (et le ratio est maintenu)
		// sinon on resize largeur et hauteur (et peut changer le ratio)
		ImageProcessor ip2 = null;
		if (hauteur == 0 && largeur!=0) {
			ip2 = ip.resize(largeur);
		}
		else if (hauteur==0 && largeur ==0){
			ip2=ip;
		}
		else {
			ip2 = ip.resize(largeur, hauteur, true);
		}
		imp2.setProcessor(ip2);
		return imp2;
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

	// Calcule la moyenne gèŒ…omèŒ…trique pour un organe spèŒ…cifique
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

	// Calcule la moyenne gèŒ…omèŒ…trique des nombres en paramçŒ«tre
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
		// Permet de definir le nombre de chiffre aprçŒ«s la virgule et mettre la
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

	/**
	 * Permet de generer la 1ere partie du Header qui servira a la capture finale
	 * @param imp : imageplus originale (pour recuperer des elements du Header tels que le nom du patient...)
	 * @param nomProgramme : nom du programme qui l'utilise si par exemple "pulmonary shunt"  la capture sera appelee "Capture Pulmonary Shunt"
	 * @return retourne la première partie du header en string auquelle on ajoutera la 2eme partie via la deuxieme methode
	 */
	public static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme) {
		String sopID = generateSOPInstanceUID(new Date());
		String uid = generateUID6digits();
		String tag = "0002,0002 Media Storage SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0002,0003 Media Storage SOP Inst UID: " + sopID + "\n" 
				+ "0002,0010 Transfer Syntax UID: " + "1.2.840.10008.1.2.1" + "\n" 
				+ "0002,0013 Implementation Version Name: jpeg" + "\n"
				+ "0002,0016 Source Application Entity Title: " + "\n" 
				+ "0008,0008 Image Type: DERIVED\\SECONDARY " + "\n" 
				+ "0008,0016 SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0008,0018 SOP Instance UID: " + sopID + "\n" 
				+ "0008,0020 Study Date:" + DicomTools.getTag(imp, "0008,0020") + "\n" 
				+ "0008,0021 Series Date:" + DicomTools.getTag(imp, "0008,0021") + "\n" 
				+ "0008,0030 Study Time:" + DicomTools.getTag(imp, "0008,0030") + "\n" 
				+ "0008,0031 Series Time:" + DicomTools.getTag(imp, "0008,0031") + "\n" 
				+ "0008,0050 Accession Number:" + DicomTools.getTag(imp, "0008,0050") + "\n" 
				+ "0008,0060 Modality:" + DicomTools.getTag(imp, "0008,0060") + "\n" 
				+ "0008,0064 Conversion Type: WSD" + "\n"
				+ "0008,0070 Manufacturer:" + DicomTools.getTag(imp, "0008,0070") + "\n" 
				+ "0008,0080 Institution Name:" + DicomTools.getTag(imp, "0008,0080") + "\n" 
				+ "0008,0090 Referring Physician's Name:" + DicomTools.getTag(imp, "0008,0090") + "\n" 
				+ "0008,1030 Study Description:" + DicomTools.getTag(imp, "0008,1030") + "\n" 
				+ "0008,103E Series Description: Capture " + nomProgramme + "\n" 
				+ "0010,0010 Patient's Name:" + DicomTools.getTag(imp, "0010,0010") + "\n"
				+ "0010,0020 Patient ID:" + DicomTools.getTag(imp, "0010,0020") + "\n"
				+ "0010,0030 Patient's Birth Date:" + DicomTools.getTag(imp, "0010,0030") + "\n"
				+ "0010,0040 Patient's Sex:" + DicomTools.getTag(imp, "0010,0040") + "\n"
				+ "0020,000D Study Instance UID:" + DicomTools.getTag(imp, "0020,000D") + "\n"
				+ "0020,000E Series Instance UID:" + DicomTools.getTag(imp, "0020,000E").substring(0, DicomTools.getTag(imp, "0020,000E").length() - 6) + uid + "\n" 
				+ "0020,0010 Study ID :" + DicomTools.getTag(imp, "0020,0010") + "\n"
				+ "0020,0011 Series Number: 1337" + "\n" + "0020,0013 Instance Number: 1" + "\n"
				+ "0020,0032 Image Position (Patient):" + DicomTools.getTag(imp, "0020,0032") + "\n"
				+ "0020,0037 Image Orientation (Patient):" + DicomTools.getTag(imp, "0020,0037") + "\n"
				+ "0028,0002 Samples per Pixel: 3" + "\n" + "0028,0004 Photometric Interpretation: RGB" + "\n"
				+ "0028,0006 Planar Configuration: 0" + "\n" + "0028,0008 Number of Frames: 1 \n";
		return tag;
	}
	
	private static String generateSOPInstanceUID(Date dt0) {
		Date dt1 = dt0;
		if( dt1 == null) dt1 = new Date();
		SimpleDateFormat df1 = new SimpleDateFormat("2.16.840.1.113664.3.yyyyMMdd.HHmmss", Locale.US);
		return df1.format(dt1);
	}
	
	private static String generateUID6digits() {
		Integer rnd = (int)(Math.random()*1000000.);
		return rnd.toString();
	}
	
	
	
	/**
	 * Permet d'obtenir la 2ème partie du header qu'il faudra ajouter à la 1ere partie 
	 * @param CaptureFinale : L'ImagePlus de la capture secondaire (permet de récuperer le nombre de ligne et de colonne qui doit apparait dans le header DICOM)
	 * @return retourne la 2eme partie du tag qu'il faut ajouter à la 1ere partie (tag1+=tag2)
	 */
	public static String genererDicomTagsPartie2(ImagePlus CaptureFinale) {
		String tag = "0028,0010 Rows: " + CaptureFinale.getHeight() + "\n" 
					+ "0028,0011 Columns: " + CaptureFinale.getWidth() + "\n" 
					+ "0028,0100 Bits Allocated: 8" + "\n" + "0028,0101 Bits Stored: 8" + "\n" 
					+ "0028,0102 High Bit: 7" + "\n" 
					+ "0028,0103 Pixel Representation: 0 \n";
		return tag;
	}

	protected String[] buildCSVResultats() {

		String[] res2 = retour.clone();
		String[] res3 = new String[(res2.length )*2];
		for (int i = 0, j=0; i < res2.length ; i++,j++) {
			res3[j] = res2[i].split(":")[0];
			res3[j] = res3[j].trim();
			j++;
			res3[j] = res2[i].split(":")[1];
			res3[j] = res3[j].trim();
		}
		return res3;
	}

	// Permet la sauvegarde finale a partir du string builder contenant le
	// tableau de resultat, ROI manager, nom programme et imageplus finale pour
	// recuperer ID et date examen
	/**
	 * Permet de realiser l'export du fichier CSV et des ROI contenues dans l'export Manager vers le repertoire d'export defini dans les options
	 * @param resultats : Tableau contenant les resultats a exporter (doit contenir les titres de colonnes)
	 * @param nombreColonne : Nombre de colonne avant de passer à la seconde ligne (si 4 colonne mettre 4)
	 * @param Roi : le ROI manager utilise dans le programme
	 * @param nomProgramme : le nom du programme (sera utilise comme sous repertoire)
	 * @param imp : l'ImagePlus d'une image originale ou de la capture secondaire auquel on a ajoute le header, permet de recuperer le nom, l'ID et la date d'examen
	 * @throws FileNotFoundException : en cas d'erreur d'ecriture
	 */
	public static void exportAll(String[] resultats, int nombreColonne, RoiManager Roi, String nomProgramme, ImagePlus imp)
			throws FileNotFoundException {
		
		
		// On recupere le Patient Name de l'ImagePlus
		String patientName = new String();
		patientName = DicomTools.getTag(imp, "0010,0010");
		patientName = patientName.substring(1);
		// On eleve l'espace de la fin si il existe
		if (patientName.endsWith(" "))
		patientName = patientName.substring(0, patientName.length() - 1);

				
		// On recupere le Patient ID de l'ImagePlus
		String patientID = new String();
		patientID = DicomTools.getTag(imp, "0010,0020");
		patientID = patientID.substring(1);
		// On eleve l'espace de la fin si il existe
		if (patientID.endsWith(" "))
			patientID = patientID.substring(0, patientID.length() - 1);

		// On recupere la date d'examen
		String date = new String();
		date = DicomTools.getTag(imp, "0008,0020");
		date = date.substring(1);
		// On enleve l'espace de la fin si il existe
		if (date.endsWith(" "))
			date = date.substring(0, date.length() - 1);

		//Realisation du string builder qui sera ecrit en CSV
		StringBuilder content = new StringBuilder();
		//Ajout titre colonne
		content.append("Patient's Name");
		content.append(',');
		content.append("Patient's ID");
		content.append(',');
		content.append("Study Date");
		content.append('\n');
		// Ajouts des valeurs
		content.append(patientName);
		content.append(',');
		content.append(patientID);
		content.append(',');
		content.append(date);
		for (int i = 0; i < resultats.length; i++) {
			// Si multiple de n (nombre de valeur par ligne) on fait retour Ã  la
			// ligne sinon on met une virgule
			if (i % nombreColonne == 0) {
				content.append('\n');
			} else {
				content.append(',');
			}
			content.append(resultats[i]);
		}
		content.append('\n');
	

	//On recupere le path de sauvegarde
	String path = Prefs.get("dir.preferred", null);
	Boolean testEcriture=false;
	
	// On verifie que le path est writable si il existe
	if(path!=null)
	{
		File testPath = new File(path);
		testEcriture = testPath.canWrite();
	}

	if(path!=null&&testEcriture==false)
	{
		// Si pas de repertoire defini on notifie l'utilisateur
		IJ.showMessage("CSV Path not writable, CSV/ZIP export has failed");
	}if(path!=null&&testEcriture==true)
	{
		// On construit le sous repertoire avecle nom du programme et l'ID du
		// Patient
		String pathFinal = path + File.separator + nomProgramme + File.separator + patientID;
		File subDirectory = new File(pathFinal);
		subDirectory.mkdirs();

		File f = new File(subDirectory + File.separator + patientID + "_" + date + ".csv");

		// On ecrit les CSV
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
			pw.write(content.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}

		// On ecrit le ZIP contenant la sauvegarde des ROIs
		Roi[] rois2 = Roi.getRoisAsArray();
		int[] tab = new int[rois2.length];
		for (int i = 0; i < rois2.length; i++)
			tab[i] = i;
		Roi.setSelectedIndexes(tab);
		Roi.runCommand("Save", pathFinal.toString() + File.separator + patientID + "_" + date + ".zip");

	}
}
	/**
	 * Permet d'exporter le ROI manager uniquement dans un zip contenant les ROI (dans le cadre d'un logiciel ne generant pas de resultat utile a sauver qui seront traités par un autre logiciel par exemple)
	 * @param Roi : Le ROI manager utilise dans le programme
	 * @param nomProgramme : Le nom du programme (creation d'un sous repertoire)
	 * @param imp : Une ImagePlus originale ou de capture secondaire avec le header pour recuperer nom, ID, date d'examen.
	 */
	public static void exportRoiManager(RoiManager Roi, String nomProgramme, ImagePlus imp) {

		// On recupere le Patient ID de l'ImagePlus
		String patientID = new String();
		patientID = DicomTools.getTag(imp, "0010,0020");
		patientID = patientID.substring(1);
		// On eleve l'espace de la fin si il existe
		if (patientID.endsWith(" "))
			patientID = patientID.substring(0, patientID.length() - 1);

		// On recupere la date d'examen
		String date = new String();
		date = DicomTools.getTag(imp, "0008,0020");
		date = date.substring(1);
		// On enleve l'espace de la fin si il existe
		if (date.endsWith(" "))
			date = date.substring(0, date.length() - 1);

		//On recupere le path de sauvegarde
		String path = Prefs.get("dir.preferred", null);
		Boolean testEcriture=false;
	
		// On verifie que le path est writable si il existe
		if(path!=null)
		{
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if(path!=null&&testEcriture==false)
		{
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("Path not writable, CSV/ZIP export has failed");
		}
	
		if(path!=null&&testEcriture==true)
		{
			// On construit le sous repertoire avecle nom du programme et l'ID du
			// Patient
			String pathFinal = path + File.separator + nomProgramme + File.separator + patientID;
			File subDirectory = new File(pathFinal);
			subDirectory.mkdirs();

			// On ecrit le ZIP contenant la sauvegarde des ROIs
			Roi[] rois2 = Roi.getRoisAsArray();
			int[] tab = new int[rois2.length];
			for (int i = 0; i < rois2.length; i++)
				tab[i] = i;
			Roi.setSelectedIndexes(tab);
			Roi.runCommand("Save", pathFinal.toString() + File.separator + patientID + "_" + date + ".zip");

		}
	}

}
