package org.petctviewer.scintigraphy.scin;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

public abstract class ModeleScin {
	
	protected ImagePlus imp;

	public static double moyGeom(Double a, Double b) {
		return Math.sqrt(a * b);
	}
	
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * keys : id nom date
	 * 
	 * @param imp
	 * @return
	 */
	public static HashMap<String, String> getDicomInfo(ImagePlus imp) {
		HashMap<String, String> hm = new HashMap<String, String>();
		String nom = DicomTools.getTag(imp, "0010,0010").trim();
		hm.put("nom", nom.replace("^", " "));

		hm.put("id", DicomTools.getTag(imp, "0010,0020").trim());

		String dateStr = DicomTools.getTag(imp, "0008,0022").trim();
		Date result = null;
		try {
			result = new SimpleDateFormat("yyyymmdd").parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String r = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(result);

		hm.put("date", r);
		return hm;
	}

	
	/**
	 * Renvoie le nombre de coups sur la roi presente dans l'image plus
	 * @param imp
	 * @return
	 */
	public Double getCounts(ImagePlus imp) {
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer.setMeasurement(Measurements.MEAN, true);
		Analyzer analyser = new Analyzer(imp);
		analyser.measure();
		ResultsTable density = Analyzer.getResultsTable();
		return density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
	}
	
	/**
	 * Enregistrer la mesure de la roi courante de l'image plus dans le format souhait�
	 * @param nomRoi nom de la roi presente sur l'image plus
	 * @param imp ImagePlus a traiter
	 */
	public abstract void enregisterMesure(String nomRoi, ImagePlus imp);

	/**
	 * Permet de creer un stack a partir d'un tableau d'ImagePlus
	 * 
	 * @param tableauImagePlus
	 *            : Tableau contenant les ImagePlus a mettre dans le stack (toutes
	 *            les images doivent avoir la m�me taille)
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

	/**
	 * Capture secondaire de l'image sans l'interface et la redimmensionner � la
	 * taille voulue
	 * 
	 * @param imp
	 *            : ImagePlus a capturer
	 * @param largeur
	 *            : largeur de la capture finale (si hauteur et largeur = 0 : pas de
	 *            redimensionnement)
	 * @param hauteur
	 *            : hauteur de la capture finale (si hauteur =0 on ne redimensionne
	 *            que la largeur en gardant le ratio)
	 * @return Renvoie l'ImagePlus contenant la capture secondaire
	 */
	public static ImagePlus captureImage(ImagePlus imp, int largeur, int hauteur) {
		// Cette methode capture la partie image seule d'une fenetre
		ImageWindow win = imp.getWindow();
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
		if (hauteur == 0 && largeur != 0) {
			ip2 = ip.resize(largeur);
		} else if (hauteur == 0 && largeur == 0) {
			ip2 = ip;
		} else {
			ip2 = ip.resize(largeur, hauteur, true);
		}
		imp2.setProcessor(ip2);
		// On renvoie l'ImagePlus contenant la capture
		return imp2;
	}

	/**
	 * Permet de capturer la fenetre entiere et de choisir la taille de l'image
	 * finale
	 * 
	 * @param imp
	 *            : l'ImagePlus de la fenetre � capturer
	 * @param largeur
	 *            : largeur de l'image finale si largeur et hauteur =0 pas de resize
	 *            on a la meme resolution que l'ecran
	 * @param hauteur
	 *            : hauteur de l'image finale si hauteur =0 on ne resize que la
	 *            largeur en gardant le m�me ratio
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
		if (hauteur == 0 && largeur != 0) {
			ip2 = ip.resize(largeur);
		} else if (hauteur == 0 && largeur == 0) {
			ip2 = ip;
		} else {
			ip2 = ip.resize(largeur, hauteur, true);
		}
		imp2.setProcessor(ip2);
		return imp2;
	}

	private static String generateSOPInstanceUID(Date dt0) {
		Date dt1 = dt0;
		if (dt1 == null)
			dt1 = new Date();
		SimpleDateFormat df1 = new SimpleDateFormat("2.16.840.1.113664.3.yyyyMMdd.HHmmss", Locale.US);
		return df1.format(dt1);
	}

	private static String generateUID6digits() {
		Integer rnd = (int) (Math.random() * 1000000.);
		return rnd.toString();
	}
	
	public static Date getDateAcquisition(ImagePlus imp) {// Parse de la date et heure d'acquisition
		String aquisitionDate = DicomTools.getTag(imp, "0008,0022");
		String aquisitionTime = DicomTools.getTag(imp, "0008,0032");
		String dateInput = aquisitionDate.trim() + aquisitionTime.trim();
		// On enleve les milisec qui sont inconstantes
		int separateurPoint = dateInput.indexOf(".");
		if (separateurPoint != -1)
			dateInput = dateInput.substring(0, separateurPoint);

		SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dateAcquisition = null;
		try {
			dateAcquisition = parser.parse(dateInput);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateAcquisition;
	}

	/**
	 * Permet de generer la 1ere partie du Header qui servira a la capture finale
	 * 
	 * @param imp
	 *            : imageplus originale (pour recuperer des elements du Header tels
	 *            que le nom du patient...)
	 * @param nomProgramme
	 *            : nom du programme qui l'utilise si par exemple "pulmonary shunt"
	 *            la capture sera appelee "Capture Pulmonary Shunt"
	 * @return retourne la premi�re partie du header en string auquelle on ajoutera
	 *         la 2eme partie via la deuxieme methode
	 */
	public static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme) {
		String sopID = generateSOPInstanceUID(new Date());
		String uid = generateUID6digits();
		String tag = "0002,0002 Media Storage SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0002,0003 Media Storage SOP Inst UID: " + sopID + "\n" + "0002,0010 Transfer Syntax UID: "
				+ "1.2.840.10008.1.2.1" + "\n" + "0002,0013 Implementation Version Name: jpeg" + "\n"
				+ "0002,0016 Source Application Entity Title: " + "\n" + "0008,0008 Image Type: DERIVED\\SECONDARY "
				+ "\n" + "0008,0016 SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0008,0018 SOP Instance UID: " + sopID + "\n" + "0008,0020 Study Date:"
				+ DicomTools.getTag(imp, "0008,0020") + "\n" + "0008,0021 Series Date:"
				+ DicomTools.getTag(imp, "0008,0021") + "\n" + "0008,0030 Study Time:"
				+ DicomTools.getTag(imp, "0008,0030") + "\n" + "0008,0031 Series Time:"
				+ DicomTools.getTag(imp, "0008,0031") + "\n";
		if (DicomTools.getTag(imp, "0008,0050") != null)
			tag += "0008,0050 Accession Number:" + DicomTools.getTag(imp, "0008,0050") + "\n";
		if (DicomTools.getTag(imp, "0008,0060") != null)
			tag += "0008,0060 Modality:" + DicomTools.getTag(imp, "0008,0060") + "\n";
		tag += "0008,0064 Conversion Type: WSD" + "\n" + "0008,0070 Manufacturer:" + DicomTools.getTag(imp, "0008,0070")
				+ "\n";
		if (DicomTools.getTag(imp, "0008,0080") != null)
			tag += "0008,0080 Institution Name:" + DicomTools.getTag(imp, "0008,0080") + "\n";
		if (DicomTools.getTag(imp, "0008,0090") != null)
			tag += "0008,0090 Referring Physician's Name:" + DicomTools.getTag(imp, "0008,0090") + "\n";
		if (DicomTools.getTag(imp, "0008,1030") != null)
			tag += "0008,1030 Study Description:" + DicomTools.getTag(imp, "0008,1030") + "\n";
		tag += "0008,103E Series Description: Capture " + nomProgramme + "\n" + "0010,0010 Patient's Name:"
				+ DicomTools.getTag(imp, "0010,0010") + "\n" + "0010,0020 Patient ID:"
				+ DicomTools.getTag(imp, "0010,0020") + "\n";
		if (DicomTools.getTag(imp, "0010,0030") != null)
			tag += "0010,0030 Patient's Birth Date:" + DicomTools.getTag(imp, "0010,0030") + "\n";
		if (DicomTools.getTag(imp, "0010,0040") != null)
			tag += "0010,0040 Patient's Sex:" + DicomTools.getTag(imp, "0010,0040") + "\n";
		tag += "0020,000D Study Instance UID:" + DicomTools.getTag(imp, "0020,000D") + "\n"
				+ "0020,000E Series Instance UID:"
				+ DicomTools.getTag(imp, "0020,000E").substring(0, DicomTools.getTag(imp, "0020,000E").length() - 6)
				+ uid + "\n";
		if (DicomTools.getTag(imp, "0020,0010") != null)
			tag += "0020,0010 Study ID :" + DicomTools.getTag(imp, "0020,0010") + "\n";
		tag += "0020,0011 Series Number: 1337" + "\n" + "0020,0013 Instance Number: 1" + "\n"
				+ "0020,0032 Image Position (Patient):" + DicomTools.getTag(imp, "0020,0032") + "\n"
				+ "0020,0037 Image Orientation (Patient):" + DicomTools.getTag(imp, "0020,0037") + "\n"
				+ "0028,0002 Samples per Pixel: 3" + "\n" + "0028,0004 Photometric Interpretation: RGB" + "\n"
				+ "0028,0006 Planar Configuration: 0" + "\n" + "0028,0008 Number of Frames: 1 \n";
		return tag;
	}

