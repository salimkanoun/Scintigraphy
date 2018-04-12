package org.petctviewer.scintigraphy.scin.view;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.Concatenator;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.LUT;
import ij.util.DicomTools;

import java.awt.Font;
import java.awt.Label;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;
import org.petctviewer.scintigraphy.scin.controleur.ControleurScin;
import org.petctviewer.scintigraphy.shunpo.Vue_Shunpo;

public abstract class VueScin implements PlugIn {
	private String examType;
	
	protected FenetreApplication fen_application;
	private ModeleScin leModele;
	private ControleurScin leControleur;
	
	private ImagePlus imp;
	private Boolean antPost = Boolean.valueOf(false);
	protected int nombreAcquisitions;
	
	private RoiManager roiManager;

	private Overlay overlay;

	public VueScin(String examType) {
		this.examType = examType;
	}
	
	public void setControleur(ControleurScin ctrl) {
		this.leControleur = ctrl;
	}

	public void run(String arg) {
		new FenetreDialogue(this.examType, this);
	}
	
	protected abstract void ouvertureImage(String[] titresFenetres);

	protected void UIResultats(ImagePlus screen, JTable tableresults) {
		FenetreResultat win = new FenetreResultat(screen, tableresults);

		win.setTitle("Cardiac Results");
		win.setLocationRelativeTo(null);
		win.getCanvas().setMagnification(0.7D);
		win.getCanvas().setScaleToFit(true);
		win.pack();
		win.setSize(win.getPreferredSize());

		IJ.setTool("hand");
	}

	/**
	 * Affiche D et G en overlay sur l'image
	 * 
	 * @param overlay
	 *            : Overlay sur lequel ajouter D/G
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayDG(Overlay overlay, ImagePlus imp) {
		// Get taille Image
		int tailleImage = imp.getHeight();

		// Position au mileu dans l'axe Y
		double y = ((tailleImage) / 2);

		// Cote droit
		TextRoi right = new TextRoi(0, y, "R");

		// Cote gauche
		double xl = imp.getWidth() - (overlay.getLabelFont().getSize()); // sinon on sort de l'image
		TextRoi left = new TextRoi(xl, y, "L");

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

		if (slices == 1)
			return false;
		else
			return true;

	}

	/**
	 * Permet de trier les image Anterieure et posterieure et retourne les images
	 * posterieures pour garder la meme lateralisation (la droite est � gauche de
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
	 * Eviter d'utiliser, pr�f�rer la methode sortImageAntPost(ImagePlus imp)
	 * qui est g�n�rique pour tout type d'image
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
				} catch (ParseException e) {
					e.printStackTrace();
				}

				return (int) ((timeImage0.getTime() - timeImage1.getTime()) / 1000);
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
	 * Cree overlay et set la police SK : A Optimiser pour tenir compte de la taille
	 * initiale de l'Image
	 * 
	 * @return Overlay
	 */
	public static Overlay initOverlay(ImagePlus imp) {
		// On initialise l'overlay il ne peut y avoir qu'un Overlay
		// pour tout le programme sur lequel on va ajouter/enlever les ROI au fur et a
		// mesure
		Overlay overlay = new Overlay();
		// On defini la police et la propriete des Overlays
		int height = imp.getHeight();
		// On normalise Taille 12 a 256 pour avoir une taille stable pour toute image
		Float facteurConversion = (float) ((height * 1.0) / 256);
		Font font = new Font("Arial", Font.PLAIN, Math.round(12 * facteurConversion));
		overlay.drawLabels(true);
		overlay.drawNames(true);
		overlay.setLabelFont(font, true);
		// Pour rendre overlay non selectionnable
		overlay.selectable(false);

		return overlay;
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

	public void end() {
		this.fen_application.dispose();
		System.gc();
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

	public FenetreApplication getFen_application() {
		return fen_application;
	}

	public boolean isAntPost() {
		return antPost;
	}
	
	public RoiManager getRoiManager() {
		return this.roiManager;
	}
	
	public Overlay getOverlay() {
		return this.overlay;
	}

}
