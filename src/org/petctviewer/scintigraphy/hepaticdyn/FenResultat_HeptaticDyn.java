package org.petctviewer.scintigraphy.hepaticdyn;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ij.ImagePlus;
import ij.plugin.ZProjector;

public class FenResultat_HeptaticDyn extends JFrame{

	private static final long serialVersionUID = 1094251580157650693L;
	
	public FenResultat_HeptaticDyn(Vue_HepaticDyn vue, BufferedImage capture) {
		this.setLayout(new GridLayout(2,2));
		System.out.println(capture.getHeight());
		JLabel cpt = new JLabel();
		cpt.setIcon(new ImageIcon(capture));
		this.add(cpt);
		
		JPanel northEast = new JPanel(new GridLayout(4,4));
		int nSlice = vue.getImpAnt().getStackSize();
		for(int i = 0; i < 16; i++) {
			int start = (nSlice/16)*i;
			int stop = start + (nSlice/16);
			ImagePlus projectionImp = ZProjector.run(vue.getImpAnt(), "max", start, stop);
			BufferedImage projection = projectionImp.getBufferedImage();
			JLabel proj = new JLabel();
			cpt.setIcon(new ImageIcon(projection));
			northEast.add(proj);
		}
		
		this.pack();
		this.setVisible(true);
		this.setSize(this.getPreferredSize());
		this.setLocationRelativeTo(null);	
	}

}
