package org.petctviewer.scintigraphy.scin;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

public abstract class ModeleScin {

	private Integer uid;

	/*********** Public Abstract *********/
	/**
	 * Enregistrer la mesure de la roi courante de l'image plus dans le format
	 * souhait�
	 * 
	 * @param nomRoi
	 *            nom de la roi presente sur l'image plus
	 * @param imp
	 *            ImagePlus a traiter
	 */
	public abstract void enregistrerMesure(String nomRoi, ImagePlus imp);

	public abstract void calculerResultats();

	/**
	 * calcule la decay fraction (countsCorrected=counts/decayedFraction)
	 * 
	 * @param delaySeconds
	 * @param halLifeSeconds
	 * @return
	 */
	public static double getDecayFraction(int delaySeconds, int halLifeSeconds) {
		double tcLambdaSeconds = (Math.log(2) / (halLifeSeconds));
		double decayedFraction = Math.pow(Math.E, (tcLambdaSeconds * delaySeconds * (-1)));
		// Decayed fraction est la fraction de la radioactivit� qui a disparu
		// Pour avoir les coups corrige de la decroissance
		// countsCorrected=counts/decayedFraction
		return decayedFraction;
	}
	
	
	
	/********** Public *******************/
	private String generateUID6digits() {
		if (this.uid == null) {
			this.uid = (int) (Math.random() * 1000000.);
		}
		return this.uid.toString();
	}
	
	/**
	 * Permet de generer la 1ere partie du Header qui servira a la capture finale,
	 * l'iud est genere aleatoirement au premier appel de la fonction et reste le meme
	 * pour tout le modele
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
	public String genererDicomTagsPartie1SameUID(ImagePlus imp, String nomProgramme) {
		String uid = generateUID6digits();
		return Library_Capture_CSV.genererDicomTagsPartie1(imp, nomProgramme, uid);
	}
	
	
	
}