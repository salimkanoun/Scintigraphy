package org.petctviewer.scintigraphy.calibration;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

public class ControleurResultatsCalibration implements ItemListener {
	
	private FenResultatsCalibration fen;
	private ModeleResultatsCalibration modele;
	
	public ControleurResultatsCalibration(FenResultatsCalibration fen, Doublet[][] data) {
		this.fen = fen;
		this.modele = new ModeleResultatsCalibration(data);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox selected = (JCheckBox)e.getSource();
		System.out.println("name : "+ selected.getName()+" ?? :"+selected.isSelected());
		this.fen.actualiserDatasetFromCheckbox(Integer.parseInt(selected.getName().split("\\|")[0]), Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());		
	}
}
