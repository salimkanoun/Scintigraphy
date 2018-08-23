package org.petctviewer.scintigraphy.scin;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.LutLoader;
import ij.plugin.MontageMaker;
import ij.plugin.ZProjector;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.StackProcessor;
import ij.util.DicomTools;

public class StaticMethod {

	/********** Public Static Getter ********/
	/**
	 * Renvoie le nombre de coups sur la roi presente dans l'image plus
	 * 
	 * @param imp
	 *            l'imp
	 * @return nombre de coups
	 */
	public static Double getCounts(ImagePlus imp) {
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer.setMeasurement(Measurements.MEAN, true);
		Analyzer analyser = new Analyzer(imp);
		analyser.measure();
		ResultsTable density = Analyzer.getResultsTable();
		return density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
	}

	
	/**
	 * renvoie le nombre de coups moyens de la roi presente sur l'imp
	 * 
	 * @param imp
	 *            l'imp
	 * @return nombre moyen de coups
	 */
	public static Double getAvgCounts(ImagePlus imp) {
		int area = imp.getStatistics().pixelCount;
		return getCounts(imp) / area;
	}


	
	/**
	 * renvoie une hasmap contenant les informations du patient selon le tag info de
	 * l'imp keys : id name date
	 * 
	 * @param imp
	 * @return
	 */
	public static HashMap<String, String> getPatientInfo(ImagePlus imp) {
		HashMap<String, String> hm = new HashMap<>();
	
		// ajout du nom, si il n'existe pas on ajoute une string vide
		if (DicomTools.getTag(imp, "0010,0010") != null) {
			String nom = DicomTools.getTag(imp, "0010,0010").trim();
			hm.put("name", nom.replace("^", " "));
		} else {
			hm.put("name", "");
		}
	
		// ajout de l'id, si il n'existe pas on ajoute une string vide
		if (DicomTools.getTag(imp, "0010,0020") != null) {
			hm.put("id", DicomTools.getTag(imp, "0010,0020").trim());
		} else {
			hm.put("id", "");
		}
	
		// ajout de la date nom, si il n'existe pas on ajoute une string vide
		if (DicomTools.getTag(imp, "0008,0022") != null) {
			String dateStr = DicomTools.getTag(imp, "0008,0022").trim();
			Date result = null;
			try {
				result = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			String r = new SimpleDateFormat(Prefs.get("dateformat.preferred", "MM/dd/yyyy")).format(result);
			hm.put("date", r);
	
		} else {
			hm.put("date", "");
		}
		return hm;
	}


	// Parse de la date et heure d'acquisition
	
	public static Date getDateAcquisition(ImagePlus imp) {
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


	/**prepare la premiere partie des tags du header du dicom avec l'iud passe en parametre <br>
	 * <br>
	 * See also :
	 * <br>{@link StaticMethod#genererDicomTagsPartie1(ImagePlus, String)} <br> {@link ModeleScin#genererDicomTagsPartie1SameUID(ImagePlus, String)}
	 * @param imp
	 * @param nomProgramme
	 * @param uid
	 * @return
	 */
	public static String getTagPartie1(HashMap tags, String nomProgramme, String uid) {
		String sopID = StaticMethod.generateSOPInstanceUID(new Date());
		String tag = "0002,0002 Media Storage SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0002,0003 Media Storage SOP Inst UID: " + sopID + "\n" 
				+ "0002,0010 Transfer Syntax UID: "
				+ "1.2.840.10008.1.2.1" + "\n" + "0002,0013 Implementation Version Name: jpeg" + "\n"
				+ "0002,0016 Source Application Entity Title: " + "\n" 
				+ "0008,0008 Image Type: DERIVED\\SECONDARY "
				+ "\n" + "0008,0016 SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0008,0018 SOP Instance UID: " + sopID + "\n" + "0008,0020 Study Date:"
				+ tags.get("0008,0020") + "\n" + "0008,0021 Series Date:"
				+ tags.get("0008,0021") + "\n" + "0008,0030 Study Time:"
				+ tags.get("0008,0030") + "\n" + "0008,0031 Series Time:"
				+ tags.get("0008,0031") + "\n";
		if (tags.get("0008,0050") != null)
			tag += "0008,0050 Accession Number:" + tags.get("0008,0050") + "\n";
		if (tags.get("0008,0060") != null)
			tag += "0008,0060 Modality:" + tags.get("0008,0060") + "\n";
		tag += "0008,0064 Conversion Type: WSD" + "\n" + "0008,0070 Manufacturer:" + tags.get("0008,0070")
				+ "\n";
		if (tags.get("0008,0080") != null)
			tag += "0008,0080 Institution Name:" + tags.get("0008,0080") + "\n";
		if (tags.get("0008,0090") != null)
			tag += "0008,0090 Referring Physician's Name:" + tags.get("0008,0090") + "\n";
		if (tags.get("0008,1030") != null)
			tag += "0008,1030 Study Description:" + tags.get("0008,1030") + "\n";
		tag += "0008,103E Series Description: Capture " + nomProgramme + "\n" + "0010,0010 Patient's Name:"
				+ tags.get("0010,0010") + "\n" + "0010,0020 Patient ID:"
				+ tags.get("0010,0020") + "\n";
		if (tags.get("0010,0030") != null)
			tag += "0010,0030 Patient's Birth Date:" + tags.get("0010,0030") + "\n";
		if (tags.get("0010,0040") != null)
			tag += "0010,0040 Patient's Sex:" + tags.get("0010,0040") + "\n";
		tag += "0020,000D Study Instance UID:" + tags.get("0020,000D") + "\n"
				+ "0020,000E Series Instance UID:"
				+ ((String) tags.get("0020,000E")).substring(0, ((String) tags.get("0020,000E")).length() - 6)
				+ uid + "\n";
		if (tags.get("0020,0010") != null)
			tag += "0020,0010 Study ID :" + tags.get("0020,0010") + "\n";
		tag += "0020,0011 Series Number: 1337" + "\n" + "0020,0013 Instance Number: 1" + "\n"
				+ "0020,0032 Image Position (Patient):" + tags.get("0020,0032") + "\n"
				+ "0020,0037 Image Orientation (Patient):" + tags.get("0020,0037") + "\n"
				+ "0028,0002 Samples per Pixel: 3" + "\n" + "0028,0004 Photometric Interpretation: RGB" + "\n"
				+ "0028,0006 Planar Configuration: 0" + "\n" + "0028,0008 Number of Frames: 1 \n";
		return tag;
	}


	/********** Public Static  ********/
	/**
	 * renvoie la moyenne geometrique
	 * 
	 * @param a
	 *            chiffre a
	 * @param b
	 *            chiffre b
	 * @return moyenne geometrique
	 */
	public static double moyGeom(Double a, Double b) {
		return Math.sqrt(a * b);
	}


	/**
	 * arrondi la valeur
	 * 
	 * @param value
	 *            valeur a arrondir
	 * @param places
	 *            nb de chiffre apres la virgule
	 * @return valeur arrondie
	 */
	public static double round(Double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException("place doit etre superieur ou egal a zero");
		}
		
		if(value.equals(Double.NaN) || value .equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY)) {
			return value;
		}
	
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}


