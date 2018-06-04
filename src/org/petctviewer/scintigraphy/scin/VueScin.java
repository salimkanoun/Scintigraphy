package org.petctviewer.scintigraphy.scin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.util.DicomTools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

public abstract class VueScin implements PlugIn {
	public static final int HEART = 0, INFLAT = 1, KIDNEY = 2;

	private String examType;

	private FenApplication fen_application;

	private ImagePlus imp;
	private Boolean antPost = Boolean.valueOf(false);
	protected int nombreAcquisitions;

	protected VueScin(String examType) {
		this.examType = examType;
	}

	/**
	 * Lance la fen�tre de dialogue permettant le lancemet du programme
	 */
	@Override
	public void run(String arg) {
		FenSelectionDicom selection = new FenSelectionDicom(this.getExamType());
		selection.setModal(true);
		selection.setVisible(true);
		if (selection.getSelectedWindowsTitles() != null) {
			if (selection.getSelectedWindowsTitles().length > 0) {
				try {
					ouvertureImage(selection.getSelectedWindowsTitles());
				} catch (Exception e) {
					System.err.println("The selected dicoms are not usable for this exam");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Permet de renvoyer une tableau d'image plus selon les dicoms ouvertes, il
	 * peut y avoir une ou deux ouverte
	 * @return les imps, [0] correspond a l'ant, [1] a la post
	 */
	public static ImagePlus[] sortAntPost(ImagePlus[] imagePlus) {
		if (imagePlus.length > 2) {
			throw new IllegalArgumentException("Too much imp");
		}

		ImagePlus[] imps = new ImagePlus[2];

		if (imagePlus.length == 1) { // si il y a qu'un fenetre d'ouverte
			ImagePlus imp = imagePlus[0];

			if (VueScin.isMultiFrame(imp)) { // si l'image est multiframe

				if (!VueScin.isSameCameraMultiFrame(imp)) {
					return VueScin.splitCameraMultiFrame(imp);
				}

				if (VueScin.isAnterieur(imp)) {
					imps[0] = imp;
				} else {
					imps[1] = imp;
				}

			} else if (VueScin.isAnterieur(imp)) {
				imps[0] = imp;
			} else {
				imps[1] = imp;
			}

		} else { // si il y a deux images dans le tableau
			for (ImagePlus imp : imagePlus) { // pour chaque fenetre
				if (VueScin.isAnterieur(imp)) { // si la vue est ant, on choisi cette image
					imps[0] = (ImagePlus) imp.clone();
				} else {
					imps[1] = (ImagePlus) imp.clone();
				}
			}
		}

		return imps;
	}

	// TODO refactoriser en preparer imp et ouvrir fenetre ?
	/**
	 * Prepare la fenetre de l'application selon les dicoms ouvertes
	 * 
	 * @param titresFenetres
	 *            liste des fenetres ouvertes
	 */
	protected abstract void ouvertureImage(String[] titresFenetres);

	/**
	 * Affiche D et G en overlay sur l'image, L a gauche et R a droite
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayGD(Overlay overlay, ImagePlus imp) {
		setOverlaySides(overlay, imp, null, "L", "R", 0);
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
		setOverlaySides(overlay, imp, color, "L", "R", 0);
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
		setOverlaySides(overlay, imp, null, "R", "L", 0);
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
		setOverlaySides(overlay, imp, color, "R", "L", 0);
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
		ImagePlus imp2 = null;
		if (isMultiFrame(imp)) {
			imp2 = sortAntPostMultiFrame(imp);
		}
		if (!isMultiFrame(imp)) {
			imp2 = sortAntPostUniqueFrame(imp);
		}
		return imp2;
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
		if (isMultiFrame(imp)) {
			anterieur = isAnterieurMultiframe(imp);
		}
		if (!isMultiFrame(imp)) {
			anterieur = isAnterieurUniqueFrame(imp);
		}
		return anterieur;
	}

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
		Boolean anterieurPremiereImage = isAnterieurMultiframe(imp);

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

	/**
	 * Test si les images du MutiFrame viennent toutes de la meme camera
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
	 * Cree overlay et set la police initiale de l'Image
	 * 
	 * @return Overlay
	 */
	public static Overlay initOverlay(ImagePlus imp, int taille) {
		int taille2;
		if (taille != -1) {
			taille2 = taille;
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

	public void setCaptureButton(JButton btn_capture, JLabel lbl_credits, JFrame jf, ModeleScin modele,
			String additionalInfo) {
		setCaptureButton(btn_capture, new Component[] { lbl_credits }, new Component[] { btn_capture }, jf, modele,
				additionalInfo);
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

	/**
	 * Prepare le bouton capture de la fenetre resultat
	 * 
	 * @param btn_capture
	 *            le bouton capture, masque lors de la capture
	 * @param show
	 *            le label de credits, affiche lors de la capture
	 * @param jf
	 *            la jframe
	 * @param modele
	 *            le modele
	 * @param additionalInfo
	 *            string a ajouter a la fin du nom de la capture si besoin
	 */
	public void setCaptureButton(JButton btn_capture, Component[] show, Component[] hide, JFrame jf, ModeleScin modele,
			String additionalInfo) {

		String examType = this.getExamType();

		// generation du tag info
		String info = ModeleScin.genererDicomTagsPartie1(this.getImp(), this.getExamType())
				+ ModeleScin.genererDicomTagsPartie2(this.getImp());

		// on ajoute le listener sur le bouton capture
		btn_capture.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// on suprrime le bouton et on affiche le label
				JButton b = (JButton) (e.getSource());

				for (Component comp : hide) {
					comp.setVisible(false);
				}

				for (Component comp : show) {
					comp.setVisible(true);
				}

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Container c = jf.getContentPane();

						// Capture, nouvelle methode a utiliser sur le reste des programmes
						BufferedImage capture = new BufferedImage(c.getWidth(), c.getHeight(),
								BufferedImage.TYPE_INT_ARGB);
						c.paint(capture.getGraphics());
						ImagePlus imp = new ImagePlus("capture", capture);

						for (Component comp : hide) {
							comp.setVisible(true);
						}

						for (Component comp : show) {
							comp.setVisible(false);
						}

						jf.dispose();

						// on passe a la capture les infos de la dicom
						imp.setProperty("Info", info);
						// on affiche la capture
						imp.show();

						// on change l'outil
						IJ.setTool("hand");

						// generation du csv
						String resultats = modele.toString();

						try {
							ModeleScin.exportAll(resultats, getFenApplication().getControleur().getRoiManager(),
									examType, imp, additionalInfo);

							getFenApplication().getControleur().getRoiManager().close();

							imp.killRoi();
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// Execution du plugin myDicom
						try {
							IJ.run("myDicom...");
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						VueScin.this.fen_application.windowClosing(null);
						System.gc();
					}
				});

			}
		});
	}
	
	// cree la roi de bruit de fond
	public static Roi createBkgRoi(Roi roi, ImagePlus imp, int organ) {
		Roi bkg = null;
		RoiManager rm = new RoiManager(true);
		rm.setVisible(true);

		switch (organ) {
		case VueScin.KIDNEY:
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

		case VueScin.HEART:
			// TODO
			break;

		case VueScin.INFLAT:
			// TODO
			break;
		default:
			bkg = roi;
			break;
		}

		rm.dispose();

		bkg.setStrokeColor(Color.GRAY);
		return bkg;
	}

	public static void editLabelOverlay(Overlay ov, String oldName, String newName, Color c) {
		Roi roi = ov.get(ov.getIndex(oldName));
		if (roi != null) {
			roi.setName(newName);
			roi.setStrokeColor(c);
		}
	}
	
	/**
	 * Renvoie un montage
	 * 
	 * @param frameDuration
	 * @param imp
	 * @param size
	 * @return
	 */
	public static ImagePlus creerMontage(int[] frameDuration, ImagePlus imp, int size, int rows, int columns) {
		int nSlice = frameDuration.length;

		int[] summed = new int[frameDuration.length];
		summed[0] = frameDuration[0];
		for (int i = 1; i < nSlice; i++) {
			summed[i] = summed[i - 1] + frameDuration[i];
		}

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

		Concatenator enchainer = new Concatenator();
		ImagePlus impStacked = enchainer.concatenate(impList, false);

		MontageMaker mm = new MontageMaker();

		return mm.makeMontage2(impStacked, columns, rows, 1.0, 1, impList.length, 1, 0, false);
	}
	
	public static ImagePlus[] splitAntPost(ImagePlus imp) {
		return null;
	}

	public ImagePlus getImp() {
		return this.imp;
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}

	public String getExamType() {
		return this.examType;
	}

	public void setExamType(String examType) {
		this.examType = examType;
	}

	public FenApplication getFenApplication() {
		return this.fen_application;
	}

	public void setFenApplication(FenApplication fen_application) {
		this.fen_application = fen_application;
	}

	public boolean isAntPost() {
		return this.antPost;
	}

}
