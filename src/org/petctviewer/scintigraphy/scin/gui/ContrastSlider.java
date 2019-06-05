package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

public class ContrastSlider extends JSlider implements ChangeListener {
	private static final long serialVersionUID = 1L;

	private ImagePlus reference;
	private DynamicImage result;

	public ContrastSlider(int orientation, ImagePlus reference, DynamicImage result) {
		super(orientation);

		this.reference = reference;
		this.result = result;

		this.setMaximum((int) reference.getStatistics().max);
		this.setMinimum((int) reference.getStatistics().min);
		this.setValue((int) reference.getStatistics().min);

		this.addChangeListener(this);

		this.reference.show();
		this.reference.getWindow().setVisible(false);

		this.stateChanged(null);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		reference.getProcessor().setMinAndMax(0, this.getMaximum() - this.getValue() + 1);
		// Update image overlay
		this.reference.updateAndDraw();
		this.result.setImage(Library_Capture_CSV.captureImage(this.reference, 0, 0).getBufferedImage());
	}
}
