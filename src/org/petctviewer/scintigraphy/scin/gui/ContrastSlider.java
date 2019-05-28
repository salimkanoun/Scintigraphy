package org.petctviewer.scintigraphy.scin.gui;

import ij.ImagePlus;
import ij.gui.Overlay;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ContrastSlider extends JSlider implements ChangeListener {

	private ImagePlus reference;
	private Overlay overlay;
	private DynamicImage result;

	public ContrastSlider(int orientation, ImagePlus reference, DynamicImage result) {
		super(orientation);

		this.reference = reference;
		this.overlay = reference.getOverlay().duplicate();
		this.result = result;

		reference.killRoi();
		this.setMaximum((int) reference.getStatistics().max);
		this.setMinimum((int) reference.getStatistics().min);
		this.setValue((int) reference.getStatistics().min);

		this.addChangeListener(this);
	}

	public ImagePlus getReference() {
		return this.reference;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		reference.getProcessor().setMinAndMax(0, this.getMaximum() - this.getValue() + 1);
		// Update image overlay
		this.reference.setOverlay(this.overlay);
		this.result.setImage(this.reference.getBufferedImage());
	}
}
