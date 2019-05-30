package org.petctviewer.scintigraphy.scin.library;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.process.LUT;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Library_Gui {

	/********************* Public Static ****************************************/

	
	public static final int DEFAULT_FONT_SIZE = 12;

	/**
	 *  Change le studyName et la couleur de l'overlay
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
		// On initialise l'overlay il ne peut y avoir qu'un Overlay
		// pour tout le programme sur lequel on va ajouter/enlever les ROI au fur et a
		// mesure
		Overlay overlay = new Overlay();
		// On defini la police et la propriete des Overlays
		int width = imp.getWidth();
		// On normalise Taille 12 a 256 pour avoir une taille stable pour toute image
		float facteurConversion = (float) ((width * 1.0) / 256);
		Font font = new Font("Arial", Font.PLAIN, Math.round(taillePolice * facteurConversion));
		overlay.setLabelFont(font, true);
		overlay.drawLabels(true);
		overlay.drawNames(true);
		// Pour rendre overlay non selectionnable
		overlay.selectable(false);
		
		imp.setOverlay(overlay);
	
		return overlay;
	}

	/**
	 * Cree overlay et set la police a la taille standard (12) initial de l'Image
	 * 
	 * @return Overlay
	 */
	public static Overlay initOverlay(ImagePlus imp) {
		return initOverlay(imp, DEFAULT_FONT_SIZE);
	}
	
	/** 
	 * Affiche D et G en overlay sur l'image, L a gauche et R a droite
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayGD(ImagePlus imp) {
		Library_Gui.setOverlaySides(imp, null, "L", "R", 0);
	}

	/**
	 * Affiche D et G en overlay sur l'image, L a gauche et R a droite
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 * @param color
	 *            : Couleur de l'overlay
	 */
	public static void setOverlayGD(ImagePlus imp, Color color) {
		Library_Gui.setOverlaySides(imp, color, "L", "R", 0);
	}

	/**
	 * Affiche D et G en overlay sur l'image, R a gauche et L a droite
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 */
	public static void setOverlayDG(ImagePlus imp) {
		Library_Gui.setOverlaySides(imp, null, "R", "L", 0);
	}

	/**
	 * Affiche D et G en overlay sur l'image, R a gauche et L a droite
	 * @param imp
	 *            : ImagePlus sur laquelle est appliqu�e l'overlay
	 * @param color
	 *            : Couleur de l'overlay
	 */
	public static void setOverlayDG(ImagePlus imp, Color color) {
		Library_Gui.setOverlaySides(imp, color, "R", "L", 0);
	}

	public static void setOverlayTitle(String title, ImagePlus imp, Color color, int slice) {
		Overlay overlay = imp.getOverlay();
		
		int w = imp.getWidth();
		//int h = imp.getHeight();
	
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
	
		Rectangle2D bounds = overlay.getLabelFont().getStringBounds(title, frc);
		//double textHeight = bounds.getHeight();
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

	public static void setOverlaySides(ImagePlus imp, Color color, String textL, String textR, int slice) {
		Overlay overlay = imp.getOverlay();
		
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
	
	/**
	 * Applique la LUT definie dans les preference � l'ImagePlus demandee.<br/>
	 * Le paramètre String Lut est le chemin des preferences donnant la Lut à appliquer.
	 * @param imp
	 *            : L'ImagePlus sur laquelle on va appliquer la LUT des preferences
	 * @param Lut
	 *            : chemin des preferences servant à définir la Lut à appliquer
	 */
	public static void setCustomLut(ImagePlus imp,String Lut) {
		String lalut = Prefs.get(Lut, null);
		if (lalut != null) {
			LUT lut = ij.plugin.LutLoader.openLut(lalut);
			imp.setLut(lut);
		}
	}

}
