package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

/**
 * affichage imaage plus avec reglage contraste SK algo contraste à revoir
 * 
 * @author diego
 *
 */
public abstract class PanelImpContrastSlider extends TabResult {
	private ImagePlus imp;
	private DynamicImage dynamicImp;

	private final JLabel sliderLabel;
	protected Box boxSlider;

	final String additionalInfo;
	final String nomFen;

	public PanelImpContrastSlider(String nomFen, String additionalInfo, FenResults parent) {
		super(parent, nomFen, true);
		this.additionalInfo = additionalInfo;
		this.nomFen = nomFen;
		this.parent = parent;

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public void finishBuildingWindow() {
		this.dynamicImp = new DynamicImage(this.imp.getBufferedImage());

		ContrastSlider slider = new ContrastSlider(SwingConstants.HORIZONTAL, imp.duplicate(), this.dynamicImp);

		boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(slider);
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow();
		this.reloadDisplay();
	}

	public ImagePlus getImagePlus() {
		return this.imp;
	}

	@Override
	public Component getSidePanelContent() {
		return this.boxSlider;
	}

	@Override
	public JPanel getResultContent() {
		if (this.imp == null)
			return null;
		return this.dynamicImp;
	}
}
