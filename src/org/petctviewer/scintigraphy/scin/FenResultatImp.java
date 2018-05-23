package org.petctviewer.scintigraphy.scin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public abstract class FenResultatImp extends FenResultatSidePanel implements ChangeListener {

	private ImagePlus imp;
	private ImageIcon icon;
	private JLabel lbl;
	
	public FenResultatImp(String nomFen, VueScin vueScin, BufferedImage capture, String additionalInfo) {
		super(nomFen, vueScin, capture, additionalInfo);
	}

	@Override
	public void finishBuildingWindow(boolean capture) {
		if(this.imp != null) {
			JPanel flow = new JPanel();
			this.lbl = new JLabel();
			BufferedImage img = this.imp.getBufferedImage();
			this.icon = new ImageIcon(img);
			lbl.setIcon(icon);
			flow.add(lbl);
			
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
			
			JLabel sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
	        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, 4);
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
					lbl.setIcon(icon);
					lbl.repaint();
				}
			});
		} catch (@SuppressWarnings("unused") Exception e1) {
			//vide
		}
	}

}
