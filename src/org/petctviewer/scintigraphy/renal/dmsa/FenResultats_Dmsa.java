package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

public class FenResultats_Dmsa extends JFrame{
	
	private static final long serialVersionUID = 8836086131939302449L;

	public FenResultats_Dmsa(Scintigraphy scin, BufferedImage capture) {
		Modele_Dmsa modele = (Modele_Dmsa) scin.getModele();
		SidePanel side = new SidePanel(getSidePanelContent(modele), "DMSA", scin.getImp());
		side.addCaptureBtn(scin, "");
		
		this.add(new DynamicImage(capture), BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.pack();
		this.setVisible(true);
	}

	public Component getSidePanelContent(Modele_Dmsa modele) {
		Double pctL = modele.getPct()[0] * 100;
		Double pctR = modele.getPct()[1] * 100;
		JPanel flow = new JPanel();

		JLabel lbl_dmsaL = new JLabel("" + Library_Quantif.round(pctL, 1) + "%");
		JLabel lbl_dmsaR = new JLabel("" + Library_Quantif.round(pctR, 1) + "%");
		
		if(pctL > 55 | pctL < 45) {
			lbl_dmsaL.setForeground(Color.RED);
			lbl_dmsaR.setForeground(Color.RED);
		}
		
		JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
		
		grid.add(new JPanel());
		JLabel lbl_l = new JLabel("L");
		lbl_l.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_l);
		
		JLabel lbl_r = new JLabel("R");
		lbl_r.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_r);
		
		grid.add(new JLabel("DMSA"));
		
		lbl_dmsaL.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_dmsaL);
		
		lbl_dmsaR.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_dmsaR);
		
		flow.add(grid);
		return flow;
	}

}
