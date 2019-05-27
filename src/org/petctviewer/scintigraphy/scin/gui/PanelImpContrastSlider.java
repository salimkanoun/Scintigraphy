package org.petctviewer.scintigraphy.scin.gui;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * affichage imaage plus avec reglage contraste 
 * SK algo contraste à revoir
 * @author diego
 *
 */
public abstract class PanelImpContrastSlider extends TabResult implements ChangeListener {
	private ImagePlus imp;
	private DynamicImage dynamicImp;

	private final Scintigraphy scin;
	private final JLabel sliderLabel;
	private JSlider slider;
	protected Box boxSlider;
	
	final String additionalInfo;
	final String nomFen;

	public PanelImpContrastSlider(String nomFen, Scintigraphy scin, String additionalInfo, FenResults parent) {
		super(parent, nomFen, true);
		this.scin=scin;
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

		BufferedImage img = this.imp.getBufferedImage();
		if (this.dynamicImp == null) {
			this.dynamicImp = new DynamicImage(img);
			this.setContrast(this.slider.getValue());
//			this.add(dynamicImp, BorderLayout.CENTER);
		}

		this.setContrast(slider.getValue());
		
//		sidePanel.add(boxSlider);
//		this.parent.setSidePanelContent(boxSlider);

//		sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] { this.slider }, model);
//		this.parent.createCaptureButton();

//		this.add(sidePanel, BorderLayout.EAST);
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
		imp.getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - sliderValue)+1);
		
		SwingUtilities.invokeLater(() -> {
			dynamicImp.setImage(imp.getBufferedImage());
			dynamicImp.repaint();
		});

	}

	@Override
	public Component getSidePanelContent() {
		return this.boxSlider;
	}

	@Override
	public JPanel getResultContent() {
		if(this.imp == null)
			return null;
		return this.dynamicImp;
	}
	
	public void setOnlyImp(ImagePlus imp){
		this.imp = imp;
	}
}
