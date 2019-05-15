package org.petctviewer.scintigraphy.scin.library;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Concatenator;
import ij.plugin.ZProjector;
import ij.process.StackProcessor;
import ij.util.DicomTools;

public class Library_Dicom {

	/**
	 *
	 * @param imp
	 * @return date d'acquisition de l'image plus
	 */
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

	/**
	 * Permet de spliter les images d'un multiFrame contenant 2 camera, image 0
	 * camera Ant et Image1 Camera Post (ne flip pas l'image post)
	 * 
	 * @param imp : ImagePlus a traiter
	 * @return Tableau d'imagePlus avec 2 ImagePlus (camera 1 et 2 )
	 */
	private static ImagePlus[] splitCameraMultiFrame(ImagePlus imp, boolean isAntPost) {
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
//		Boolean anterieurPremiereImage = Library_Dicom.isAnterieurMultiframe(imp);

		// On ajoute les images dans les camera adhoc

		if (isAntPost) {
			for (int i = 0; i < sequenceDetecteur.length; i++) {
				if (sequenceDetecteur[i].equals(detecteurPremiereImage)) {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
		} else // if (anterieurPremiereImage != null && !anterieurPremiereImage) {
			for (int i = 0; i < sequenceDetecteur.length; i++) {
				if (sequenceDetecteur[i].equals(detecteurPremiereImage)) {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
//		} else {
//			System.out.println("assuming image 2 is posterior. Please notify Salim.kanoun@gmail.com");
//			for (int i = 0; i < sequenceDetecteur.length; i++) {
//				if (sequenceDetecteur[i].equals("1")) {
//					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
//				} else if (sequenceDetecteur[i].equals("2")) {
//					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
//				}
//			}
//		}

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

	/*********************
	 * Public Static Is
	 ****************************************/

	/**
	 * Test si les images du MutiFrame viennent toutes de la meme camera
	 * 
	 * @param imp : ImagePlus � traiter
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
	 * @param imp : ImagePus A traiter
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
	 * d'utiliser car la methode {@link Library_Dicom#isAnterieur()} est generique
	 * pour tout type d'image
	 * 
	 * @param imp : ImagePlus a tester
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
					System.out.println("Orientation not reckognized");
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
					System.out.println("Orientation Not reckgnized, assuming vector 1 is anterior");
				}
				// le Boolean reste null et on informe l'user
				else {
					System.out.println("Orientation not reckognized");
				}
			}

		}

		// Si aucun des deux echec du reperage
		else {
			System.out.println("Orientation not reckognized");
		}

		return anterieur;
	}

	/**
	 * Permet de tester si l'image est anterieure pour une MultiFrame, ne teste que
	 * la premiere Image (peut etre generalisee plus tard si besoin) A Eviter
	 * d'utiliser car la methode {@link Library_Dicom#isAnterieur()} est generique
	 * pour tout type d'image
	 * 
	 * @param imp : ImagePlus a tester
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
				System.out.println("Information not reckognized");

			}
		} else {
			System.out.println("No localization information");
		}

		return anterieur;
	}

	/**
	 * Permet de tester si la 1ere image de l'ImagePlus est une image anterieure
	 * 
	 * @param imp : ImagePlus a tester
	 * @return booleen vrai si image anterieure
	 */
	public static Boolean isAnterieur(ImagePlus imp) {
		Boolean anterieur = null;

		if (Library_Dicom.isMultiFrame(imp)) {
			anterieur = isAnterieurMultiframe(imp);
		}
		if (!Library_Dicom.isMultiFrame(imp)) {
			anterieur = isAnterieurUniqueFrame(imp);
		}
		return anterieur;
	}

	/**
	 * Permet de savoir si l'ImagePlus vient d'une Image MultiFrame (teste l'Image
	 * 1)
	 * 
	 * @param imp : L'ImagePlus a tester
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
	 * Splits the specified image into an array of ImageSelection such as
	 * <ul>
	 * <li>[0]-><code>ANT</code></li>
	 * <li>[1]-><code>POST</code>.</li>
	 * </ul>
	 * The returned images are clones of the input image, and their orientation is
	 * updated.
	 * 
	 * @param image Dynamic image in DynP/A or DynA/P orientation
	 * @return array of ImageSelection
	 * @throws WrongOrientationException if the image has an orientation different
	 *                                   than DYNAMIC_ANT_POST and DYNAMIC_POST_ANT
	 * @throws IllegalArgumentException  if the image's tag indicates the camera is
	 *                                   the same or if it indicates it's not a
	 *                                   dynamic image
	 * @author Titouan QUÉMA
	 */
	public static ImageSelection[] splitDynamicAntPost(ImageSelection image)
			throws WrongOrientationException, IllegalArgumentException {
		Orientation[] expectedOrientations = new Orientation[] { Orientation.DYNAMIC_ANT_POST,
				Orientation.DYNAMIC_POST_ANT };
		ImagePlus imagePlus = image.getImagePlus();
		if (!Arrays.stream(expectedOrientations).anyMatch(i -> i.equals(image.getImageOrientation())))
			throw new WrongOrientationException(image.getImageOrientation(), expectedOrientations);

		if (!isMultiFrame(imagePlus) || isSameCameraMultiFrame(imagePlus))
			throw new IllegalArgumentException("The image's tag are incorrect and cannot be detected as an "
					+ Arrays.toString(Orientation.dynamicOrientations()) + " image!");

		ImageSelection[] result = new ImageSelection[2];
		for (int i = 0; i < result.length; i++)
			result[i] = image.clone();

		ImagePlus[] imageSplitted = splitCameraMultiFrame(imagePlus,
				image.getImageOrientation() == Orientation.DYNAMIC_ANT_POST);
		result[0].setImagePlus(imageSplitted[0]);
		result[1].setImagePlus(imageSplitted[1]);

		return result;
	}

	/**
	 * Permet de trier les image Anterieure et posterieure et retourne les images
	 * posterieures pour garder la meme lateralisation (la droite est a gauche de
	 * l'image comme une image de face)
	 * 
	 * @param imp : ImagePlus a trier
	 * @return Retourne l'ImagePlus avec les images posterieures inversees
	 */
	public static ImagePlus sortImageAntPost(ImagePlus imp) {
		return isMultiFrame(imp) ? Library_Dicom.sortAntPostMultiFrame(imp) : Library_Dicom.sortAntPostUniqueFrame(imp);
	}

	/**
	 * Permet de tirer et inverser les images posterieure pour les images multiframe
	 * A Eviter d'utiliser, preferer la methode
	 * {@link Library_Dicom#sortImageAntPost()} qui est generique pour tout type
	 * d'image
	 * 
	 * @param imp0 : ImagePlus a trier
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
			// On recupere le numero du detecteur
			int detecteurAnterieur = Integer.parseInt(sequenceDeteceur[0]);
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ce
			// n'est pas le numero de ce deteceur
			for (int j = 0; j < sequenceDeteceur.length; j++) {
				int detecteur = Integer.parseInt(sequenceDeteceur[j]);
				if (detecteur != detecteurAnterieur) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}

		// Si la 1ere image est labelisee posterieurs
		if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
			// on recupere le numero du detecteur posterieur
			int detecteurPosterieur = Integer.parseInt(sequenceDeteceur[0]);
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ca
			// correspond a ce deteceur
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
			System.out.println(
					"No Orientation tag found, assuming detector 2 is posterior. Please Notify Salim.Kanoun@gmail.com");
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
		imp2.setProperty("Info", metadata);
		// ImagePlus imp2 = enchainer.concatenate(impAnt,impPost, false);
		// On retourne le resultat
		return imp2;

	}

	/**
	 * Permet de trier les image unique frame et inverser l'image posterieure A
	 * Eviter d'utiliser, pr�f�rer la methode
	 * {@link Library_Dicom#sortImageAntPost()} qui est g�n�rique pour tout type
	 * d'image
	 * 
	 * @param imp0 : ImagePlus a trier
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
			// if (!StringUtils.isEmpty(tagStartAngle)) tagStartAngle=tagStartAngle.trim();

			String tagVector = DicomTools.getTag(imp, "0054,0020");
			if (!StringUtils.isEmpty(tagVector))
				tagVector = tagVector.trim();

			if (!StringUtils.isEmpty(tag)) {
				tag = tag.trim();
				if (StringUtils.contains(tag, "POS") || StringUtils.contains(tag, "_F")) {
					imp.getProcessor().flipHorizontal();
					imp.setTitle("Post" + i);
				} else if (StringUtils.contains(tag, "ANT") || StringUtils.contains(tag, "_E")) {
					imp.setTitle("Ant" + i);// On ne fait rien
				} else {
					if (imp.getStackSize() == 2) {
						System.out.println(
								"No Orientation found assuming Image 2 is posterior, please send image sample to Salim.kanoun@gmail.com if wrong");
						imp.getProcessor().flipHorizontal();
					}
				}
			}

			else {
				if (imp.getStackSize() == 2 && StringUtils.equals(tagVector, "2")) {
					System.out.println(
							"No Orientation found assuming Image 2 is posterior, please send image sample to Salim.kanoun@gmail.com if wrong");
					imp.getProcessor().flipHorizontal();
				}
			}

		}

		return imp;
	}

	/**************** Public Static Getter ***************************/

	/**
	 * 
	 * return frame Duration as this tag is stored in sequence tag that are ignored
	 * by dicomTools (if multiple the first one is sent)
	 * 
	 * @param imp
	 * @return
	 */
	public static int getFrameDuration(ImagePlus imp) {
		String property = imp.getInfoProperty();
		int index1 = property.indexOf("0018,1242");
		int index2 = property.indexOf(":", index1);
		int index3 = property.indexOf("\n", index2);
		String tag00181242 = property.substring(index2 + 1, index3).trim();
		return Integer.parseInt(tag00181242);
	}

	/**
	 * Inverts the stack of the specified image.
	 * 
	 * @param ims The image containing <b>only</b> the Post orientation
	 */
	public static void flipStackHorizontal(ImageSelection ims) {
		StackProcessor sp = new StackProcessor(ims.getImagePlus().getImageStack());
		sp.flipHorizontal();
	}

	/**
	 * 
	 * @param imp
	 * @return tableau de duree de l'acquisition de chaque image du dynamique
	 */
	public static int[] buildFrameDurations(ImagePlus imp) {
		int[] frameDurations = new int[imp.getStackSize()];
		int nbPhase;
		if (DicomTools.getTag(imp, "0054,0031") != null) {
			nbPhase = Integer.parseInt(DicomTools.getTag(imp, "0054,0031").trim());
		} else
			nbPhase = 1;

		if (nbPhase == 1) {
			int duration = getFrameDuration(imp);
			for (int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = duration;
			}
		} else {
			String[] phasesStr = DicomTools.getTag(imp, "0054,0030").trim().split(" ");
			int[] phases = new int[phasesStr.length];

			Integer[] durations = Library_Dicom.getDurations(imp);

			for (int i = 0; i < phases.length; i++) {
				phases[i] = Integer.parseInt(phasesStr[i]);
			}

			for (int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = durations[phases[i] - 1];
			}
		}

		return frameDurations;
	}

	/**
	 * @deprecated Internal method, used in
	 *             {@link Library_Dicom#buildFrameDurations()}
	 * 
	 */
	private static Integer[] getDurations(ImagePlus imp) {
		List<Integer> duration = new ArrayList<>();
		String info = imp.getInfoProperty();
		String[] split = info.split("\n");
		for (String s : split) {
			if (s.startsWith("0018,1242")) {
				String[] mots = s.split(" ");
				duration.add(Integer.parseInt(mots[mots.length - 1]));
			}
		}
		return duration.toArray(new Integer[0]);
	}

	/**
	 * Make a projection of an image.<br>
	 * This method returns a clone of the specified image.
	 * 
	 * @param ims        Image to project
	 * @param startSlice Index of slice to project from
	 * @param stopSlice  Index of slice to project to
	 * @param type       Type of projection
	 * @return projected image
	 * @see ZProjector#run(ImagePlus, String, int, int)
	 */
	public static ImageSelection project(ImageSelection ims, int startSlice, int stopSlice, String type) {
		ImageSelection imsProj = ims.clone();
		imsProj.setImagePlus(ZProjector.run(ims.getImagePlus(), type, startSlice, stopSlice));
		imsProj.getImagePlus().setProperty("Info", ims.getImagePlus().getInfoProperty());
		return imsProj;
	}

	/**
	 * Make projection of stack
	 * 
	 * @param imp        : image to project
	 * @param startSlice : first index slice
	 * @param stopSlice  : last index slice
	 * @param type       : "avg" or "max" or "sum"
	 * @return projected imageplus (of all slice)
	 * @deprecated Please use {@link #project(ImageSelection, int, int, String)}
	 *             instead
	 */
	@Deprecated
	public static ImagePlus projeter(ImagePlus imp, int startSlice, int stopSlice, String type) {
		ImagePlus pj = ZProjector.run(imp, type, startSlice, stopSlice);
		pj.setProperty("Info", imp.getInfoProperty());
		System.out.println("Checking for " + imp.getTitle());
		System.out.println("Pj null: " + pj == null);

		return pj;
	}

	/**
	 * This method will always return a clone of the specified ImageSelection in
	 * Ant/Post orientation with the Post image flipped. This ensure the image is in
	 * the right lateralisation.<br>
	 * This method can only be used with Ant/Post or Post/Ant images.
	 * 
	 * @param ims ImageSelection to compute
	 * @return ImageSelection in Ant/Post with Post flipped
	 * @throws WrongOrientationException if the orientation of the image is
	 *                                   different than Ant/Post or Post/Ant
	 * @author Titouan QUÉMA
	 */
	public static ImageSelection ensureAntPostFlipped(ImageSelection ims) throws WrongOrientationException {
		ImageSelection result = ims.clone();
		if (ims.getImageOrientation() == Orientation.POST_ANT) {
			// Reverse
			IJ.run(result.getImagePlus(), "Reverse", "");
			// Flip
			result.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else if (ims.getImageOrientation() == Orientation.ANT_POST) {
			// Flip
			result.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else
			throw new WrongOrientationException(ims.getImageOrientation(),
					new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
		return result;
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 * To avoid a loss of information, we recommand to do this normalization on a 32
	 * bit image. Otherwise, the count are only ineter, and we lose many
	 * informations.
	 * 
	 * @param imp           ImagePlus to normalize
	 * @param framDurations int[] of the ImagePlus duration frames
	 */
	public static void normalizeToCountPerSecond(ImagePlus imp, int[] frameDurations) {
		IJ.run(imp, "32-bit", "");
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			imp.getImageStack().getProcessor(i).multiply(1000d / (double) frameDurations[i - 1]);
		}
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 * To avoid a loss of information, we recommand to do this normalization on a 32
	 * bit image. Otherwise, the count are only ineter, and we lose many
	 * informations.
	 * 
	 * @param imp ImagePlus to normalize
	 * 
	 */
	public static void normalizeToCountPerSecond(ImagePlus imp) {
		int[] frameDurations = Library_Dicom.buildFrameDurations(imp);
		normalizeToCountPerSecond(imp, frameDurations);
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 * To avoid a loss of information, we recommand to do this normalization on a 32
	 * bit image. Otherwise, the count are only ineter, and we lose many
	 * informations.
	 * 
	 * @param imp ImageSelection to normalize
	 * 
	 */
	public static void normalizeToCountPerSecond(ImageSelection imp) {
		normalizeToCountPerSecond(imp.getImagePlus());
	}
	
	public static String findIsotopeCode(ImagePlus imp) {
		String infoProperty = imp.getInfoProperty();
		if (infoProperty != null) {
			Pattern pattern = Pattern.compile("Radionuclide Code Sequence.*Code Value: (C-\\w*)", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(infoProperty);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	public static Isotope findIsotope(ImagePlus imp) {
		return Isotope.getIsotopeFromCode(findIsotopeCode(imp));
	}

}
