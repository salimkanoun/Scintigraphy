package org.petctviewer.scintigraphy.scin.library;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.process.LUT;

public class Library_Gui {

	/********************* Public Static ****************************************/

	
	/**
	 *  Change le nom et la couleur de l'overlay
	 * @param ov
	 * @param oldName
	 * @param newName
	 * @param c
	 * 
	 */
	public static void editLabelOverlay(Overlay ov, String oldName, String newName, Color c) {
		Roi roi = ov.get(ov.getIndex(oldName));
		if (roi != null) {
			roi.setName(newName);
			roi.setStrokeColor(c);
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
		Library_Gui.setOverlaySides(overlay, imp, null, "L", "R", 0);
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
		Library_Gui.setOverlaySides(overlay, imp, color, "L", "R", 0);
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
		Library_Gui.setOverlaySides(overlay, imp, null, "R", "L", 0);
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
		Library_Gui.setOverlaySides(overlay, imp, color, "R", "L", 0);
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

}
