package org.petctviewer.scintigraphy.calibration.resultats;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class ControleurResultatsCalibration implements ChangeListener {
	
	private final FenResultatsCalibration fen;
	private final ModeleResultatsCalibration modele;
	
	public ControleurResultatsCalibration(FenResultatsCalibration fen, ArrayList<ArrayList<HashMap<String, Object>>> arrayList) {
		this.fen = fen;
		this.modele = new ModeleResultatsCalibration(arrayList);
		this.fen.setGraph(this.modele.buildCollection());
		this.fen.setCoef(this.modele.geta(), this.modele.getb());
	}


	
	@Override
	public void stateChanged(ChangeEvent e) {
		//checkbox change
		if(e.getSource() instanceof JCheckBox) {
			JCheckBox selected = (JCheckBox)e.getSource();
			this.modele.actualiserDatasetFromCheckbox(Integer.parseInt(selected.getName().split("\\|")[0]), Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());		
		
			this.fen.setGraph(this.modele.buildCollection());
			
			this.fen.setCoef(this.modele.geta(), this.modele.getb());
		}else 
			
			
			//tab detail click event
			if(e.getSource() instanceof JTabbedPane){
				if(((JTabbedPane)e.getSource()).getSelectedIndex()==1) {
					
					this.modele.runCalculDetails();
					this.fen.setTableDetails(this.modele.getDataDetails(), this.modele.getMoyenneDifferenceDetails());
				}
		}
		
		
		
		
		
		
		
		
		
	}

	
	
	
}
