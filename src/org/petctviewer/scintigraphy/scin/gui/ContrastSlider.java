package org.petctviewer.scintigraphy.scin.gui;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This component is a slider used to modify the contrast of an ImagePlus.
 *
 * @author Titouan QUÉMA
 */
public class ContrastSlider extends JSlider implements ChangeListener {
	private static final long serialVersionUID = 1L;

	private ImagePlus reference;
	private DynamicImage result;

	public ContrastSlider(ImagePlus image, DynamicImage result) {
		super(JSlider.HORIZONTAL);
		if (image == null) throw new IllegalArgumentException("Image cannot be null");

		this.reference = image;
		this.result = result;

		this.setMaximum((int) image.getStatistics().max);
		this.setMinimum((int) image.getStatistics().min);
		this.setValue((int) image.getStatistics().min);

		this.addChangeListener(this);

		image.show();
		image.getWindow().setVisible(false);

		this.stateChanged(null);
	}

	public ContrastSlider(ImageState state, DynamicImage result) {
		this(state.getImage().getImagePlus(), result);
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE) throw new IllegalArgumentException(
				"The image of the state must be set");

//		if (this.reference.getOverlay() != null) {
//			int oldWidth = this.reference.getProcessor().getWidth();
//			this.reference.setProcessor(this.reference.getProcessor().resize(512));
//			Roi[] rois = this.reference.getOverlay().toArray();
//			this.reference.getOverlay().clear();
//
//			Font font = new Font("Arial", Font.PLAIN, Math.round(15));
//			this.reference.getOverlay().setLabelFont(font, true);
//
//			Library_Gui.setOverlayTitle(state.title(), this.reference, Color.YELLOW, 1);
//			if (state.isLateralisationRL()) Library_Gui.setOverlayDG(this.reference, Color.YELLOW);
//			else Library_Gui.setOverlayGD(this.reference, Color.YELLOW);
//
//			for (Roi roi : rois) {
//				if (roi.getStudyName() != null) {
//					Roi newRoi = RoiScaler.scale(roi, 512 / oldWidth, 512 / oldWidth, false);
//					newRoi.setName(roi.getStudyName());
//					newRoi.setStrokeColor(Color.YELLOW);
//					this.reference.getOverlay().add(newRoi);
//				}
//			}
//		}

		this.stateChanged(null);
	}

	public ImagePlus getAssociatedImage() {
		return this.reference;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.reference.getProcessor().setMinAndMax(0, this.getMaximum() - this.getValue() + 1);
		// Update image overlay
		this.reference.updateAndDraw();
		this.result.setImage(Library_Capture_CSV.captureImage(this.reference, 0, 0).getBufferedImage());
	}
}
