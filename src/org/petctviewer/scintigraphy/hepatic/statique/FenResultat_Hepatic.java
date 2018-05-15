package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.VueScin;

import java.awt.image.BufferedImage;

public class FenResultat_Hepatic extends FenResultatSidePanel {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Hepatic(VueScin vueScin, BufferedImage capture) {
		super(vueScin.getExamType(), vueScin, capture, "");
		this.resultats = vueScin.getFen_application().getControleur().getModele().getResultsHashMap();
		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		Component[] c = new Component[1];
		
		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		
		panel.add(new JLabel(""));
		for(String k : this.resultats.keySet()) {
			panel.add(getLabel(k));
		}
		
		JPanel flow = new JPanel();
		flow.add(panel);
		
		c[0] = flow;
		return c;
	}
	
	private JLabel getLabel(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.get(key));
		return lbl_hwb;
	}

}
