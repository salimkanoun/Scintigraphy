package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.library.Library_Gui;

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
	private String titleOverlay;
	private Boolean lateralisationRL;
	
	public PanelImpContrastSlider(String nomFen, String additionalInfo, FenResults parent) {
		this(nomFen, additionalInfo, parent, null, null);
	}

	public PanelImpContrastSlider(String nomFen, String additionalInfo, FenResults parent, String titleOverlay, Boolean lateralisationRL) {
		super(parent, nomFen, true);
		this.additionalInfo = additionalInfo;
		this.nomFen = nomFen;
		this.parent = parent;
		this.titleOverlay = titleOverlay;
		this.lateralisationRL = lateralisationRL;

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public void finishBuildingWindow() {
		this.dynamicImp = new DynamicImage(this.imp.getBufferedImage());

		ContrastSlider slider = new ContrastSlider(SwingConstants.HORIZONTAL, imp.duplicate(), this.dynamicImp, this.titleOverlay, this.lateralisationRL);

		boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(slider);
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		Library_Gui.setCustomLut(imp);
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
