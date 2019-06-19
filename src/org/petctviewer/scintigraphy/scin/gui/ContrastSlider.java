package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.RoiScaler;
import ij.process.ImageProcessor;

/**
 * This component is a slider used to modify the contrast of an ImagePlus.
 *
 * @author Titouan QUÉMA
 */
public class ContrastSlider extends JSlider implements ChangeListener {
	private static final long serialVersionUID = 1L;

	private ImagePlus reference;
	private DynamicImage result;

	private static int IMG_WIDTH = 512;

	public ContrastSlider(ImagePlus image, DynamicImage result) {
		super(JSlider.HORIZONTAL);
		if (image == null)
			throw new IllegalArgumentException("Image cannot be null");

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
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE)
			throw new IllegalArgumentException("The image of the state must be set");

		if (this.reference.getOverlay() != null) {
			int oldWidth = this.reference.getProcessor().getWidth();
			// for(int i = 1 ; i <= this.reference.getStack().size() ; i++) {
			//// this.reference.getStack().setProcessor(this.reference.getStack().getProcessor(i).resize(IMG_WIDTH)
			// , i);
			// this.reference.getStack().getProcessor(i).resize(IMG_WIDTH);
			// }

			// The macro delete the Overlay, so we save data before
			Roi[] rois = this.reference.getOverlay().toArray();

			// IJ.run(this.reference, "Size...", "width=" + IMG_WIDTH + " height=" +
			// (IMG_WIDTH * this.reference.getHeight()/this.reference.getWidth()) + "
			// depth=2 constrain average " + "interpolation=Bilinear");

			ImagePlus temp = this.resize(this.reference, IMG_WIDTH, IMG_WIDTH);

			this.reference.close();
			this.reference = temp;
			this.reference.show();
			this.reference.getWindow().setVisible(false);

			// In case they change the behavior
			if (this.reference.getOverlay() != null)
				this.reference.getOverlay().clear();
			else
				Library_Gui.initOverlay(this.reference);

			Font font = new Font("Arial", Font.PLAIN, Math.round(15));
			this.reference.getOverlay().setLabelFont(font, true);

			Library_Gui.setOverlayTitle(state.title(), this.reference, Color.YELLOW, 1);
			if (state.isLateralisationRL())
				Library_Gui.setOverlayDG(this.reference, Color.YELLOW);
			else
				Library_Gui.setOverlayGD(this.reference, Color.YELLOW);

			// Applying the saved Overlay
			for (Roi roi : rois) {
				if (roi.getName() != null) {
					Roi newRoi = RoiScaler.scale(roi, IMG_WIDTH / oldWidth, IMG_WIDTH / oldWidth, false);
					newRoi.setPosition(0);
					newRoi.setName(roi.getName());
					newRoi.setStrokeColor(Color.YELLOW);
					this.reference.getOverlay().add(newRoi);
				}
			}
		}

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

	public ImagePlus resize(ImagePlus imagePlus, int newWidth, int newHeight) {

		ImageStack stack = imagePlus.getStack();
		ImageProcessor ip = imagePlus.getProcessor();

		int nSlices = stack.getSize();

		ImageStack stack2 = new ImageStack(newWidth, newHeight);
		ImageProcessor ip2 = null;
		// Rectangle roi = ip != null ? ip.getRoi() : null;
		if (ip == null)
			ip = stack.getProcessor(1).duplicate();
		try {
			for (int i = 1; i <= nSlices; i++) {
				// showStatus("Resize: ",i,nSlices);
				ip.setPixels(stack.getPixels(1));
				String label = stack.getSliceLabel(1);
				stack.deleteSlice(1);
				ip2 = ip.resize(newWidth, newHeight, true);
				if (ip2 != null)
					stack2.addSlice(label, ip2);
				IJ.showProgress((double) i / nSlices);
			}
			IJ.showProgress(1.0);
		} catch (OutOfMemoryError o) {
			while (stack.getSize() > 1)
				stack.deleteLastSlice();
			IJ.outOfMemory("StackProcessor.resize");
			IJ.showProgress(1.0);
		}

		ImagePlus newImagePlus = new ImagePlus();
		newImagePlus.setStack(stack2);
		newImagePlus.setProcessor(ip2);
		newImagePlus.setProperty("Info", imagePlus.getProperty("Info"));

		return newImagePlus;
	}

}