package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

public class FenResultats_Dmsa extends FenResultatSidePanel{
	
	private static final long serialVersionUID = 8836086131939302449L;

	public FenResultats_Dmsa(VueScin vue, BufferedImage capture) {
		super("DMSA", vue, capture, "");
		this.finishBuildingWindow(true);
		this.setVisible(true);
	}

	@Override
	public Component getSidePanelContent() {
		Modele_Dmsa modele = (Modele_Dmsa) this.getVue().getFenApplication().getControleur().getModele();
		Double pctL = modele.getPct()[0] * 100;
		Double pctR = modele.getPct()[1] * 100;
		JPanel flow = new JPanel();

		JLabel lbl_dmsaL = new JLabel("" + ModeleScin.round(pctL, 1) + "%");
		JLabel lbl_dmsaR = new JLabel("" + ModeleScin.round(pctR, 1) + "%");
		
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
