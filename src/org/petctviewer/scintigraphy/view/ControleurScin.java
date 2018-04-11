package org.petctviewer.scintigraphy.view;

import ij.plugin.frame.RoiManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.petctviewer.scintigraphy.platelet.Modele_Plaquettes;
import org.petctviewer.scintigraphy.platelet.Vue_Plaquettes;

public class ControleurScin implements ActionListener {

	private String[] listeInstructions;
	private VueScin vue;
	private String etat;
	private Modele_Plaquettes leModele;
	private int index = 0;
	protected static boolean showLog;
	private String tagCapture;
	private int cycles = 0;
	protected RoiManager leRoi;

	public ControleurScin(VueScin vueScin, ModeleScin leModele, String[] listeInstructions) {
		this.leRoi = new RoiManager();
	}

	public void actionPerformed(ActionEvent arg0) {
	}

	public String[] getListeInstructions() {
		return null;
	}
}
