package org.petctviewer.scintigraphy.dmsa;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

public class FenResultats_Dmsa extends FenResultatSidePanel{

	public FenResultats_Dmsa(VueScin vue, BufferedImage capture) {
		super("DMSA", vue, capture, "");
		this.setTitle("DMSA");
		this.finishBuildingWindow(true);
		this.setVisible(true);
	}

	@Override
	public Component[] getSidePanelContent() {
		Double pct = ((Modele_Dmsa) this.getVue().getFenApplication().getControleur().getModele()).getPct() * 100;
		JPanel flow = new JPanel();
		JLabel lbl_dmsa = new JLabel("DMSA : " + ModeleScin.round(pct, 1));
		
		if(pct > 55 | pct < 45) {
			lbl_dmsa.setForeground(Color.RED);
		}
		
		flow.add(lbl_dmsa);
		return new Component[] {flow};
	}

}
