package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
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

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public abstract class PanelResultatImp extends JPanel implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImagePlus imp;
	private DynamicImage dynamicImp;

	private Scintigraphy vue;
	private JLabel sliderLabel;
	private JSlider slider;
	protected Box boxSlider;

	protected SidePanel sidePanel;
	String additionalInfo, nomFen;

	public PanelResultatImp(String nomFen, Scintigraphy vue, BufferedImage capture, String additionalInfo) {
		super(new BorderLayout());
		this.setVue(vue);
		this.additionalInfo = additionalInfo;
		this.nomFen = nomFen;

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		 sidePanel = new SidePanel(null, nomFen, vue.getImp());
		this.add(sidePanel, BorderLayout.EAST);
	}

	public void finishBuildingWindow() {
		int min = 0;
		int max = 20;
		this.slider = new JSlider(SwingConstants.HORIZONTAL, min, max, 4);
		slider.addChangeListener(this);
		
		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);

		BufferedImage img = this.imp.getBufferedImage();
		if (this.dynamicImp == null) {
			this.dynamicImp = new DynamicImage(img);
			this.setContrast(this.slider.getValue());
			this.add(dynamicImp, BorderLayout.CENTER);
		}

		this.setContrast(slider.getValue());

		 //sidePanel = new SidePanel(boxSlider, nomFen, getVue().getImp());
		sidePanel.addCaptureBtn(getVue(), this.additionalInfo, new Component[] { this.slider });

		this.add(sidePanel, BorderLayout.EAST);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
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

	public Scintigraphy getVue() {
		return vue;
	}
	
	
	
	
	
	
	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow();
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

	public void setVue(Scintigraphy vue) {
		this.vue = vue;
	}
}