	/**
	 * Permet d'obtenir la 2�me partie du header qu'il faudra ajouter � la 1ere
	 * partie
	 * 
	 * @param CaptureFinale
	 *            : L'ImagePlus de la capture secondaire (permet de r�cuperer le
	 *            nombre de ligne et de colonne qui doit apparait dans le header
	 *            DICOM)
	 * @return retourne la 2eme partie du tag qu'il faut ajouter � la 1ere partie
	 *         (tag1+=tag2)
	 */
	public static String genererDicomTagsPartie2(ImagePlus CaptureFinale) {
		String tag = "0028,0010 Rows: " + CaptureFinale.getHeight() + "\n" + "0028,0011 Columns: "
				+ CaptureFinale.getWidth() + "\n" + "0028,0100 Bits Allocated: 8" + "\n" + "0028,0101 Bits Stored: 8"
				+ "\n" + "0028,0102 High Bit: 7" + "\n" + "0028,0103 Pixel Representation: 0 \n";
		return tag;
	}
	
	//[0] : nom, [1] : id, [2] : date
	private static String[] getInfoPatient(ImagePlus imp) {
		String[] infoPatient = new String[3];
		
		// On recupere le Patient Name de l'ImagePlus
		String patientName = new String();
		patientName = DicomTools.getTag(imp, "0010,0010");
		if (patientName != null && !patientName.isEmpty())
			patientName = patientName.trim();

		// On recupere le Patient ID de l'ImagePlus
		String patientID = new String();
		patientID = DicomTools.getTag(imp, "0010,0020");
		if (patientID != null && !patientID.isEmpty())
			patientID = patientID.trim();

		// On recupere la date d'examen
		String date = new String();
		date = DicomTools.getTag(imp, "0008,0020");
		if (date != null && !date.isEmpty())
			date = date.trim();
		
		infoPatient[0] = patientName;
		infoPatient[1] = patientID;
		infoPatient[2] = date;
		
		return infoPatient;
	}

