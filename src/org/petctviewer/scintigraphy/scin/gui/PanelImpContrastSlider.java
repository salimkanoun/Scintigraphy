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

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

/**
 * affichage imaage plus avec reglage contraste SK algo contraste à revoir
 * 
 * @author diego
 *
 */
public abstract class PanelImpContrastSlider extends TabResult implements ChangeListener {
	private ImagePlus imp;
	private DynamicImage dynamicImp;

	private Scintigraphy scin;
	private JLabel sliderLabel;
	private JSlider slider;
	protected Box boxSlider;

	String additionalInfo, nomFen;

	public PanelImpContrastSlider(String nomFen, Scintigraphy scin, String additionalInfo, FenResults parent) {
		super(parent, nomFen, true);
		this.scin = scin;
		this.additionalInfo = additionalInfo;
		this.nomFen = nomFen;
		this.parent = parent;

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public void finishBuildingWindow() {
		System.out.println(imp.getStatistics().max);
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) imp.getStatistics().max, 4);
		slider.addChangeListener(this);

		boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);

		BufferedImage img;
		// this.imp.show();
		if (this.imp.getCanvas() == null)
			img = this.imp.getBufferedImage();
		else {
			img = Library_Capture_CSV.captureImage(this.imp, 512, 0).getBufferedImage();
			System.out.println("captureImage");
		}

		// this.imp.duplicate().show();
		if (this.dynamicImp == null)
			this.dynamicImp = new DynamicImage(img);
		else
			this.dynamicImp.setImage(img);

		// this.setContrast(this.slider.getValue());
		// this.add(dynamicImp, BorderLayout.CENTER);

		// sidePanel.add(boxSlider);
		// this.parent.setSidePanelContent(boxSlider);

		// sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] {
		// this.slider }, model);
		// this.parent.createCaptureButton();

		// this.add(sidePanel, BorderLayout.EAST);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
	}

	public Box getBoxSlider() {
		return this.boxSlider;
	}

	public JSlider getSlider() {
		return this.slider;
	}

	public Scintigraphy getScin() {
		return scin;
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow();
		this.reloadDisplay();
	}

	public ImagePlus getImagePlus() {
		return this.imp;
	}

	private void setContrast(int sliderValue) {
		imp.getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - sliderValue) + 1);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				dynamicImp.setImage(Library_Capture_CSV.captureImage(imp, 512, 0).getBufferedImage());
				dynamicImp.repaint();
			}
		});

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

	public void setOnlyImp(ImagePlus imp) {
		this.imp = imp;
	}
}
