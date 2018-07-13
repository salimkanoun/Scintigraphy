package org.petctviewer.scintigraphy.calibration.resultats;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import org.jfree.data.xy.XYSeriesCollection;

public class ControleurResultatsCalibration implements ItemListener {
	
	private FenResultatsCalibration fen;
	private ModeleResultatsCalibration modele;
	
	public ControleurResultatsCalibration(FenResultatsCalibration fen, Doublet[][] data) {
		this.fen = fen;
		this.modele = new ModeleResultatsCalibration(data);
		this.fen.setGraph(this.modele.buildCollection());
		this.fen.setCoef(this.modele.geta(), this.modele.getb());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox selected = (JCheckBox)e.getSource();
		System.out.println("name : "+ selected.getName()+" ?? :"+selected.isSelected());
		
		this.modele.actualiserDatasetFromCheckbox(Integer.parseInt(selected.getName().split("\\|")[0]), Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());		
	
		this.fen.setGraph(this.modele.buildCollection());
		
		this.fen.setCoef(this.modele.geta(), this.modele.getb());
	}
}
