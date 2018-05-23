package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.FenResultatImp;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;

public class TabTimedImage extends FenResultatImp{

	private static final long serialVersionUID = 8125367912250906052L;

	public TabTimedImage(VueScin vueScin, int height, int rows, int columns) {
		super("Renal Scintigraphy", vueScin, null, "");
		ImagePlus montage = FenResultatSidePanel.creerMontage(ModeleScinDyn.FRAMEDURATION, vueScin.getImp(), height, rows, columns);
		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		
		this.setImp(montage);
		
		this.finishBuildingWindow(true);
	}

}
