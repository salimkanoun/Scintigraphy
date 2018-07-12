package org.petctviewer.scintigraphy.calibration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;

public class ControleurChargementCalibration implements ActionListener{

	ModeleCalibration modele ;
	FenChargementCalibration fen ;
	
	public ControleurChargementCalibration(ArrayList<String[]> examList, FenChargementCalibration fenChargementCalibration) {
		//this.modele = new ModeleCalibration(examList);
		this.fen = fenChargementCalibration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//fen.lancerRes(this.modele.getDonnees());
		fen.lancerRes(ModeleCalibration.setDonnees());
	}

	
	
}
