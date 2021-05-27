package org.petctviewer.scintigraphy.scin.library;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Concatenator;
import ij.plugin.ImageCalculator;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import ij.util.DicomTools;
import org.apache.commons.lang.StringUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.IsotopeDialog;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Library_Dicom {

	/**
	 * Get the acquisition date of the specified image.
	 *
	 * @param imp Image to retrieve the date from
	 * @return acquisition date of the image
	 */
	public static Date getDateAcquisition(ImagePlus imp) {
		String aquisitionDate = DicomTools.getTag(imp, "0008,0022");
		String aquisitionTime = DicomTools.getTag(imp, "0008,0032");
		String dateInput = aquisitionDate.trim() + aquisitionTime.trim();

		// On enleve les milisec qui sont inconstantes
		int separateurPoint = dateInput.indexOf(".");
		if (separateurPoint != -1) dateInput = dateInput.substring(0, separateurPoint);

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
	 * Cut an anterior/posterior (or P/A) Image Plus into a Anterior Stack and Posterior Stack A new Image is created
	 * distinct than the original one
	 *
	 * @param imp                Image to split
	 * @param anteriorFirstImage is first image Anterior (shall be tested before splitting)
	 * @return Array ImagePlus Anterior in position 0 and Posterior in position 1
	 * @throws ReadTagException if a DICOM tag could not be retrieve
	 */
	private static ImagePlus[] splitCameraMultiFrame(ImagePlus imp, boolean anteriorFirstImage) throws
			ReadTagException {
		int[] sequenceDetector = Library_Dicom.getCameraNumberArrayMultiFrame(imp);

		// Instantiate header that will receive pixels
		ImageStack camera0 = new ImageStack(imp.getWidth(), imp.getHeight());
		ImageStack camera1 = new ImageStack(imp.getWidth(), imp.getHeight());

		// Get Detector of the first image
		int detectorFirstImage = sequenceDetector[0];
		// Add the image to the correct stack
		if (anteriorFirstImage) {
			for (int i = 0; i < sequenceDetector.length; i++) {
				if (sequenceDetector[i] == detectorFirstImage) {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				}
			}
		} else {
			for (int i = 0; i < sequenceDetector.length; i++) {
				if (sequenceDetector[i] == detectorFirstImage) {
					camera1.addSlice(imp.getImageStack().getProcessor((i + 1)));
				} else {
					camera0.addSlice(imp.getImageStack().getProcessor((i + 1)));
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

		// Copy header in resulting images (correspondence is broken, just made to track patient identity)
		String metadata = imp.getInfoProperty();
		for (ImagePlus camera : cameras) {
			camera.setProperty("Info", metadata);
		}
		return cameras;
	}

	/**
	 * Check is all images came from the same camera
	 *
	 * @throws ReadTagException if a DICOM tag could not be retrieved
	 */
	public static boolean isSameCameraMultiFrame(ImagePlus imp) throws ReadTagException {

		int[] cameraArray = Library_Dicom.getCameraNumberArrayMultiFrame(imp);
		int[] unique = Arrays.stream(cameraArray).distinct().toArray();
		return unique.length == 1;

	}

	private static int getCameraNumberUniqueFrame(ImagePlus imp) throws ReadTagException {
		// Find the camera number in the current image
		String tagVector = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagVector)) tagVector = tagVector.trim();
		else {
			throw new ReadTagException("Camera Number not found");
		}
		return Integer.parseInt(tagVector);
	}

	private static int[] getCameraNumberArrayMultiFrame(ImagePlus imp) throws ReadTagException {

		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (!StringUtils.isEmpty(tagDetecteur)) {
			tagDetecteur = tagDetecteur.trim();
			//For orthanc replace \ by space for uniformity with IJ
			tagDetecteur = tagDetecteur.replaceAll("\\\\", " ");
			String delims = "[ ]+";
			String[] sequenceDeteceur = tagDetecteur.split(delims);
			int[] detectorArray = new int[sequenceDeteceur.length];
			for (int i = 0; i < sequenceDeteceur.length; i++) {
				detectorArray[i] = Integer.parseInt(sequenceDeteceur[i]);
			}
			return detectorArray;

		} else {
			throw new ReadTagException("Camera Number not found");
		}


	}

	/**
	 * Return concatenation of two tag that may contain orientation data (E/F/Ant/Post) If Tag not available return an
	 * empty string (no exception)
	 */
	private static String getOrientationString(ImagePlus imp) {
		String tag = "";
		if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0011,1012"))) {
			tag += DicomTools.getTag(imp, "0011,1012");
		}
		if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0011,1030"))) tag += DicomTools.getTag(imp, "0011,1030");

		return tag;

	}

	/**
	 * Test if first image of stack in anterior for uniqueFrame Images
	 **/
	private static boolean isAnteriorUniqueFrame(ImagePlus imp) throws ReadTagException {
		imp.setSlice(1);

		//Get Tags that may contains orientation informations
		String tag = Library_Dicom.getOrientationString(imp);

		if (tag.contains("ANT") || tag.contains("_E")) {
			return true;
		} else if (tag.contains("POS") || tag.contains("_F")) {
			return false;
		}

		//If no data presume detector 1 is anterior
		int cameraNumber = Library_Dicom.getCameraNumberUniqueFrame(imp);
		System.out.println("Orientation Not recognized, assuming vector 1 is anterior");
		if (cameraNumber == 1) return true;

		return false;

	}

	/**
	 * Test if the first image is Anterior for multi-frame images
	 */
	private static boolean isAnteriorMultiFrame(ImagePlus imp) throws ReadTagException {
		// Only check first image
		imp.setSlice(1);
		String tag = Library_Dicom.getOrientationString(imp);

		// Get the first separator of each view in the orientation field
		int separateur = tag.indexOf("\\");

		// If the separator could not be found, place the position at the end to parse everything
		if (separateur == -1) separateur = (tag.length());

		// Check first image has Anterior label
		if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
			return true;
		}
		// Check first image has Posterior label
		else if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
			return false;
		}

		// If no data matched assume camera 1 is anterior
		int vectorCamera0 = Library_Dicom.getCameraNumberArrayMultiFrame(imp)[0];
		// TODO: warn user of that!
		System.out.println("No localization information assuming vector 1 is Ant and 2 Post");
		if (vectorCamera0 == 1) {
			return true;
		} else if (vectorCamera0 == 2) {
			return false;
		}

		return false;
	}

	/**
	 * Test if first image of the stack is anterior image
	 *
	 * @param imp : ImagePlus input
	 * @return boolean true if anterior
	 */
	public static boolean isAnterior(ImagePlus imp) throws ReadTagException {

		if (Library_Dicom.isMultiFrame(imp)) {
			return isAnteriorMultiFrame(imp);
		} else {
			return isAnteriorUniqueFrame(imp);
		}

	}

	/**
	 * Test if ImagePlus is multi-frame image (same DICOM header for all images)
	 *
	 * @param imp : ImagePlus to Test
	 * @return : boolean true if multi-frame
	 */
	public static boolean isMultiFrame(ImagePlus imp) {
		// Check first slice
		imp.setSlice(1);

		int slices = 0;

		// Check if it is unique or multi frame
		String numFrames = DicomTools.getTag(imp, "0028,0008");

		if (!StringUtils.isEmpty(numFrames)) {
			numFrames = numFrames.trim();
			slices = Integer.parseInt(numFrames);
		}

		return slices != 1;

	}

	/**
	 * Splits the specified image into an array of ImageSelection such as
	 * <ul>
	 * <li>[0]-><code>ANT</code></li>
	 * <li>[1]-><code>POST</code>.</li>
	 * </ul>
	 * The returned images are clones of the input image, and their orientation is updated.
	 *
	 * @param image Dynamic image in DynP/A or DynA/P orientation
	 * @return array of ImageSelection
	 * @throws WrongOrientationException if the image has an orientation different than DYNAMIC_ANT_POST and
	 *                                   DYNAMIC_POST_ANT
	 * @throws IllegalArgumentException  if the image's tag indicates the camera is the same or if it indicates it's
	 * not
	 *                                   a dynamic image
	 * @throws ReadTagException          if the DICOM tags of the image could not be retrieved
	 * @author Titouan QUÉMA
	 */
	public static ImageSelection[] splitDynamicAntPost(ImageSelection image) throws WrongOrientationException,
			IllegalArgumentException, ReadTagException {
		Orientation[] expectedOrientations =
				new Orientation[]{Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT};
		ImagePlus imagePlus = image.getImagePlus();
		if (Arrays.stream(expectedOrientations).noneMatch(i -> i.equals(image.getImageOrientation())))
			throw new WrongOrientationException(image.getImageOrientation(), expectedOrientations);

		if (!isMultiFrame(imagePlus) || isSameCameraMultiFrame(imagePlus)) throw new WrongOrientationException(
				"The image's tag are incorrect and cannot be detected as an " +
						Arrays.toString(Orientation.dynamicOrientations()) + " image!");

		ImageSelection[] result = new ImageSelection[2];
		for (int i = 0; i < result.length; i++)
			result[i] = image.clone();

		ImagePlus[] imageSplit = splitCameraMultiFrame(imagePlus,
													   image.getImageOrientation() == Orientation.DYNAMIC_ANT_POST);
		result[0].setImagePlus(imageSplit[0]);
		result[1].setImagePlus(imageSplit[1]);

		return result;
	}

	/**
	 * Splits the specified image into an array of ImageSelection such as
	 * <ul>
	 * <li>[0]-><code>ANT</code></li>
	 * <li>[1]-><code>POST</code></li>
	 * </ul>
	 * The returned images are clones of the input image, and their orientations are updated.<br> If the input image is
	 * a dynamic, then is method is equivalent to {@link #splitDynamicAntPost(ImageSelection)}.
	 *
	 * @param ims Image to split in A/P, P/A or DynA/P, DynP/A orientation
	 * @return array of ImageSelection [0]=ANT [1]=POST
	 * @throws WrongOrientationException if the image has an orientation different than ANT_POST, POST_ANT,
	 *                                   DYNAMIC_ANT_POST, DYNAMIC_POST_ANT
	 * @throws ReadTagException          if the image tags could not be retrieved
	 * @author Titouan QUÉMA
	 */
	public static ImageSelection[] splitAntPost(ImageSelection ims) throws WrongOrientationException,
			ReadTagException {
		if (ims.getImageOrientation().isDynamic()) return splitDynamicAntPost(ims);

		ImagePlus imp = ims.getImagePlus();
		ImageStack imageAnt = new ImageStack(imp.getWidth(), imp.getHeight()), imagePost = new ImageStack(
				imp.getWidth(), imp.getHeight());


		if (ims.getImageOrientation() == Orientation.ANT_POST) {
			imp.setSlice(1);
			imageAnt.addSlice(imp.getProcessor());
			imp.setSlice(2);
			imagePost.addSlice(imp.getProcessor());
		} else if (ims.getImageOrientation() == Orientation.POST_ANT) {
			imp.setSlice(2);
			imageAnt.addSlice(imp.getProcessor());
			imp.setSlice(1);
			imagePost.addSlice(imp.getProcessor());
		} else throw new WrongOrientationException(ims.getImageOrientation(),
												   new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT,
																	 Orientation.DYNAMIC_ANT_POST,
																	 Orientation.DYNAMIC_POST_ANT});


		ImagePlus impAnt = imp.createImagePlus();
		impAnt.setStack(imageAnt);
		impAnt.getProcessor().convertToFloatProcessor();
		ImageSelection ant = ims.clone(Orientation.ANT);
		ant.setImagePlus(impAnt);

		ImagePlus impPost = imp.createImagePlus();
		impPost.setStack(imagePost);
		impPost.getProcessor().convertToFloatProcessor();
		ImageSelection post = ims.clone(Orientation.POST);
		post.setImagePlus(impPost);

		return new ImageSelection[]{ant, post};
	}

	/**
	 * Recognize and inverse posterior images
	 */
	public static ImagePlus sortImageAntPost(ImagePlus imp) throws ReadTagException {
		return isMultiFrame(imp) ? Library_Dicom.sortAntPostMultiFrame(imp) :
				Library_Dicom.sortAntPostUniqueFrame(imp);
	}


	/**
	 * Flip posterior images for muti-frame
	 */
	private static ImagePlus sortAntPostMultiFrame(ImagePlus imp0) throws ReadTagException {
		// On duplique pour faire les modifs dans l'image dupliqu锟絜
		ImagePlus imp = imp0.duplicate();

		// On prend le Header
		String metadata = imp.getInfoProperty();

		String tag = Library_Dicom.getOrientationString(imp);

		// On recupere la chaine de detecteurER
		int[] tagDetecteurArray = Library_Dicom.getCameraNumberArrayMultiFrame(imp);

		/// On recupere le 1er separateur de chaque vue dans le champ des orientation
		int separateur = tag.indexOf("\\");
		// Si on ne trouve pas le separateur, on met la position du separateur a la fin
		// de la string pour tout traiter
		if (separateur == -1) separateur = (tag.length());

		// Si la 1ere image est labelisee anterieure
		if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
			// On recupere le numero du detecteur
			int detecteurAnterieur = tagDetecteurArray[0];
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ce
			// n'est pas le numero de ce deteceur
			for (int j = 0; j < tagDetecteurArray.length; j++) {
				int detecteur = tagDetecteurArray[j];
				if (detecteur != detecteurAnterieur) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}

		// Si la 1ere image est labelisee posterieurs
		if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
			// on recupere le numero du detecteur posterieur
			int detecteurPosterieur = tagDetecteurArray[0];
			// On parcours la sequence de detecteur et on flip 锟� chaque fois que ca
			// correspond a ce deteceur
			for (int j = 0; j < tagDetecteurArray.length; j++) {
				int detecteur = tagDetecteurArray[j];
				if (detecteur == detecteurPosterieur) {
					imp.getStack().getProcessor(j + 1).flipHorizontal();
				}
			}
		}

		// Si on ne trouve pas de tag on flip toute detecteur 2 et on notifie
		// l'utilisateur
		if (!tag.substring(0, separateur).contains("POS") && !tag.substring(0, separateur).contains("_F") &&
				!tag.substring(0, separateur).contains("ANT") && !tag.substring(0, separateur).contains("_E")) {
			System.out.println(
					"No Orientation tag found, assuming detector 2 is posterior. Please Notify Salim.Kanoun@gmail" +
							".com");
			for (int j = 0; j < tagDetecteurArray.length; j++) {
				int detecteur = tagDetecteurArray[j];
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

		return imp2;

	}


	/**
	 * Flip posterior images for unique frame
	 */
	private static ImagePlus sortAntPostUniqueFrame(ImagePlus imp0) throws ReadTagException {
		// On copie dans une nouvelle image qu'on va renvoyer
		ImagePlus imp = imp0.duplicate();

		// Si unique frame on inverse toute image qui contient une image post锟絩ieure
		for (int i = 1; i <= imp.getImageStackSize(); i++) {
			imp.setSlice(i);
			String tag = Library_Dicom.getOrientationString(imp);


			if (StringUtils.contains(tag, "POS") || StringUtils.contains(tag, "_F")) {
				imp.getProcessor().flipHorizontal();
				imp.setTitle("Post" + i);
			} else if (StringUtils.contains(tag, "ANT") || StringUtils.contains(tag, "_E")) {
				imp.setTitle("Ant" + i);// On ne fait rien
			} else {
				System.out.println(
						"No Orientation found assuming Camera 2 is posterior, please send image sample to " + "Salim" +
								".kanoun@gmail.com if wrong");
				int tagVector = Library_Dicom.getCameraNumberUniqueFrame(imp);
				if (tagVector == 2) {
					imp.getProcessor().flipHorizontal();
				}

			}

		}

		return imp;
	}

	/**
	 * return frame Duration as this tag is stored in sequence tag that are ignored by dicomTools (if multiple the
	 * first
	 * one is sent)
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
	 * @return tableau de duree de l'acquisition de chaque image du dynamique
	 */
	public static int[] buildFrameDurations(ImagePlus imp) {
		int[] frameDurations = new int[imp.getStackSize()];
		int nbPhase;
		if (DicomTools.getTag(imp, "0054,0031") != null) {
			nbPhase = Integer.parseInt(DicomTools.getTag(imp, "0054,0031").trim());
		} else nbPhase = 1;

		if (nbPhase == 1) {
			int duration = getFrameDuration(imp);
			Arrays.fill(frameDurations, duration);
		} else {
			String durationsTag = DicomTools.getTag(imp, "0054,0030").trim();
			//For orthanc replace \ by space for uniformity with IJ
			durationsTag = durationsTag.replaceAll("\\\\", " ");
			String[] phasesStr = durationsTag.split(" ");
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
	 * Make a projection of an image.<br> This method returns a clone of the specified image.
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

	//SK UPDATER LES REFERENCE A CETTE METHODE DEPRECIEE

	/**
	 * Make projection of stack
	 *
	 * @param imp        : image to project
	 * @param startSlice : first index slice
	 * @param stopSlice  : last index slice
	 * @param type       : "avg" or "max" or "sum"
	 * @return projected imageplus (of all slice)
	 */
	@Deprecated
	public static ImagePlus projeter(ImagePlus imp, int startSlice, int stopSlice, String type) {
		ImagePlus pj = ZProjector.run(imp, type, startSlice, stopSlice);
		pj.setProperty("Info", imp.getInfoProperty());
		return pj;
	}

	/**
	 * This method will always return a clone of the specified ImageSelection in Ant/Post orientation.<br> This method
	 * can only be used with Ant/Post or Post/Ant images.
	 *
	 * @param ims ImageSelection to compute
	 * @return ImageSelection in Ant/Post
	 * @throws WrongOrientationException if the orientation of the image is different than Ant/Post or Post/Ant
	 * @author Titouan QUÉMA
	 */
	public static ImageSelection ensureAntPost(ImageSelection ims) throws WrongOrientationException {
		ImageSelection result = ims.clone();
		if (ims.getImageOrientation() == Orientation.POST_ANT) {
			// Reverse
			IJ.run(result.getImagePlus(), "Reverse", "");
		} else if (ims.getImageOrientation() != Orientation.ANT_POST) {
			throw new WrongOrientationException(ims.getImageOrientation(),
												new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT});
		}
		return result;
	}

	/**
	 * This method will always return a clone of the specified ImageSelection in Ant/Post orientation with the Post
	 * image flipped. This ensure the image is in the right lateralisation.<br> This method can only be used with
	 * Ant/Post or Post/Ant images.
	 *
	 * @param ims ImageSelection to compute
	 * @return ImageSelection in Ant/Post with Post flipped
	 * @throws WrongOrientationException if the orientation of the image is different than Ant/Post or Post/Ant
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
		} else throw new WrongOrientationException(ims.getImageOrientation(),
												   new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT});
		return result;
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 *
	 * @param imp            ImagePlus to normalize
	 * @param frameDurations int[] of the ImagePlus duration frames
	 */
	public static void normalizeToCountPerSecond(ImagePlus imp, int[] frameDurations) {
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			imp.getImageStack().getProcessor(i).multiply(1000d / (double) frameDurations[i - 1]);
		}
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 *
	 * @param imp ImagePlus to normalize
	 */
	public static void normalizeToCountPerSecond(ImagePlus imp) {
		int[] frameDurations = Library_Dicom.buildFrameDurations(imp);
		normalizeToCountPerSecond(imp, frameDurations);
	}

	/**
	 * Normalize to have on each frame, the count/second number.
	 * 
	 *
	 * @param imp ImageSelection to normalize
	 */
	public static void normalizeToCountPerSecond(ImageSelection imp) {
		normalizeToCountPerSecond(imp.getImagePlus());
	}

	/**
	 * Returns a geometric mean series from ANT and POST
	 */
	public static ImageSelection geomMean(ImageSelection ims) throws WrongOrientationException, ReadTagException {
		ImagePlus imp = new ImagePlus();
		imp.setProperty("Info", ims.getImagePlus().getInfoProperty());
		ImageStack imStack = new ImageStack();

		ImageSelection[] imsTab = splitAntPost(ims);
		for (int i = 1; i <= imsTab[0].getImagePlus().getNSlices(); i++) {
			imsTab[0].getImagePlus().setSlice(i);
			imsTab[1].getImagePlus().setSlice(i);
			imsTab[1].getImagePlus().getProcessor().flipHorizontal();

			ImagePlus slice = ImageCalculator.run(imsTab[0].getImagePlus(), imsTab[1].getImagePlus(), "Multiply create");
			slice.getProcessor().sqrt();

			imStack.addSlice(slice.getProcessor());
		}
		imp.setStack(imStack);

		return new ImageSelection(imp, ims.getColumns().toArray(new String[0]), ims.getValues().toArray(new String[0]));
	}
	
	
	/**
	 * Create a copy of the ImagePlus and resize it, by resizing every slice of the ImageStack thanks to the ImageProcessor.
	 * @param imagePlus		ImagePlus to resize
	 * @param newWidth		New Width for the resize
	 * @param newHeight		New height for the resize
	 * @return				A <b>new</b> ImagePlus
	 */
	public static ImagePlus resize(ImagePlus imagePlus, int newWidth, int newHeight) {

		ImageStack stack = imagePlus.getStack();
		ImageProcessor ip = imagePlus.getProcessor();

		int nSlices = stack.getSize();

		ImageStack stack2 = new ImageStack(newWidth, newHeight);
		ImageProcessor ip2 = null;
		// Rectangle roi = ip != null ? ip.getRoi() : null;
		if (ip == null)
			ip = stack.getProcessor(1).duplicate();
		try {
			for (int i = 1; i <= nSlices; i++) {
				// showStatus("Resize: ",i,nSlices);
				ip.setPixels(stack.getPixels(1));
				String label = stack.getSliceLabel(1);
				stack.deleteSlice(1);
				ip2 = ip.resize(newWidth, newHeight, true);
				if (ip2 != null)
					stack2.addSlice(label, ip2);
				IJ.showProgress((double) i / nSlices);
			}
			IJ.showProgress(1.0);
		} catch (OutOfMemoryError o) {
			while (stack.getSize() > 1)
				stack.deleteLastSlice();
			IJ.outOfMemory("StackProcessor.resize");
			IJ.showProgress(1.0);
		}

		ImagePlus newImagePlus = new ImagePlus();
		newImagePlus.setStack(stack2);
		newImagePlus.setProcessor(ip2);
		newImagePlus.setProperty("Info", imagePlus.getProperty("Info"));

		return newImagePlus;
	}

	/**
	 * Finds the isotope code in the DICOM header of the specified image. If the code could not be found, then this
	 * method returns null.
	 *
	 * @param imp Image to search the isotope code from
	 * @return string with the isotope code or null if not found
	 */
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

	/**
	 * Finds the isotope in the DICOM header of the specified image. If the code could not be found, then this method
	 * returns null. If the code could be found but doesn't match any known Isotope, then this method returns null.
	 *
	 * @param imp Image to search the isotope from
	 * @return Isotope found or null if not found or unknown
	 */
	public static Isotope findIsotope(ImagePlus imp) {
		return Isotope.getIsotopeFromCode(findIsotopeCode(imp));
	}

	/**
	 * Finds the isotope in the DICOM header of the specified image. If the isotope could not be found (because the
	 * code
	 * was not found or unknown) then this method asks the user to enter a valid isotope. So this method will always
	 * return a valid isotope.
	 *
	 * @param imp Image to search the isotope from
	 * @return Isotope found or entered by the user (never null)
	 */
	public static Isotope getIsotope(ImagePlus imp, FenApplication view) {
		// Find isotope
		String isotopeCode = Library_Dicom.findIsotopeCode(imp);

		if (isotopeCode == null) {
			// No code
			// Ask user for isotope
			IsotopeDialog isotopeDialog = new IsotopeDialog(view);
			isotopeDialog.setVisible(true);
			return isotopeDialog.getIsotope();
		}

		Isotope isotope = Isotope.getIsotopeFromCode(isotopeCode);
		if (isotope == null) {
			// Code unknown
			// Ask user for isotope
			IsotopeDialog isotopeDialog = new IsotopeDialog(view, isotopeCode);
			isotopeDialog.setVisible(true);
			isotope = isotopeDialog.getIsotope();
		}
		return isotope;
	}
}
