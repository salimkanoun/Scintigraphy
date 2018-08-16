package org.petctviewer.scintigraphy.calibration.resultats;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControleurResultatsCalibration implements ChangeListener {
	
	private FenResultatsCalibration fen;
	private ModeleResultatsCalibration modele;
	
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
			//System.out.println("name : "+ selected.getName()+" ?? :"+selected.isSelected());
			this.modele.actualiserDatasetFromCheckbox(Integer.parseInt(selected.getName().split("\\|")[0]), Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());		
		
			this.fen.setGraph(this.modele.buildCollection());
			
			this.fen.setCoef(this.modele.geta(), this.modele.getb());
		}else 
			
			
			//tab detail click event

			if(e.getSource() instanceof JTabbedPane){
				if(((JTabbedPane)e.getSource()).getSelectedIndex()==1) {
					
					this.modele.runCalculDetails();
					this.fen.setTableDetails(this.modele.getDataDetails(), this.modele.getMoyenneDifferenceDetails());
					//System.out.println("Changement d'inglet"+((JTabbedPane)e.getSource()).getSelectedIndex());
				}
		}
		
		
		
		
		
		
		
		
		
	}

	
	
	
}