	/**
	 * Permet de creer un stack a partir d'un tableau d'ImagePlus *
	 * 
	 * @param tableauImagePlus
	 *            : Tableau contenant les ImagePlus a mettre dans le stack (toutes
	 *            les images doivent avoir la m�me taille)
	 * @return Renvoie le stack d'image produit
	 */
	public static ImageStack captureToStack(ImagePlus[] tableauImagePlus) {
		// On verifie que toutes les images ont la meme taille
		int[][] dimensionCapture = new int[tableauImagePlus.length][2];
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
			e.printStackTrace();
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
			e.printStackTrace();
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


	/**
	 * Permet de generer la 1ere partie du Header qui servira a la capture finale,
	 * l'iud est genere aleatoirement a chauqe appel de la fonction
	 * 
	 * @param imp
	 *            : imageplus originale (pour recuperer des elements du Header tels
	 *            que le nom du patient...)
	 * @param nomProgramme
	 *            : nom du programme qui l'utilise si par exemple "pulmonary shunt"
	 *            la capture sera appelee "Capture Pulmonary Shunt"
	 * @return retourne la premi�re partie du header en string auquelle on
	 *         ajoutera la 2eme partie via la deuxieme methode
	 */
	public static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme) {
		Random random = new Random();
		String uid = Integer.toString(random.nextInt(1000000));
		
		return StaticMethod.genererDicomTagsPartie1(imp, nomProgramme, uid);
	}


