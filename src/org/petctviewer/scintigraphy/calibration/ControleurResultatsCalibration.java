package org.petctviewer.scintigraphy.calibration;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

public class ControleurResultatsCalibration implements ItemListener {
	
	FenResultatsCalibration fen;
	
	public ControleurResultatsCalibration(FenResultatsCalibration fen) {
		this.fen = fen;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox selected = (JCheckBox)e.getSource();
		System.out.println("name : "+ selected.getName()+" ?? :"+selected.isSelected());
		this.fen.v(Integer.parseInt(selected.getName().split("\\|")[0]), Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());		
	}
}
