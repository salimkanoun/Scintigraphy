package org.petctviewer.scintigraphy.calibration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;

public class JTableCheckBox extends JPanel {

	private JPanel tab;
	
	// checkbox à l'interieur du tableau
	private JCheckBox[][] checkboxInterior ;
	
	private JCheckBox[] checkboxHeadRows;
	private JCheckBox[] checkboxHeadCols;
	
	private Component[][] tableauEntier ;
	
	public JTableCheckBox(String[] titleRows, String[] titleCols, ItemListener listener) {
			
		
		// checkbox à l'interieur du tableau
		checkboxInterior = new JCheckBox[titleRows.length][titleCols.length];
		for(int i =0; i< titleRows.length; i++) {
			for(int j =0; j< titleCols.length; j++) {
				JCheckBox ch = new JCheckBox("", true);
				ch.setName(i+"|"+j);
				ch.addItemListener(listener);
				checkboxInterior[i][j] = ch;
			}
		}
		
		//check box en tete des lignes et colonnes
		checkboxHeadCols = new JCheckBox[titleCols.length];
		for(int i=0; i< checkboxHeadCols.length; i++) {
			JCheckBox ch = new JCheckBox("", true);
			ch.setName(i+"");
			ch.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					JCheckBox selected = (JCheckBox)e.getSource();
					if(selected.isSelected()) {
						setStateCheckboxHeadCols(Integer.parseInt(selected.getName()), true);
					}else {
						setStateCheckboxHeadCols(Integer.parseInt(selected.getName()), false);
					}
				}
			});
			checkboxHeadCols[i] = ch;
		}
			
		checkboxHeadRows = new JCheckBox[titleRows.length];
		for(int i=0; i< checkboxHeadRows.length; i++) {
			JCheckBox ch =  new JCheckBox("", true);
			ch.setName(i+"");
			ch.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					JCheckBox selected = (JCheckBox)e.getSource();
					if(selected.isSelected()){
						setStateCheckboxHeadRows(Integer.parseInt(selected.getName()), true);
						/*
						for(int j =0; j< checkboxHeadRows.length;j++) {
							if(checkboxHeadRows[j].equals(e.getSource())) {
								setStateCheckboxHeadRows(j,true);
								System.out.println("lalalall : "+j);
							}
						}*/
					}else{
						setStateCheckboxHeadRows(Integer.parseInt(selected.getName()), false);
					}
				}
			});
			checkboxHeadRows[i] =ch;
		}
				
				
	
		//le tableau contenant tout
		tableauEntier = new Component[titleRows.length+2][titleCols.length+2];
		tableauEntier[0][0] = new JLabel("");
		tableauEntier[0][1] = new JLabel("");
		tableauEntier[1][0] = new JLabel("");
		tableauEntier[1][1] = new JLabel("");

		// ajout de tout dans le tableau entier à la bonne place
		for(int i =0 ;i< checkboxHeadRows.length; i++) {
			tableauEntier[i+2][0] = checkboxHeadRows[i]; 
		}
		
		for(int i =0 ;i< checkboxHeadCols.length; i++) {
			tableauEntier[0][i+2] = checkboxHeadCols[i]; 
		}
		
		for(int i=0; i< titleRows.length; i++) {
			JLabel m = new JLabel(titleRows[i]);
			m.setBorder(BorderFactory.createLineBorder(Color.black,1));
			m.setBackground(Color.LIGHT_GRAY);
			m.setOpaque(true);
			tableauEntier[i+2][1] = m;
		}
		
		for(int i=0; i< titleCols.length; i++) {
			JLabel m = new JLabel(titleCols[i]);
			m.setBorder(BorderFactory.createLineBorder(Color.black,1));
			m.setBackground(Color.LIGHT_GRAY);
			m.setOpaque(true);
			tableauEntier[1][i+2] = m;
		}
		
		for(int i =2; i< titleRows.length+2; i++) {
			for(int j =2; j< titleCols.length+2; j++) {
				tableauEntier[i][j] = checkboxInterior[i-2][j-2];
			}
		}
		
		
		// add finale
		tab = new JPanel(new GridLayout(tableauEntier.length,tableauEntier[0].length));
		tab.setBackground(Color.white);
		tab.setOpaque(true);

		tab.setBorder(BorderFactory.createLineBorder(Color.black,1));
		
		for(int i =0; i< tableauEntier.length; i++) {
			for(int j =0; j< tableauEntier[i].length; j++) {
				if(tableauEntier[i][j] == null) {
					tab.add(new JLabel("null"));
				}else {
					tab.add(tableauEntier[i][j]);
				}
			}
		}
		this.add(tab);
	}
	
	private void setStateCheckboxHeadRows(int ligne, boolean state) {
		for(int i =0; i< checkboxInterior[ligne].length; i++) {
			checkboxInterior[ligne][i].setSelected(state);
		}
	}
	
	private void setStateCheckboxHeadCols(int ligne, boolean state) {
		for(int i =0; i< checkboxInterior.length; i++) {
			checkboxInterior[i][ligne].setSelected(state);
		}
	}


	
	
	
}
