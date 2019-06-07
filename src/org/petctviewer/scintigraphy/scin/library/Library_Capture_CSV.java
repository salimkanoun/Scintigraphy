package org.petctviewer.scintigraphy.scin.library;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabMain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Library_Capture_CSV {

	public static final String PATIENT_INFO_NAME = "name", PATIENT_INFO_ID = "id", PATIENT_INFO_DATE = "date",
			PATIENT_INFO_ACCESSION_NUMBER = "accessionNumber";

	/**
	 * renvoie une hasmap contenant les informations du patient selon le tag info de
	 * l'imp keys : id name date
	 */
	public static HashMap<String, String> getPatientInfo(ImagePlus imp) {
		HashMap<String, String> hm = new HashMap<>();

		// ajout du studyName, si il n'existe pas on ajoute une string vide
		String tagName = DicomTools.getTag(imp, "0010,0010");
		if (tagName != null)
			hm.put(PATIENT_INFO_NAME, tagName.trim().replace("^", " "));
		else
			hm.put(PATIENT_INFO_NAME, "");

		// ajout de l'id, si il n'existe pas on ajoute une string vide
		String tagId = DicomTools.getTag(imp, "0010,0020");
		if (tagId != null)
			hm.put(PATIENT_INFO_ID, tagId.trim());
		else
			hm.put(PATIENT_INFO_ID, "");

		// ajout de la date studyName, si il n'existe pas on ajoute une string vide
		String tagDate = DicomTools.getTag(imp, "0008,0022");
		if (tagDate != null) {
			String dateStr = tagDate.trim();
			Date result;
			try {
				result = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
				String r = new SimpleDateFormat(Prefs.get(PrefTabMain.PREF_DATE_FORMAT, "MM/dd/yyyy")).format(result);
				hm.put(PATIENT_INFO_DATE, r);
			} catch (ParseException e) {
				hm.put(PATIENT_INFO_DATE, "");
				e.printStackTrace();
			}

		} else
			hm.put(PATIENT_INFO_DATE, "");

		// ajout de l'accesionNumber, si il n'existe pas on ajoute une string vide
		String tagAccessionNumber = DicomTools.getTag(imp, "0008,0050");
		if (tagAccessionNumber != null)
			hm.put(PATIENT_INFO_ACCESSION_NUMBER, tagAccessionNumber.trim());
		else
			hm.put(PATIENT_INFO_ACCESSION_NUMBER, "");
		return hm;
	}

	/**
	 * prepare la premiere partie des tags du header du dicom avec l'iud passe en
	 * parametre <br>
	 * <br>
	 * See also : <br>
	 * {@link Library_Capture_CSV#genererDicomTagsPartie1(ImagePlus, String)} <br>
	 * {@link ModelScin#genererDicomTagsPartie1SameUID(ImagePlus, String)}
	 */
	public static String getTagPartie1(HashMap tags, String nomProgramme, String uid) {
		String sopID = Library_Capture_CSV.generateSOPInstanceUID(new Date());
		String tag = "0002,0002 Media Storage SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n" + "0002,0003 Media"
				+ " Storage SOP Inst UID: " + sopID + "\n" + "0002,0010 Transfer Syntax UID: " + "1.2.840.10008.1.2.1"
				+ "\n" + "0002,0013 Implementation Version Name: jpeg" + "\n"
				+ "0002,0016 Source Application Entity Title: " + "\n" + "0008,0008 Image Type: DERIVED\\SECONDARY "
				+ "\n" + "0008,0016 SOP Class UID: " + "1.2.840.10008.5.1.4.1.1.7" + "\n"
				+ "0008,0018 SOP Instance UID: " + sopID + "\n" + "0008,0020 Study Date:" + tags.get("0008,0020") + "\n"
				+ "0008,0021 Series Date:" + tags.get("0008,0021") + "\n" + "0008,0030 Study Time:"
				+ tags.get("0008,0030") + "\n" + "0008,0031 Series Time:" + tags.get("0008,0031") + "\n";
		if (tags.get("0008,0050") != null)
			tag += "0008,0050 Accession Number:" + tags.get("0008,0050") + "\n";
		if (tags.get("0008,0060") != null)
			tag += "0008,0060 Modality:" + tags.get("0008,0060") + "\n";
		tag += "0008,0064 Conversion Type: WSD" + "\n" + "0008,0070 Manufacturer:" + tags.get("0008,0070") + "\n";
		if (tags.get("0008,0080") != null)
			tag += "0008,0080 Institution Name:" + tags.get("0008,0080") + "\n";
		if (tags.get("0008,0090") != null)
			tag += "0008,0090 Referring Physician's Name:" + tags.get("0008,0090") + "\n";
		if (tags.get("0008,1030") != null)
			tag += "0008,1030 Study Description:" + tags.get("0008,1030") + "\n";
		tag += "0008,103E Series Description: Capture " + nomProgramme + "\n" + "0010,0010 Patient's Name:"
				+ tags.get("0010,0010") + "\n" + "0010,0020 Patient ID:" + tags.get("0010,0020") + "\n";
		if (tags.get("0010,0030") != null)
			tag += "0010,0030 Patient's Birth Date:" + tags.get("0010,0030") + "\n";
		if (tags.get("0010,0040") != null)
			tag += "0010,0040 Patient's Sex:" + tags.get("0010,0040") + "\n";
		tag += "0020,000D Study Instance UID:" + tags.get("0020,000D") + "\n" + "0020,000E Series Instance UID:"
				+ ((String) tags.get("0020,000E")).substring(0, ((String) tags.get("0020,000E")).length() - 6) + uid
				+ "\n";
		if (tags.get("0020,0010") != null)
			tag += "0020,0010 Study ID :" + tags.get("0020,0010") + "\n";
		tag += "0020,0011 Series Number: 1337" + "\n" + "0020,0013 Instance Number: 1" + "\n" + "0020,0032 Image "
				+ "Position (Patient):" + tags.get("0020,0032") + "\n" + "0020,0037 Image Orientation (Patient):"
				+ tags.get("0020,0037") + "\n" + "0028,0002 Samples per Pixel: 3" + "\n" + "0028,0004 Photometric "
				+ "Interpretation: RGB" + "\n" + "0028,0006 Planar Configuration: 0" + "\n" + "0028,0008 Number of "
				+ "Frames: 1 \n";
		return tag;
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
		for (ImagePlus imagePlus : tableauImagePlus) {

			stackCapture.addSlice(imagePlus.getProcessor());
		}
		// On retourne le stack de Capture
		return stackCapture;
	}

	/**
	 * Creates a capture of the specified image (this includes the overlay of the
	 * ImagePlus). The image is then resized with the specified dimensions (width,
	 * height).<br>
	 * If width <b>or</b> height is set to 0, then the ratio is kept.<br>
	 * If <b>both</b> width and height are set to 0, then the original dimensions
	 * are used.
	 *
	 * @param imp
	 *            ImagePlus to take a capture from
	 * @param width
	 *            Width of the output capture
	 * @param height
	 *            Height of the output capture
	 * @return capture of the image at the specified dimensions
	 * @throws IllegalArgumentException
	 *             if any dimension is negative
	 * @author Titouan QUÉMA
	 */
	public static ImagePlus captureImage(ImagePlus imp, int width, int height) throws IllegalArgumentException {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("Width and height cannot be negative");

		ImageCanvas canvas = imp.getCanvas();
		BufferedImage buf = (BufferedImage) canvas.createImage(canvas.getWidth(), canvas.getHeight());
		Graphics2D g2 = buf.createGraphics();
		canvas.paint(g2);

		// Calculate ratio
		Image img;
		if (width == 0 && height == 0)
			img = buf;
		else if (width == 0)
			img = buf.getScaledInstance(canvas.getWidth() * height / canvas.getHeight(), height, Image.SCALE_DEFAULT);
		else if (height == 0)
			img = buf.getScaledInstance(width, canvas.getHeight() * width / canvas.getWidth(), Image.SCALE_DEFAULT);
		else
			img = buf.getScaledInstance(width, height, Image.SCALE_DEFAULT);

		return new ImagePlus("Capture of " + imp.getShortTitle(), img);
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
		ImageProcessor ip2;
		if (hauteur == 0 && largeur != 0) {
			ip2 = ip.resize(largeur);
		} else if (hauteur == 0) {
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
	 *            que le studyName du patient...)
	 * @param nomProgramme
	 *            : studyName du programme qui l'utilise si par exemple "pulmonary
	 *            shunt" la capture sera appelee "Capture Pulmonary Shunt"
	 * @return retourne la premi�re partie du header en string auquelle on ajoutera
	 *         la 2eme partie via la deuxieme methode
	 */
	public static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme) {
		Random random = new Random();
		String uid = Integer.toString(random.nextInt(1000000));

		return Library_Capture_CSV.genererDicomTagsPartie1(imp, nomProgramme, uid);
	}

	public static String genererDicomTagsPartie1(ImagePlus imp, String nomProgramme, String uid) {
		HashMap<String, String> tags = new HashMap<>();
		tags.put("0008,0020", DicomTools.getTag(imp, "0008,0020"));
		tags.put("0008,0021", DicomTools.getTag(imp, "0008,0021"));
		tags.put("0008,0030", DicomTools.getTag(imp, "0008,0030"));
		tags.put("0008,0031", DicomTools.getTag(imp, "0008,0031"));
		tags.put("0008,0050", DicomTools.getTag(imp, "0008,0050"));
		tags.put("0008,0060", DicomTools.getTag(imp, "0008,0060"));
		tags.put("0008,0070", DicomTools.getTag(imp, "0008,0070"));
		tags.put("0008,0080", DicomTools.getTag(imp, "0008,0080"));
		tags.put("0008,0090", DicomTools.getTag(imp, "0008,0090"));
		tags.put("0008,1030", DicomTools.getTag(imp, "0008,1030"));
		tags.put("0010,0010", DicomTools.getTag(imp, "0010,0010"));
		tags.put("0010,0020", DicomTools.getTag(imp, "0010,0020"));
		tags.put("0010,0030", DicomTools.getTag(imp, "0010,0030"));
		tags.put("0010,0040", DicomTools.getTag(imp, "0010,0040"));
		tags.put("0020,000D", DicomTools.getTag(imp, "0020,000D"));
		tags.put("0020,000E", DicomTools.getTag(imp, "0020,000E"));
		tags.put("0020,0010", DicomTools.getTag(imp, "0020,0010"));
		tags.put("0020,0032", DicomTools.getTag(imp, "0020,0032"));
		tags.put("0020,0037", DicomTools.getTag(imp, "0020,0037"));
		return getTagPartie1(tags, nomProgramme, uid);
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
		return "0028,0010 Rows: " + CaptureFinale.getHeight() + "\n" + "0028,0011 Columns: " + CaptureFinale.getWidth()
				+ "\n" + "0028,0100 Bits Allocated: 8" + "\n" + "0028,0101 Bits Stored: 8" + "\n" + "0028,"
				+ "0102 High Bit: 7" + "\n" + "0028,0103 Pixel Representation: 0 \n";
	}

	/**
	 * Permet d'exporter le ROI manager uniquement dans un zip contenant les ROI
	 * (dans le cadre d'un logiciel ne generant pas de resultat utile a sauver qui
	 * seront trait�s par un autre logiciel par exemple)
	 *
	 * @param Roi
	 *            : Le ROI manager utilise dans le programme
	 * @param nomProgramme
	 *            : Le studyName du programme (creation d'un sous repertoire)
	 * @param imp
	 *            : Une ImagePlus originale ou de capture secondaire avec le header
	 *            pour recuperer studyName, ID, date d'examen.
	 */
	public static void exportRoiManager(RoiManager Roi, String nomProgramme, ImagePlus imp) {

		// On recupere le Patient ID de l'ImagePlus
		String patientID;
		patientID = DicomTools.getTag(imp, "0010,0020");
		if (patientID != null && !patientID.isEmpty())
			patientID = patientID.trim();

		// On recupere la date d'examen
		String date;
		date = DicomTools.getTag(imp, "0008,0020");
		if (date != null && !date.isEmpty())
			date = date.trim();

		// On recupere le path de sauvegarde
		String path = Prefs.get(PrefTabMain.PREF_SAVE_DIRECTORY, null);
		boolean testEcriture = false;

		// On verifie que le path est writable si il existe
		if (path != null) {
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if (path != null && !testEcriture) {
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("Path not writable, CSV/ZIP export has failed");
		}

		if (path != null && testEcriture) {
			// On construit le sous repertoire avecle studyName du programme et l'ID du
			// Patient
			String pathFinal = path + File.separator + nomProgramme + File.separator + patientID;
			File subDirectory = new File(pathFinal);
			if (subDirectory.mkdirs()) {

				// On ecrit le ZIP contenant la sauvegarde des ROIs
				Roi[] rois2 = Roi.getRoisAsArray();
				int[] tab = new int[rois2.length];
				for (int i = 0; i < rois2.length; i++)
					tab[i] = i;
				Roi.setSelectedIndexes(tab);
				Roi.runCommand("Save", pathFinal + File.separator + patientID + "_" + date + ".zip");
			} else {
				System.err.println("An error occurred when trying to create directories for the path: " + pathFinal
						+ ". Aborting" + " creation of ZIP.");
			}
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

	// [0] : studyName, [1] : id, [2] : date
	public static String[] getInfoPatient(ImagePlus imp) {
		String[] infoPatient = new String[4];

		// On recupere le Patient Name de l'ImagePlus
		String patientName;
		patientName = DicomTools.getTag(imp, "0010,0010");
		if (patientName != null && !patientName.isEmpty())
			patientName = patientName.trim();

		// On recupere le Patient ID de l'ImagePlus
		String patientID;
		patientID = DicomTools.getTag(imp, "0010,0020");
		if (patientID != null && !patientID.isEmpty())
			patientID = patientID.trim();

		// On recupere la date d'examen
		String date;
		date = DicomTools.getTag(imp, "0008,0020");
		if (date != null && !date.isEmpty())
			date = date.trim();

		// We get the AccessionNumber
		String accessionNumber;
		accessionNumber = DicomTools.getTag(imp, "0008,0050");
		if (accessionNumber != null && !accessionNumber.isEmpty())
			accessionNumber = accessionNumber.trim();

		infoPatient[0] = patientName;
		infoPatient[1] = patientID;
		infoPatient[2] = date;
		infoPatient[3] = accessionNumber;

		return infoPatient;
	}

	/**
	 * Renvoie un montage avec un pas regulier
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

	// dayum that recursion
	public static Container getRootContainer(Container cont) {
		if (cont.getParent() instanceof Window) {
			return cont;
		}

		return getRootContainer(cont.getParent());
	}

	/**************** Private Static Getter ***************************/
	public static Container getRootContainer(Component comp) {
		return getRootContainer(comp.getParent());
	}

}
