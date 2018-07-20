package org.petctviewer.scintigraphy.calibration.chargement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.petctviewer.scintigraphy.calibration.resultats.FenResultatsCalibration;

public class ControleurChargementCalibration implements ActionListener{

	private ModeleChargementCalibration modele ;
	private FenChargementCalibration fenCharg ;
	
	public ControleurChargementCalibration( FenChargementCalibration fenChargementCalibration) {
		this.fenCharg = fenChargementCalibration;
	}

	//quand le calcul termine
	@Override
	public void actionPerformed(ActionEvent e) {		
		this.modele = new ModeleChargementCalibration(fenCharg);	
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				modele.runCalcul();			
				FenResultatsCalibration fen = new FenResultatsCalibration(modele.getDonnees2());
				fen.setVisible(true);
			}
		});
		t.start();
		
		
	}
	

	
	
	
}


