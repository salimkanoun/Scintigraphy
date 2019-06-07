package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.RoiScaler;

/**
 * This component is a slider used to modify the contrast of an ImagePlus.
 *
 * @author Titouan QUÉMA
 */
public class ContrastSlider extends JSlider implements ChangeListener {
	private static final long serialVersionUID = 1L;

	private ImagePlus reference;
	private DynamicImage result;


	
	public ContrastSlider(int orientation, ImagePlus reference, DynamicImage result) {
		this(orientation, reference, result, null, null);
	}

	public ContrastSlider(int orientation, ImagePlus reference, DynamicImage result, String titleOverlay, Boolean lateralisationRL) {
		super(orientation);

		this.reference = reference;
		this.result = result;

		this.setMaximum((int) reference.getStatistics().max);
		this.setMinimum((int) reference.getStatistics().min);
		this.setValue((int) reference.getStatistics().min);

		this.addChangeListener(this);

		this.reference.show();
		if(this.reference.getOverlay() != null) {
			int oldWidth = this.reference.getProcessor().getWidth();
			this.reference.setProcessor(this.reference.getProcessor().resize(512));
			Roi[] rois = this.reference.getOverlay().toArray();
			this.reference.getOverlay().clear();
			
			Font font = new Font("Arial", Font.PLAIN, Math.round(15));
			this.reference.getOverlay().setLabelFont(font, true);
			
			Library_Gui.setOverlayTitle(titleOverlay, this.reference, Color.YELLOW, 1);
			if(lateralisationRL)
				Library_Gui.setOverlayDG(this.reference, Color.YELLOW);
			else
				Library_Gui.setOverlayGD(this.reference, Color.YELLOW);
			
			for(Roi roi : rois) {
				if(roi.getName() != null) {
					Roi newRoi = RoiScaler.scale(roi, 512/oldWidth, 512/oldWidth, false);
					newRoi.setName(roi.getName());
					newRoi.setStrokeColor(Color.YELLOW);
					this.reference.getOverlay().add(newRoi);
				}
			}
		}
		
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
