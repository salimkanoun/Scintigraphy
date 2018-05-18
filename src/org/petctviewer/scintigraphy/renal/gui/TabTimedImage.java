package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImagePanel;
import ij.plugin.ContrastEnhancer;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.ImageProcessor;

public class TabTimedImage extends FenResultatSidePanel implements ChangeListener{

	private ImagePlus montage;
	private ImageIcon icon;
	private JLabel lbl_montage;
	
	public TabTimedImage(VueScin vueScin, int height, int rows, int columns) {
		super("Renal scintigraphy", vueScin, null, "");
		this.montage = FenResultatSidePanel.creerMontage(ModeleScinDyn.FRAMEDURATION, vueScin.getImp(), height, rows, columns);
		this.montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		
		JPanel flow = new JPanel();
		
		this.lbl_montage = new JLabel();
		
		this.icon = new ImageIcon(montage.getBufferedImage());
		lbl_montage.setIcon(icon);
		
		flow.add(lbl_montage);
		
		Box vertical = Box.createVerticalBox();
		vertical.add(Box.createVerticalGlue());
		vertical.add(flow);
		vertical.add(Box.createVerticalGlue());
		
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(vertical, BorderLayout.CENTER);

		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		ContrastAdjuster contrastAdj = new ContrastAdjuster();
		contrastAdj.run("");
		
		int min = 0;
		int max = 20;
		
		this.montage.getProcessor().convertToRGB();
		
		Box boxSlider = Box.createVerticalBox();
		
		JLabel sliderLabel = new JLabel("Contrast", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, (max + min)/2);
		
		boxSlider.add(sliderLabel);
		boxSlider.add(slider);
		
		slider.addChangeListener(this);
		
		return new Component[] {boxSlider};
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		
		ContrastEnhancer ce = new ContrastEnhancer();
		ce.stretchHistogram(this.montage, slider.getValue());
		
		this.icon.setImage(this.montage.getBufferedImage());
		
		try {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					lbl_montage.setIcon(icon);
					lbl_montage.repaint();
				}
			});
		} catch (Exception e1) {
			//vide
		}
	}

}
