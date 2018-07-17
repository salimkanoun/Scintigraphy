package org.petctviewer.scintigraphy.calibration.chargement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.petctviewer.scintigraphy.calibration.resultats.FenResultatsCalibration;

public class ControleurChargementCalibration implements ActionListener{

	ModeleChargementCalibration modele ;
	FenChargementCalibration fenCharg ;
	
	public ControleurChargementCalibration( FenChargementCalibration fenChargementCalibration) {
		this.fenCharg = fenChargementCalibration;
	}

	//calcul temrine
	@Override
	public void actionPerformed(ActionEvent e) {
		//fen.lancerRes(this.modele.getDonnees());
		//fenCharg.lancerRes(ModeleChargementCalibration.setDonnees());
		
		
		
		 modele = new ModeleChargementCalibration(this.fenCharg.getExamList());
		 FenResultatsCalibration fen = new FenResultatsCalibration(this.modele.getDonnees2());
		 fen.setVisible(true);
		//FenResultatsCalibration fen = new FenResultatsCalibration(ModeleChargementCalibration.setDonnees());
		//fen.setVisible(true);
	
	} 

	
	
	
}