	private static StringBuilder initCSVHorizontal(String[] infoPatient) {
		// Realisation du string builder qui sera ecrit en CSV
		StringBuilder content = new StringBuilder();
		// Ajout titre colonne
		content.append("Patient's Name");
		content.append(',');
		content.append("Patient's ID");
		content.append(',');
		content.append("Study Date");
		content.append('\n');
		// Ajouts des valeurs
		content.append(infoPatient[0]);
		content.append(',');
		content.append(infoPatient[1]);
		content.append(',');
		content.append(infoPatient[2]);
		
		return content;
	}
	
	private static StringBuilder initCSVVertical(String[] infoPatient) {
		// Realisation du string builder qui sera ecrit en CSV
		StringBuilder content = new StringBuilder();
		// Ajout titre colonne
		content.append("Patient's Name");
		content.append(',');
		content.append(infoPatient[0]);
		content.append('\n');
		
		content.append("Patient's ID");
		content.append(',');
		content.append(infoPatient[1]);
		content.append('\n');
		
		content.append("Study Date");
		content.append(',');
		content.append(infoPatient[2]);
		content.append('\n');
		
		return content;
	}
	
	private static void saveFiles(ImagePlus imp, RoiManager roiManager, StringBuilder csv, String nomProgramme, String[] infoPatient) {

		StringBuilder content = csv;
		
		// On recupere le path de sauvegarde
		String path = Prefs.get("dir.preferred", null);
		Boolean testEcriture = false;

		// On verifie que le path est writable si il existe
		if (path != null) {
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if (path != null && testEcriture == false) {
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("CSV Path not writable, CSV/ZIP export has failed");
		}
		if (path != null && testEcriture == true) {
			// On construit le sous repertoire avecle nom du programme et l'ID du
			// Patient
			String pathFinal = path + File.separator + nomProgramme + File.separator + infoPatient[1];
			File subDirectory = new File(pathFinal);
			subDirectory.mkdirs();

			String nomFichier = infoPatient[1] + "_" + infoPatient[2];
			
			File f = new File(subDirectory + File.separator + nomFichier + ".csv");

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
			Roi[] rois2 = roiManager.getRoisAsArray();
			int[] tab = new int[rois2.length];
			for (int i = 0; i < rois2.length; i++)
				tab[i] = i;
			roiManager.setSelectedIndexes(tab);
			roiManager.runCommand("Save", pathFinal.toString() + File.separator + nomFichier + ".zip");

			// On sauve l'image en jpeg
			IJ.saveAs(imp, "Jpeg", pathFinal.toString() + File.separator + nomFichier + ".jpg");

		}
	}
	
	// Permet la sauvegarde finale a partir du string builder contenant le
	// tableau de resultat, ROI manager, nom programme et imageplus finale pour
	// recuperer ID et date examen
	/**
	 * Permet de realiser l'export du fichier CSV et des ROI contenues dans l'export
	 * Manager vers le repertoire d'export defini dans les options
	 * 
	 * @param resultats
	 *            : Tableau contenant les resultats a exporter (doit contenir les
	 *            titres de colonnes)
	 * @param nombreColonne
	 *            : Nombre de colonne avant de passer � la seconde ligne (si 4
	 *            colonne mettre 4)
	 * @param roiManager
	 *            : le ROI manager utilise dans le programme
	 * @param nomProgramme
	 *            : le nom du programme (sera utilise comme sous repertoire)
	 * @param imp
	 *            : l'ImagePlus d'une image originale ou de la capture secondaire
	 *            auquel on a ajoute le header, permet de recuperer le nom, l'ID et
	 *            la date d'examen
	 * @throws FileNotFoundException
	 *             : en cas d'erreur d'ecriture
	 */
	public static void exportAll(String[] resultats, int nombreColonne, RoiManager roiManager, String nomProgramme,
			ImagePlus imp) throws FileNotFoundException {

		String[] infoPatient = ModeleScin.getInfoPatient(imp);
		StringBuilder content = ModeleScin.initCSVHorizontal(infoPatient);
		
		for (int i = 0; i < resultats.length; i++) {
			// Si multiple de n (nombre de valeur par ligne) on fait retour à la ligne sinon on met une virgule
			if (i % nombreColonne == 0) {
				content.append('\n');
			} else {
				content.append(',');
			}
			content.append(resultats[i]);
		}
		content.append('\n');
		
		saveFiles(imp, roiManager, content, nomProgramme, infoPatient);
	}
	
	/**
	 * Permet de realiser l'export du fichier CSV et des ROI contenues dans l'export
	 * Manager vers le repertoire d'export defini dans les options
	 * 
	 * @param resultats
	 *            : Resultats a exporter (utiliser le format csv)
	 * @param roiManager
	 *            : le ROI manager utilise dans le programme
	 * @param nomProgramme
	 *            : le nom du programme (sera utilise comme sous repertoire)
	 * @param imp
	 *            : l'ImagePlus d'une image originale ou de la capture secondaire
	 *            auquel on a ajoute le header, permet de recuperer le nom, l'ID et
	 *            la date d'examen
	 * @throws FileNotFoundException
	 *             : en cas d'erreur d'ecriture
	 */
	public static void exportAll(String resultats, RoiManager roiManager, String nomProgramme,
			ImagePlus imp) throws FileNotFoundException {

		String[] infoPatient = ModeleScin.getInfoPatient(imp);
		StringBuilder content = initCSVVertical(infoPatient);
		
		content.append(resultats);
		
		saveFiles(imp, roiManager, content, nomProgramme, infoPatient);
	}

	/**
	 * Permet d'exporter le ROI manager uniquement dans un zip contenant les ROI
	 * (dans le cadre d'un logiciel ne generant pas de resultat utile a sauver qui
	 * seront trait�s par un autre logiciel par exemple)
	 * 
	 * @param Roi
	 *            : Le ROI manager utilise dans le programme
	 * @param nomProgramme
	 *            : Le nom du programme (creation d'un sous repertoire)
	 * @param imp
	 *            : Une ImagePlus originale ou de capture secondaire avec le header
	 *            pour recuperer nom, ID, date d'examen.
	 */
	public static void exportRoiManager(RoiManager Roi, String nomProgramme, ImagePlus imp) {

		// On recupere le Patient ID de l'ImagePlus
		String patientID = new String();
		patientID = DicomTools.getTag(imp, "0010,0020");
		if (patientID != null && !patientID.isEmpty())
			patientID = patientID.trim();

		// On recupere la date d'examen
		String date = new String();
		date = DicomTools.getTag(imp, "0008,0020");
		if (date != null && !date.isEmpty())
			date = date.trim();

		// On recupere le path de sauvegarde
		String path = Prefs.get("dir.preferred", null);
		Boolean testEcriture = false;

		// On verifie que le path est writable si il existe
		if (path != null) {
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if (path != null && testEcriture == false) {
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("Path not writable, CSV/ZIP export has failed");
		}

		if (path != null && testEcriture == true) {
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

	public double getDecayFraction(int delaySeconds, int halLifeSeconds) {
		double tcLambdaSeconds = (Math.log(2) / (halLifeSeconds));
		double decayedFraction = Math.pow(Math.E, (tcLambdaSeconds * delaySeconds * (-1)));
		// Decayed fraction est la fraction de la radioactivit� qui a disparu
		// Pour avoir les coups corrige de la decroissance
		// countsCorrected=counts/decayedFraction
		return decayedFraction;
	}

	public abstract void calculerResultats();

	public abstract HashMap<String, String> getResultsHashMap();

}