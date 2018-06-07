package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import org.petctviewer.scintigraphy.scin.gui.FenResultatImp;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;

public class TabTimedImage extends FenResultatImp{

	private static final long serialVersionUID = 8125367912250906052L;

	public TabTimedImage(VueScinDyn vue, int rows, int columns, int w, int h) {
		super("Renal scintigraphy", vue, null, "timed");
		
		this.finishBuildingWindow(false);
	
		ImagePlus montage = VueScin.creerMontage(vue.getFrameDurations(), vue.getImp(), h/(columns+1), rows, columns);
		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		
		this.setImp(montage);
		
		this.setPreferredSize(new Dimension(w, h));
		
		this.finishBuildingWindow(true);
	}
}
