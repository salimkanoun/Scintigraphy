package org.petctviewer.scintigraphy.calibration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ControleurCalibration implements ActionListener{

	ModeleCalibration modele ;
	FenResultatsCalibration fen ;
	public ControleurCalibration(ArrayList<String[]> examList, FenResultatsCalibration fenResultatsCalibration) {
		this.modele = new ModeleCalibration(examList);
		this.fen = fenResultatsCalibration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		fen.afficherDonnee(this.modele.getDonnees());
	}
	
}
