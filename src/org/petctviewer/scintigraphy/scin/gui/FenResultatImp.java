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

import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public abstract class FenResultatImp extends FenResultatSidePanel implements ChangeListener {

	private ImagePlus imp;
	private ImageIcon icon;
	private JLabel lbl_icon;
	private VueScin vue;
	private JLabel sliderLabel;
	private JSlider slider;
	
	public FenResultatImp(String nomFen, VueScin vue, BufferedImage capture, String additionalInfo) {
		super(nomFen, vue, capture, additionalInfo);
		this.vue = vue;
	}

	@Override
	public void finishBuildingWindow(boolean capture) {
		if(this.imp != null) {
			JPanel flow = new JPanel();
			this.lbl_icon = new JLabel();
			BufferedImage img = this.imp.getBufferedImage();
			this.icon = new ImageIcon(img);
			lbl_icon.setIcon(icon);
			flow.add(lbl_icon);
			
			Box vertical = Box.createVerticalBox();
			vertical.add(Box.createVerticalGlue());
			vertical.add(flow);
			vertical.add(Box.createVerticalGlue());			

			this.add(vertical, BorderLayout.CENTER);
		}
		
		this.add(new JPanel(), BorderLayout.WEST);
		
		super.finishBuildingWindow(capture);
	}
	
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
	}
	
	@Override
	public Component[] getSidePanelContent() {
		if(this.imp != null) {
			int min = 0;
			int max = 20;
			
			this.imp.getProcessor().convertToRGB();
			
			Box boxSlider = Box.createVerticalBox();
			
			this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
	        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.slider = new JSlider(SwingConstants.HORIZONTAL, min, max, 4);
			this.setContrast(slider.getValue());
			
			boxSlider.add(sliderLabel);
			boxSlider.add(slider);
			
			slider.addChangeListener(this);
			
			return new Component[] {boxSlider};
		}
		return null;
	}
	
	private void setContrast(int contrast) {
		ContrastEnhancer ce = new ContrastEnhancer();
		ce.stretchHistogram(this.imp,contrast);
		
		this.icon.setImage(this.imp.getBufferedImage());
		
		try {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					lbl_icon.setIcon(icon);
					lbl_icon.repaint();
				}
			});
		} catch (@SuppressWarnings("unused") Exception e1) {
			//vide
		}
	}
	
	@Override
	public void setCaptureButton(JButton btn_capture, JLabel lbl_credits) {
		// on ajoute le listener pour la capture
		Component[] show = new Component[] {lbl_credits};
		Component[] hide = new Component[] {btn_capture, this.slider, this.sliderLabel};
		
		this.vue.setCaptureButton(btn_capture, show, hide, this, this.vue.getFenApplication().getControleur().getModele(), "");
	}

}