	static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme, String uid) {
		HashMap tags=new HashMap();
		tags.put("0008,0020", DicomTools.getTag(imp, "0008,0020") );
		tags.put("0008,0021", DicomTools.getTag(imp, "0008,0021") );
		tags.put("0008,0030", DicomTools.getTag(imp, "0008,0030") );
		tags.put("0008,0031", DicomTools.getTag(imp, "0008,0031") );
		tags.put("0008,0050", DicomTools.getTag(imp, "0008,0050") );
		tags.put("0008,0060", DicomTools.getTag(imp, "0008,0060") );
		tags.put("0008,0070", DicomTools.getTag(imp, "0008,0070") );
		tags.put("0008,0080", DicomTools.getTag(imp, "0008,0080") );
		tags.put("0008,0090", DicomTools.getTag(imp, "0008,0090") );
		tags.put("0008,1030", DicomTools.getTag(imp, "0008,1030") );
		tags.put("0010,0010", DicomTools.getTag(imp, "0010,0010") );
		tags.put("0010,0020", DicomTools.getTag(imp, "0010,0020") );
		tags.put("0010,0030", DicomTools.getTag(imp, "0010,0030") );
		tags.put("0010,0040", DicomTools.getTag(imp, "0010,0040") );
		tags.put("0020,000D", DicomTools.getTag(imp, "0020,000D") );
		tags.put("0020,000E", DicomTools.getTag(imp, "0020,000E") );
		tags.put("0020,0010", DicomTools.getTag(imp, "0020,0010") );
		tags.put("0020,0032" ,DicomTools.getTag(imp, "0020,0032") );
		tags.put("0020,0037", DicomTools.getTag(imp, "0020,0037") );
		String partie1=getTagPartie1(tags, nomProgramme, uid);
		return partie1;
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
	
		String[] infoPatient = StaticMethod.getInfoPatient(imp);
		StringBuilder content = StaticMethod.initCSVHorizontal(infoPatient);
	
		for (int i = 0; i < resultats.length; i++) {
			// Si multiple de n (nombre de valeur par ligne) on fait retour à la ligne
			// sinon
			// on met une virgule
			if (i % nombreColonne == 0) {
				content.append('\n');
			} else {
				content.append(',');
			}
			content.append(resultats[i]);
		}
		content.append('\n');
	
		StaticMethod.saveFiles(imp, roiManager, content, nomProgramme, infoPatient, "");
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
	public static void exportAll(String resultats, RoiManager roiManager, String nomProgramme, ImagePlus imp)
			throws FileNotFoundException {
	
		String[] infoPatient = StaticMethod.getInfoPatient(imp);
		StringBuilder content = StaticMethod.initCSVVertical(infoPatient);
	
		content.append(resultats);
	
		StaticMethod.saveFiles(imp, roiManager, content, nomProgramme, infoPatient, "");
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
	 * @param additionalInfo
	 *            :String qui sera rajoutée à la fin du nom du fichier
	 * @throws FileNotFoundException
	 *             : en cas d'erreur d'ecriture
	 */
	public static void exportAll(String resultats, RoiManager roiManager, String nomProgramme, ImagePlus imp,
			String additionalInfo) throws FileNotFoundException {
	
		String[] infoPatient = StaticMethod.getInfoPatient(imp);
		StringBuilder content = StaticMethod.initCSVVertical(infoPatient);
	
		content.append(resultats);
	
		StaticMethod.saveFiles(imp, roiManager, content, nomProgramme, infoPatient, additionalInfo);
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


	/********** Private static *********/
	private static String generateSOPInstanceUID(Date dt0) {
		Date dt1 = dt0;
		if (dt1 == null)
			dt1 = new Date();
		SimpleDateFormat df1 = new SimpleDateFormat("2.16.840.1.113664.3.yyyyMMdd.HHmmss", Locale.US);
		return df1.format(dt1);
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


	/*
	 * @param additionalInfo
	 *            :String qui sera rajoutée à la fin du nom du fichier
	 */
	@SuppressWarnings("null")
	private static void saveFiles(ImagePlus imp, RoiManager roiManager, StringBuilder csv, String nomProgramme,
			String[] infoPatient, String additionalInfo) {
	
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
	
			String nomFichier = infoPatient[1] + "_" + infoPatient[2] + additionalInfo;
	
			File f = new File(subDirectory + File.separator + nomFichier + ".csv");
	
			// On ecrit les CSV
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(f);
				pw.write(content.toString());
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
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


	/********** Private static Getter*********/
	// [0] : nom, [1] : id, [2] : date
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


	/********************* Public Static ****************************************/
	
	/**
	 * Premet de trier un tableau d'ImagePlus par leur acquisition date et time de
	 * la plus ancienne � la plus recente
	 * 
	 * @param serie
	 *            : Tableau d'ImagePlus a trier
	 * @return Tableau d'ImagePlus ordonne par acquisition time
	 */
	public static ImagePlus[] orderImagesByAcquisitionTime(ArrayList<ImagePlus> serie) {
	
		ImagePlus[] retour = new ImagePlus[serie.size()];
		serie.toArray(retour);
	
		Arrays.sort(retour, new Comparator<ImagePlus>() {
	
			@Override
			public int compare(ImagePlus arg0, ImagePlus arg1) {
	
				DateFormat dateHeure = new SimpleDateFormat("yyyyMMddHHmmss");
				String dateImage0 = DicomTools.getTag(arg0, "0008,0022");
				String dateImage1 = DicomTools.getTag(arg1, "0008,0022");
				String heureImage0 = DicomTools.getTag(arg0, "0008,0032");
				String heureImage1 = DicomTools.getTag(arg1, "0008,0032");
	
				String dateInputImage0 = dateImage0.trim() + heureImage0.trim();
				// On split les millisecondes qui sont apr�s le . car nombre inconstant de
				// millisec
				int separateurMilliSec = dateInputImage0.indexOf(".");
				if (separateurMilliSec != -1)
					dateInputImage0 = dateInputImage0.substring(0, separateurMilliSec);
	
				String dateInputImage1 = dateImage1.trim() + heureImage1.trim();
				int separateurMilliSecImage1 = dateInputImage1.indexOf(".");
				if (separateurMilliSecImage1 != -1)
					dateInputImage1 = dateInputImage1.substring(0, separateurMilliSecImage1);
	
				Date timeImage0 = null;
				Date timeImage1 = null;
				try {
					timeImage0 = dateHeure.parse(dateInputImage0);
					timeImage1 = dateHeure.parse(dateInputImage1);
					return (int) ((timeImage0.getTime() - timeImage1.getTime()) / 1000);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
	
		return retour;
	}


	/**
	 * Permet de spliter les images d'un multiFrame contenant 2 camera, image 0
	 * camera Ant et Image1 Camera Post (ne retourne pas l'image post)
	 * 
	 * @param imp
	 *            : ImagePlus a traiter
	 * @return Tableau d'imagePlus avec 2 ImagePlus (camera 1 et 2 )
	 */
	public static ImagePlus[] splitCameraMultiFrame(ImagePlus imp) {
		// On prend le Header
		String metadata = imp.getInfoProperty();
	
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagDetecteur))
			tagDetecteur = tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDetecteur = tagDetecteur.split(delims);
	
		// On cree les ImageStack qui vont recevoir les image de chaque t�te
		ImageStack camera0 = new ImageStack(imp.getWidth(), imp.getHeight());
		ImageStack camera1 = new ImageStack(imp.getWidth(), imp.getHeight());
	
		// Determination de l'orientation des camera en regardant la 1ere image
		String detecteurPremiereImage = sequenceDetecteur[0];
		Boolean anterieurPremiereImage = StaticMethod.isAnterieurMultiframe(imp);
	
		// On ajoute les images dans les camera adhoc
	
		if (anterieurPremiereImage != null && anterieurPremiereImage) {
			for (int i = 0; i < sequenceDetecteur.length; i++) {
				if (sequenceDetecteur[i].equals(detecteurPremiereImage)) {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
		} else if (anterieurPremiereImage != null && !anterieurPremiereImage) {
			for (int i = 0; i < sequenceDetecteur.length; i++) {
				if (sequenceDetecteur[i].equals(detecteurPremiereImage)) {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
		} else {
			IJ.log("assuming image 2 is posterior. Please notify Salim.kanoun@gmail.com");
			for (int i = 0; i < sequenceDetecteur.length; i++) {
				if (sequenceDetecteur[i].equals("1")) {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else if (sequenceDetecteur[i].equals("2")) {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
		}
	
		ImagePlus cameraAnt = new ImagePlus();
		ImagePlus cameraPost = new ImagePlus();
		cameraAnt.setStack(camera0);
		cameraPost.setStack(camera1);
	
		ImagePlus[] cameras = new ImagePlus[2];
		cameras[0] = cameraAnt;
		cameras[1] = cameraPost;
	
		// On ajoute une copie des headers
		for (int i = 0; i < cameras.length; i++) {
			cameras[i].setProperty("Info", metadata);
		}
		return cameras;
	}


	public static ImagePlus[] openImps(String[] titresFenetres) {
		ImagePlus[] imps = new ImagePlus[titresFenetres.length];
		for (int i = 0; i < titresFenetres.length; i++) {
			ImagePlus imp = WindowManager.getImage(titresFenetres[i]);
			imps[i] = imp;
		}
		return imps;
	}


	public static void closeImps(String[] titresFenetres) {
		for (int i = 0; i < titresFenetres.length; i++) {
			WindowManager.getImage(titresFenetres[i]).close();
		}
	}


	public static void editLabelOverlay(Overlay ov, String oldName, String newName, Color c) {
		Roi roi = ov.get(ov.getIndex(oldName));
		if (roi != null) {
			roi.setName(newName);
			roi.setStrokeColor(c);
		}
	}


	/**
	 * Renvoie un montage avec un pas regulier
	 * 
	 * @param frameDuration
	 * @param imp
	 * @param size
	 * @return
	 */
	public static ImagePlus creerMontage(int[] frameDuration, ImagePlus imp, int size, int rows, int columns) {
		int nSlice = frameDuration.length;
	
		// temps somme
		int[] summed = new int[frameDuration.length];
		summed[0] = frameDuration[0];
		for (int i = 1; i < nSlice; i++) {
			summed[i] = summed[i - 1] + frameDuration[i];
		}
	
		// tableau correspondant au numero des coupes bornes
		int[] sliceIndex = new int[(rows * columns) + 1];
		int pas = summed[nSlice - 1] / (rows * columns);
		for (int i = 0; i < (rows * columns) + 1; i++) {
			for (int j = 0; j < summed.length; j++) {
				if (i * pas <= summed[j] || j == summed.length - 1) {
					sliceIndex[i] = j;
					break;
				}
			}
		}
	
		// liste des projections
		ImagePlus[] impList = new ImagePlus[rows * columns];
		for (int i = 1; i < sliceIndex.length; i++) {
			int start = sliceIndex[i - 1];
			int stop = sliceIndex[i];
			ImagePlus tinyImp = ZProjector.run(imp, "sum", start, stop);
	
			ImageProcessor impc = tinyImp.getProcessor();
			impc.setInterpolationMethod(ImageProcessor.BICUBIC);
			impc = impc.resize(size);
	
			ImagePlus projectionImp = new ImagePlus("", impc);
	
			impList[i - 1] = projectionImp;
		}
	
		// fait le montage
		Concatenator enchainer = new Concatenator();
		ImagePlus impStacked = enchainer.concatenate(impList, false);
	
		// on ajoute un label avec le temps en min
		for (int i = 1; i <= impStacked.getStackSize(); i++) {
			int msPassed = summed[sliceIndex[i - 1]];
			String min = "" + msPassed / 10000 + "s";
			impStacked.getStack().setSliceLabel(min, i);
		}
	
		MontageMaker mm = new MontageMaker();
	
		return mm.makeMontage2(impStacked, columns, rows, 1.0, 1, impList.length, 1, 0, true);
	}


	public static Overlay duplicateOverlay(Overlay overlay) {
		Overlay overlay2 = overlay.duplicate();
	
		overlay2.drawLabels(overlay.getDrawLabels());
		overlay2.drawNames(overlay.getDrawNames());
		overlay2.drawBackgrounds(overlay.getDrawBackgrounds());
		overlay2.setLabelColor(overlay.getLabelColor());
		overlay2.setLabelFont(overlay.getLabelFont(), overlay.scalableLabels());
	
		// theses properties are not set by the original duplicate method
		overlay2.setIsCalibrationBar(overlay.isCalibrationBar());
		overlay2.selectable(overlay.isSelectable());
	
		return overlay2;
	}


	/**
	 * Cree overlay et set la police initiale de l'Image
	 * 
	 * @return Overlay
	 */
	public static Overlay initOverlay(ImagePlus imp, int taillePolice) {
		int taille2;
		if (taillePolice != -1) {
			taille2 = taillePolice;
		} else {
			taille2 = 12;
		}
		// On initialise l'overlay il ne peut y avoir qu'un Overlay
		// pour tout le programme sur lequel on va ajouter/enlever les ROI au fur et a
		// mesure
		Overlay overlay = new Overlay();
		// On defini la police et la propriete des Overlays
		int width = imp.getWidth();
		// On normalise Taille 12 a 256 pour avoir une taille stable pour toute image
		Float facteurConversion = (float) ((width * 1.0) / 256);
		Font font = new Font("Arial", Font.PLAIN, Math.round(taille2 * facteurConversion));
		overlay.setLabelFont(font, true);
		overlay.drawLabels(true);
		overlay.drawNames(true);
		// Pour rendre overlay non selectionnable
		overlay.selectable(false);
	
		return overlay;
	}


	/**
	 * Cree overlay et set la police a la taille standard (12) initial de l'Image
	 * 
	 * @return Overlay
	 */
	public static Overlay initOverlay(ImagePlus imp) {
		return initOverlay(imp, -1);
	}


	public static final int HEART = 0, INFLAT = 1, KIDNEY = 2, INFLATGAUCHE = 3, INFLATDROIT = 4;
	
	// cree la roi de bruit de fond
	public static Roi createBkgRoi(Roi roi, ImagePlus imp, int organ) {
		Roi bkg = null;
		RoiManager rm = new RoiManager(true);
		rm.setVisible(true);
	
		switch (organ) {
		case StaticMethod.KIDNEY:
			// largeur a prendre autour du rein
			int largeurBkg = 1;
			if (imp.getDimensions()[0] >= 128) {
				largeurBkg = 2;
			}
	
			rm.addRoi(roi);
	
			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());
	
			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());
	
			rm.setSelectedIndexes(new int[] { rm.getCount() - 2, rm.getCount() - 1 });
			rm.runCommand(imp, "XOR");
	
			bkg = imp.getRoi();
			break;
	
		case StaticMethod.HEART:
			// TODO
			break;
	
		case StaticMethod.INFLATGAUCHE:
			bkg = StaticMethod.createBkgInfLat(roi, imp, -1, rm);
			break;
	
		case StaticMethod.INFLATDROIT:
			bkg = StaticMethod.createBkgInfLat(roi, imp, 1, rm);
			break;
	
		default:
			bkg = roi;
			break;
		}
	
		rm.dispose();
	
		bkg.setStrokeColor(Color.GRAY);
		return bkg;
	}


	/*
	 *inverse le stack de l'image plus passée en paramètre 
	 *(imp : donner l'implus ne contenant que la post)
	 */
	public static ImagePlus flipStackHorizontal(ImagePlus imp) {
		StackProcessor sp = new StackProcessor(imp.getImageStack());
		sp.flipHorizontal();
		return imp;
	}


	/********************* Public Static Is ****************************************/
	/** 
	 *  Test si les images du MutiFrame viennent toutes de la meme camera
	 * 
	 * @param imp
	 *            : ImagePlus � traiter
	 * @return Boolean
	 */
	public static boolean isSameCameraMultiFrame(ImagePlus imp) {
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagDetecteur))
			tagDetecteur = tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDetecteur = tagDetecteur.split(delims);
		boolean sameCamera = true;
	
		String premiereImage = sequenceDetecteur[0];
		for (int i = 1; i < sequenceDetecteur.length; i++) {
			if (!premiereImage.equals(sequenceDetecteur[i]))
				sameCamera = false;
			premiereImage = sequenceDetecteur[i];
		}
		return sameCamera;
	}


	/**
	 * Test si la premiere image du stack est du detecteur 1
	 * 
	 * @param imp
	 *            : ImagePus A traiter
	 * @return boolean
	 */
	public static boolean isPremiereImageDetecteur1(ImagePlus imp) {
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagDetecteur))
			tagDetecteur = tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDeteceur = tagDetecteur.split(delims);
		boolean detecteur1 = false;
	
		if (Integer.parseInt(sequenceDeteceur[0]) == 1)
			detecteur1 = true;
	
		return detecteur1;
	}


	/**
	 * Permet de tester si l'image est anterieure pour une unique frame, ne teste
	 * que la premi�re Image (peut etre generalisee plus tard si besoin) A Eviter
	 * d'utiliser car la methode isAnterieur(ImagePlus imp) est generique pour tout
	 * type d'image
	 * 
	 * @param imp
	 *            : ImagePlus a tester
	 * @return boolean vrai si anterieur
	 */
	@Deprecated
	public static Boolean isAnterieurUniqueFrame(ImagePlus imp) {
		imp.setSlice(1);
	
		// Recupere le private tag qui peut contenir des informations de localisation
		// (rangueil)
		String tag = DicomTools.getTag(imp, "0011,1012");
	
		// On repere le num de camera
		String tagVector = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagVector))
			tagVector = tagVector.trim();
	
		// On ajoute un deuxieme tag de localisation a voir dans la pratique ou se situe
		// l'info
		if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0011,1030")))
			tag += DicomTools.getTag(imp, "0011,1030");
		Boolean anterieur = null;
	
		if (!StringUtils.isEmpty(tagVector) || !StringUtils.isEmpty(tag)) {
	
			// Si on a le private tag on le traite
			if (!StringUtils.isEmpty(tag)) {
	
				if (tag.contains("ANT") || tag.contains("_E")) {
					anterieur = true;
				}
	
				else if (tag.contains("POS") || tag.contains("_F")) {
					anterieur = false;
				}
	
				else {
					IJ.log("Orientation not reckognized");
				}
			}
	
			// Si pas de private tag on fait avec le numero de la camera
			else if (!StringUtils.isEmpty(tagVector)) {
	
				if (imp.getStackSize() == 2) {
					// SK FAUDRA RECONNAITRE LES IMAGE D/G ET LES DIFFERENCIER //Utilisation de
					// l'angle ??
					if (tagVector.equals("1"))
						anterieur = true;
					if (tagVector.equals("2"))
						anterieur = false;
					IJ.log("Orientation Not reckgnized, assuming vector 1 is anterior");
				}
				// le Boolean reste null et on informe l'user
				else {
					IJ.log("Orientation not reckognized");
				}
			}
	
		}
	
		// Si aucun des deux echec du reperage
		else {
			IJ.log("Orientation not reckognized");
		}
	
		return anterieur;
	}


	/**
	 * Permet de tester si l'image est anterieure pour une MultiFrame, ne teste que
	 * la premi�re Image (peut etre generalisee plus tard si besoin) A Eviter
	 * d'utiliser car la methode isAnterieur(ImagePlus imp) est generique pour tout
	 * type d'image
	 * 
	 * @param imp
	 *            : ImagePlus a tester
	 * @return boolean vrai si anterieur
	 */
	@Deprecated
	public static Boolean isAnterieurMultiframe(ImagePlus imp) {
		// On ne traite que l'image 1
		imp.setSlice(1);
		String tag = DicomTools.getTag(imp, "0011,1012");
		// On ajoute un deuxieme tag de localisation a voir dans la pratique ou se situe
		// l'info
		if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0011,1030")))
			tag += DicomTools.getTag(imp, "0011,1030");
	
		// On set le Boolean a null
		Boolean anterieur = null;
		if (!StringUtils.isEmpty(tag)) {
			/// On recupere le 1er separateur de chaque vue dans le champ des orientation
			int separateur = tag.indexOf("\\");
	
			// Si on ne trouve pas le separateur, on met la position du separateur � la
			// fin de la string pour tout traiter
			if (separateur == -1)
				separateur = (tag.length());
	
			// Si la 1ere image est labelisee anterieure
			if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
				anterieur = true;
			}
			// Si la 1ere image est labellisee posterieure
			else if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
				anterieur = false;
			}
	
			// Si on ne trouve pas de tag le booelan reste null et on notifie l'utilisateur
			else if (!tag.substring(0, separateur).contains("POS") && !tag.substring(0, separateur).contains("_F")
					&& !tag.substring(0, separateur).contains("ANT") && !tag.substring(0, separateur).contains("_E")) {
				// le Boolean reste null et on informe l'user
				IJ.log("Information not reckognized");
			}
		} else {
			IJ.log("No localization information");
		}
	
		return anterieur;
	}


	/**
	 * Permet de tester si la 1ere image de l'ImagePlus est une image anterieure
	 * 
	 * @param imp
	 *            : ImagePlus a tester
	 * @return booleen vrai si image anterieure
	 */
	public static Boolean isAnterieur(ImagePlus imp) {
		Boolean anterieur = null;
		if (StaticMethod.isMultiFrame(imp)) {
			anterieur = isAnterieurMultiframe(imp);
		}
		if (!StaticMethod.isMultiFrame(imp)) {
			anterieur = isAnterieurUniqueFrame(imp);
		}
		return anterieur;
	}


	/**
	 * Permet de savoir si l'ImagePlus vient d'une Image MultiFrame (teste l'Image
	 * 1)
	 * 
	 * @param imp
	 *            : L'ImagePlus a tester
	 * @return : vrai si multiframe
	 */
	public static boolean isMultiFrame(ImagePlus imp) {
		// On regarde la coupe 1
		imp.setSlice(1);
	
		int slices = 0;
	
		// Regarde si frame unique ou multiple
		String numFrames = DicomTools.getTag(imp, "0028,0008");
	
		if (!StringUtils.isEmpty(numFrames)) {
			numFrames = numFrames.trim();
			// On passe le texte en Int
			slices = Integer.parseInt(numFrames);
		}
	
		if (slices == 1) {
			return false;
		}
	
		return true;
	
	}


	/********************* Public Static Sort ****************************************/
	/**
	  * Permet de renvoyer un tableau d'image plus selon les dicoms ouvertes, il
	 * peut y avoir une ou deux ouverte
	 * 
	 * @return les imps, [0] correspond a l'ant, [1] a la post
	 */
	public static ImagePlus[] sortDynamicAntPost(ImagePlus imagePlus) {
		ImagePlus[] sortedImagePlus = new ImagePlus[2];
		
		// si l'image est multiframe  et  ce nest pas la meme camera 
		if (isMultiFrame(imagePlus) && !isSameCameraMultiFrame(imagePlus)) { 
				sortedImagePlus[0] = splitCameraMultiFrame(imagePlus)[0];
				sortedImagePlus[1] = splitCameraMultiFrame(imagePlus)[1];
				return sortedImagePlus;
		} else {
			if (isAnterieur(imagePlus)) {
				sortedImagePlus[0] = imagePlus;
			} else {
				sortedImagePlus[1] = imagePlus;
			}
		}
		return sortedImagePlus;
	}


	public static ImagePlus[][] sortDynamicAntPost(ImagePlus[] imagePlus) {
		ImagePlus[][] imps = new ImagePlus[imagePlus.length][2];
		for (int i =0; i< imagePlus.length; i++) { // pour chaque fenetre
				imps[i] = sortDynamicAntPost(imagePlus[i]);
		}
		return imps;
	}


	/**
	 * Permet de trier les image Anterieure et posterieure et retourne les images
	 * posterieures pour garder la meme lateralisation (la droite est a gauche de
	 * l'image comme une image de face)
	 * 
	 * @param imp
	 *            : ImagePlus a trier
	 * @return Retourne l'ImagePlus avec les images posterieures inversees
	 */
	public static ImagePlus sortImageAntPost(ImagePlus imp) {
		return isMultiFrame(imp) ? StaticMethod.sortAntPostMultiFrame(imp) : StaticMethod.sortAntPostUniqueFrame(imp);
	}


	/**
	 * Permet de tirer et inverser les images posterieure pour les images multiframe
	 * A Eviter d'utiliser, preferer la methode sortImageAntPost(ImagePlus imp) qui
	 * est generique pour tout type d'image
	 * 
	 * @param imp0
	 *            : ImagePlus a trier
	 * @return Retourne l'ImagePlus triee
	 */
	@Deprecated
	public static ImagePlus sortAntPostMultiFrame(ImagePlus imp0) {
		// On duplique pour faire les modifs dans l'image dupliqu锟絜
		ImagePlus imp = imp0.duplicate();
	
		// On prend le Header
		String metadata = imp.getInfoProperty();
	
		// On recupere la chaine de vue
		String tag = DicomTools.getTag(imp, "0011,1012");
		if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0011,1030")))
			tag += DicomTools.getTag(imp, "0011,1030");
	
		// TAG 0011, 1012 semble absent de SIEMENS, TROUVER D AUTRE EXAMPLE POUR STATUER
		// Si pas de tag
		if (StringUtils.isEmpty(tag))
			tag = "no tag";
		// On recupere la chaine de detecteur
		// SK ZONE A RISQUE SI PAS DE CHAINE DE DETECTEUR A SURVEILLER
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagDetecteur)) {
			tagDetecteur = tagDetecteur.trim();
		}
		String delims = "[ ]+";
		String[] sequenceDeteceur = tagDetecteur.split(delims);
	
		/// On recupere le 1er separateur de chaque vue dans le champ des orientation
		int separateur = tag.indexOf("\\");
		// Si on ne trouve pas le separateur, on met la position du separateur a la fin
		// de la string pour tout traiter
		if (separateur == -1)
			separateur = (tag.length());
	
		// Si la 1ere image est labelisee anterieure
		if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
			// On recupere le num锟絩o du detecteur
			int detecteurAnterieur = Integer.parseInt(sequenceDeteceur[0]);
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ce
			// n'est pas le num锟絩o de ce deteceur
			for (int j = 0; j < sequenceDeteceur.length; j++) {
				int detecteur = Integer.parseInt(sequenceDeteceur[j]);
				if (detecteur != detecteurAnterieur) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}
	
		// Si la 1ere image est labelisee posterieurs
		if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
			// on r锟絚upere le num锟絩o du detecteur posterieur
			int detecteurPosterieur = Integer.parseInt(sequenceDeteceur[0]);
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ca
			// correspond 锟� ce deteceur
			for (int j = 0; j < sequenceDeteceur.length; j++) {
				int detecteur = Integer.parseInt(sequenceDeteceur[j]);
				if (detecteur == detecteurPosterieur) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}
	
		// Si on ne trouve pas de tag on flip toute detecteur 2 et on notifie
		// l'utilisateur
		if (!tag.substring(0, separateur).contains("POS") && !tag.substring(0, separateur).contains("_F")
				&& !tag.substring(0, separateur).contains("ANT") && !tag.substring(0, separateur).contains("_E")) {
			IJ.log("No Orientation tag found, assuming detector 2 is posterior. Please Notify Salim.Kanoun@gmail.com");
			for (int j = 0; j < sequenceDeteceur.length; j++) {
				int detecteur = Integer.parseInt(sequenceDeteceur[j]);
				if (detecteur == 2) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}
	
		ImagePlus[] pileImage = new ImagePlus[imp.getStackSize()];
	
		for (int j = 0; j < imp.getStackSize(); j++) {
			pileImage[j] = new ImagePlus();
			pileImage[j].setProcessor(imp.getStack().getProcessor(j + 1));
			pileImage[j].setProperty("Info", metadata);
			pileImage[j].setTitle("Image" + j);
		}
	
		Concatenator enchainer = new Concatenator();
		ImagePlus imp2 = enchainer.concatenate(pileImage, false);
		// ImagePlus imp2 = enchainer.concatenate(impAnt,impPost, false);
		// On retourne le resultat
		return imp2;
	
	}


	/**
	 * Permet de trier les image unique frame et inverser l'image posterieure A
	 * Eviter d'utiliser, pr�f�rer la methode sortImageAntPost(ImagePlus imp) qui
	 * est g�n�rique pour tout type d'image
	 * 
	 * @param imp0
	 *            : ImagePlus a trier
	 * @return retourne l'ImagePlus trier
	 */
	@Deprecated
	public static ImagePlus sortAntPostUniqueFrame(ImagePlus imp0) {
		// On copie dans une nouvelle image qu'on va renvoyer
		ImagePlus imp = imp0.duplicate();
	
		// Si unique frame on inverse toute image qui contient une image post锟絩ieure
		for (int i = 1; i <= imp.getImageStackSize(); i++) {
			imp.setSlice(i);
			String tag = DicomTools.getTag(imp, "0011,1012");
			// START ANGLE NE PARRAIT PAS FIABLE A VERIFIER
			// String tagStartAngle = DicomTools.getTag(imp, "0054,00200");
			// SK STRINGUTILS A GENERALISER DANS LE CODE CAR REGLE LES PROBLEME DES NULL ET
			// EMPTY STRING
			if (!StringUtils.isEmpty(tag))
				tag = tag.trim();
			// if (!StringUtils.isEmpty(tagStartAngle)) tagStartAngle=tagStartAngle.trim();
	
			String tagVector = DicomTools.getTag(imp, "0054,0020");
			if (!StringUtils.isEmpty(tagVector))
				tagVector = tagVector.trim();
	
			if (!StringUtils.isEmpty(tag)) {
				if (StringUtils.contains(tag, "POS") || StringUtils.contains(tag, "_F")) {
					imp.getProcessor().flipHorizontal();
					imp.setTitle("Post" + i);
				} else if (StringUtils.contains(tag, "ANT") || StringUtils.contains(tag, "_E")) {
					imp.setTitle("Ant" + i);// On ne fait rien
				} else {
					if (imp.getStackSize() == 2) {
						IJ.log("No Orientation found assuming Image 2 is posterior, please send image sample to Salim.kanoun@gmail.com if wrong");
						imp.getProcessor().flipHorizontal();
					}
				}
			}
	
			else {
				if (imp.getStackSize() == 2 && StringUtils.equals(tagVector, "2")) {
					IJ.log("No Orientation found assuming Image 2 is posterior, please send image sample to Salim.kanoun@gmail.com if wrong");
					imp.getProcessor().flipHorizontal();
				}
			}
	
		}
	
		return imp;
	}


	/***************************** Private Static ************************/
	static Roi createBkgInfLat(Roi roi, ImagePlus imp, int xOffset, RoiManager rm) {
		// on recupere ses bounds
		Rectangle bounds = roi.getBounds();
	
		Roi liver = (Roi) roi.clone();
		rm.addRoi(liver);
	
		int[] size = { (bounds.width / 4) * xOffset, (bounds.height / 4) * 1 };
	
		Roi liverShift = (Roi) roi.clone();
		liverShift.setLocation(liverShift.getXBase() + size[0], liverShift.getYBase() + size[1]);
		rm.addRoi(liverShift);
	
		// renvoi une section de la roi
		rm.setSelectedIndexes(new int[] { 0, 1 });
		rm.runCommand(imp, "XOR");
		rm.runCommand(imp, "Split");
	
		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;
		int w = size[0] * imp.getWidth();
		int h = size[1] * imp.getHeight();
	
		// permet de diviser la roi
		Rectangle splitter;
	
		if (w > 0) {
			splitter = new Rectangle(x, y, w, h);
		} else {
			splitter = new Rectangle(x + w, y, -w, h);
		}
	
		Roi rect = new Roi(splitter);
		rm.addRoi(rect);
	
		rm.setSelectedIndexes(new int[] { rm.getCount() - 1, rm.getCount() - 2 });
		rm.runCommand(imp, "AND");
	
		Roi bkg = (Roi) imp.getRoi().clone();
		int[] offset = new int[] { size[0] / 4, size[1] / 4 };
	
		// on deplace la roi pour ne pas qu'elle soit collee
		bkg.setLocation(bkg.getXBase() + xOffset, bkg.getYBase() + 1);
	
		return bkg;
	}


	/**************** Public Static Setter ***************************/
	/** 
	 * Affiche D et G en overlay sur l'image, L a gauche et R a droite
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayGD(Overlay overlay, ImagePlus imp) {
		StaticMethod.setOverlaySides(overlay, imp, null, "L", "R", 0);
	}


	/**
	 * Affiche D et G en overlay sur l'image, L a gauche et R a droite
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 * @param color
	 *            : Couleur de l'overlay
	 */
	public static void setOverlayGD(Overlay overlay, ImagePlus imp, Color color) {
		StaticMethod.setOverlaySides(overlay, imp, color, "L", "R", 0);
	}


	/**
	 * Affiche D et G en overlay sur l'image, R a gauche et L a droite
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayDG(Overlay overlay, ImagePlus imp) {
		StaticMethod.setOverlaySides(overlay, imp, null, "R", "L", 0);
	}


	/**
	 * Affiche D et G en overlay sur l'image, R a gauche et L a droite
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 * @param color
	 *            : Couleur de l'overlay
	 */
	public static void setOverlayDG(Overlay overlay, ImagePlus imp, Color color) {
		StaticMethod.setOverlaySides(overlay, imp, color, "R", "L", 0);
	}


	public static void setOverlayTitle(String title, Overlay overlay, ImagePlus imp, Color color, int slice) {
		int w = imp.getWidth();
		int h = imp.getHeight();
	
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
	
		Rectangle2D bounds = overlay.getLabelFont().getStringBounds(title, frc);
		double textHeight = bounds.getHeight();
		double textWidth = bounds.getWidth();
	
		double x = (w / 2) - (textWidth / 2);
		TextRoi top = new TextRoi(x, 0, title);
		top.setPosition(slice);
		if (color != null) {
			top.setStrokeColor(color);
		}
	
		// Set la police des text ROI
		top.setCurrentFont(overlay.getLabelFont());
	
		overlay.add(top);
	}


	public static void setOverlaySides(Overlay overlay, ImagePlus imp, Color color, String textL, String textR,
			int slice) {
		// Get taille Image
		int tailleImage = imp.getHeight();
	
		// Position au mileu dans l'axe Y
		double y = ((tailleImage) / 2);
	
		// Cote droit
		TextRoi right = new TextRoi(0, y, textL);
		right.setPosition(slice);
	
		// Cote gauche
		double xl = imp.getWidth() - (overlay.getLabelFont().getSize()); // sinon on sort de l'image
		TextRoi left = new TextRoi(xl, y, textR);
		left.setPosition(slice);
	
		if (color != null) {
			right.setStrokeColor(color);
			left.setStrokeColor(color);
		}
	
		// Set la police des text ROI
		right.setCurrentFont(overlay.getLabelFont());
		left.setCurrentFont(overlay.getLabelFont());
	
		// Ajout de l'indication de la droite du patient
		overlay.add(right);
		overlay.add(left);
	}


	/**
	 * Applique la LUT definie dans les preference � l'ImagePlus demandee
	 * 
	 * @param imp
	 *            : L'ImagePlus sur laquelle on va appliquer la LUT des preferences
	 */
	public static void setCustomLut(ImagePlus imp) {
		String lalut = Prefs.get("lut.preferred", null);
		if (lalut != null) {
			LUT lut = ij.plugin.LutLoader.openLut(lalut);
			imp.setLut(lut);
		}
	}


	/**************** Public Static Getter ***************************/
	/**
	 * return frame Duration as this tag is stored in sequence tag that are ignored by dicomTools (if multiple the first one is sent)
	 * @param imp
	 * @return
	 */
	public static String getFrameDuration(ImagePlus imp) {
		String property=imp.getInfoProperty();
		int index1 = property.indexOf("0018,1242");
		int index2 = property.indexOf(":", index1);
		int index3 = property.indexOf("\n", index2);
		String tag00181242 = property.substring(index2+1, index3).trim();
		return tag00181242;
	}


	// dayum that recursion
	public static Container getRootContainer(Container cont) {
		if (cont.getParent() instanceof Window) {
			return cont;
		}
	
		return getRootContainer(cont.getParent());
	}


	/**************** Private Static Getter ***************************/
	static Container getRootContainer(Component comp) {
		return getRootContainer(comp.getParent());
	}

	
	
}
