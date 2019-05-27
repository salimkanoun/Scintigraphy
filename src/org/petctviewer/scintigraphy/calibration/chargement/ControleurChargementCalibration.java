package org.petctviewer.scintigraphy.calibration.chargement;

import org.petctviewer.scintigraphy.calibration.resultats.FenResultatsCalibration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurChargementCalibration implements ActionListener{

	private ModeleChargementCalibration modele ;
	private final FenChargementCalibration fenCharg ;
	
	public ControleurChargementCalibration( FenChargementCalibration fenChargementCalibration) {
		this.fenCharg = fenChargementCalibration;
	}

	//quand le calcul termine
	@Override
	public void actionPerformed(ActionEvent e) {		
		this.modele = new ModeleChargementCalibration(fenCharg);	
		
		Thread t = new Thread(() -> {
			modele.runCalcul();
			FenResultatsCalibration fen = new FenResultatsCalibration(modele.getDonnees2());
			fen.setVisible(true);
		});
		t.start();
		
		
	}
	

	
	
	
}


