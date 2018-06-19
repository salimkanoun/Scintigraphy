package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
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

import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public abstract class FenResultatImp extends FenResultatSidePanel implements ChangeListener {

	private ImagePlus imp;
	private DynamicImage dynamicImp;

	private Scintigraphy vue;
	private JLabel sliderLabel;
	private JSlider slider;
	private Box boxSlider;

	public FenResultatImp(String nomFen, Scintigraphy vue, BufferedImage capture, String additionalInfo) {
		super(nomFen, vue, capture, additionalInfo);
		this.vue = vue;

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.slider = initSlider();

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);
	}

	@Override
	public void finishBuildingWindow(boolean capture) {
		if (this.imp != null) {
			BufferedImage img = this.imp.getBufferedImage();
			if (this.dynamicImp == null) {
				this.dynamicImp = new DynamicImage(img);
				this.setContrast(this.slider.getValue());
				this.add(dynamicImp, BorderLayout.CENTER);
			}
		}

		super.finishBuildingWindow(capture);
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow(false);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
	}

	@Override
	public Component getSidePanelContent() {
		if (this.imp != null) {
			this.setContrast(slider.getValue());
			return boxSlider;
		}
		return null;
	}

	private JSlider initSlider() {
		int min = 0;
		int max = 20;
		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, 4);
		slider.addChangeListener(this);

		return slider;
	}

	private void setContrast(int contrast) {
		ContrastEnhancer ce = new ContrastEnhancer();
		ce.stretchHistogram(this.imp, contrast);

		try {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					dynamicImp.setImage(imp.getBufferedImage());
					dynamicImp.repaint();
				}
			});
		} catch (@SuppressWarnings("unused") Exception e1) {
			// vide
		}
	}

	public ImagePlus getImagePlus() {
		return this.imp;
	}

	public Box getBoxSlider() {
		return this.boxSlider;
	}

	public JSlider getSlider() {
		return this.slider;
	}

	@Override
	public void setCaptureButton(JButton btn_capture, JLabel lbl_credits) {
		// on ajoute le listener pour la capture
		Component[] show = new Component[] { lbl_credits };
		Component[] hide = new Component[] { btn_capture, this.slider, this.sliderLabel };

		this.vue.setCaptureButton(btn_capture, show, hide, this,
				this.vue.getFenApplication().getControleur().getModele(), "");
	}

}
