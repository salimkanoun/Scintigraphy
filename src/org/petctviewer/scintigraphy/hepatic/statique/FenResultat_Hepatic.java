package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class FenResultat_Hepatic extends FenResults {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Hepatic(Scintigraphy scin, BufferedImage capture, ControleurScin controller) {
		super(controller);
		this.setLayout(new BorderLayout());
		this.resultats = ((Modele_Hepatic) controller.getModel()).getResultsHashMap();
		
		this.addTab(new TabMain(this, "Result",true,capture));
	}

	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		
		panel.add(new JLabel(""));
		for(String k : this.resultats.keySet()) {
			panel.add(getLabel(k));
		}
		
		JPanel flow = new JPanel();
		flow.add(panel);
	
		return flow;
	}
	
	private JLabel getLabel(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.get(key));
		return lbl_hwb;
	}
	
	private class TabMain extends TabResult{
		
		BufferedImage capture;
		
		public TabMain(FenResults parent, String title, boolean captureBtn, BufferedImage capture) {
			super(parent, title, captureBtn);
			this.capture = capture;
		}

		@Override
		public Component getSidePanelContent() {
			return FenResultat_Hepatic.this.getSidePanelContent();
		}
		
		@Override
		public JPanel getResultContent() {
			return new DynamicImage(capture);
		}
		
	}

}
