package org.petctviewer.scintigraphy.calibration.resultats;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class JTableCheckBox extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// checkbox à l'interieur du tableau
	private final JCheckBox[][] checkboxInterior ;
	
	private final JCheckBox[] checkboxHeadRows;
	private final JCheckBox[] checkboxHeadCols;

	public JTableCheckBox(String[] titleRows, String[] titleCols, ChangeListener listener) {
			
		
		// checkbox à l'interieur du tableau
		checkboxInterior = new JCheckBox[titleRows.length][titleCols.length];
		for(int i =0; i< titleRows.length; i++) {
			for(int j =0; j< titleCols.length; j++) {
				JCheckBox ch = new JCheckBox("", true);
				ch.setName(i+"|"+j);// qui sera splité dans le controleur pour recuprer la position du bouton
				ch.addChangeListener(listener);
				ch.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {

						int posX = Integer.parseInt(((JCheckBox)e.getSource()).getName().split("\\|")[0]);
						int posY = Integer.parseInt(((JCheckBox)e.getSource()).getName().split("\\|")[1]);

						
						//controle que toute la ligne est deselectionnee
						boolean testLigne = true;
						for(int i =0; i< JTableCheckBox.this.checkboxInterior[posX].length; i++)
							if (!JTableCheckBox.this.checkboxInterior[posX][i].isSelected()) testLigne = false;
						JTableCheckBox.this.checkboxHeadRows[posX].setSelected(testLigne);
						
						
						
						//controle que toute la colonne est deselectionnee
						boolean testColonne = true;
						for (JCheckBox[] jCheckBoxes : JTableCheckBox.this.checkboxInterior)
							if (!jCheckBoxes[posY].isSelected()) testColonne = false;
						JTableCheckBox.this.checkboxHeadCols[posY].setSelected(testColonne);
						
					}
				});
				
				
				
				checkboxInterior[i][j] = ch;
			}
		}
		
		//check box en tete des colonnes
		checkboxHeadCols = new JCheckBox[titleCols.length];
		for(int i=0; i< checkboxHeadCols.length; i++) {
			JCheckBox ch = new JCheckBox("", true);
			ch.setName(i+"");
			ch.addActionListener(e -> {
				JCheckBox selected = (JCheckBox)e.getSource();
				setStateCheckboxHeadCols(Integer.parseInt(selected.getName()), selected.isSelected());
			});
			checkboxHeadCols[i] = ch;
		}
			
		//check box en tete des lignes 
		checkboxHeadRows = new JCheckBox[titleRows.length];
		for(int i=0; i< checkboxHeadRows.length; i++) {
			JCheckBox ch =  new JCheckBox("", true);
			ch.setName(i+"");
			ch.addActionListener(e -> {
				JCheckBox selected = (JCheckBox)e.getSource();
				/*
					for(int j =0; j< checkboxHeadRows.length;j++) {
						if(checkboxHeadRows[j].equals(e.getSource())) {
							setStateCheckboxHeadRows(j,true);
						}
					}*/
				setStateCheckboxHeadRows(Integer.parseInt(selected.getName()), selected.isSelected());
			});
			checkboxHeadRows[i] =ch;
		}
				
				
	
		//le tableau contenant tout (head col, head row , title row, title col, tableau interieur)
		Component[][] tableauEntier = new Component[titleRows.length + 2][titleCols.length + 2];
		tableauEntier[0][0] = new JLabel("");
		tableauEntier[0][1] = new JLabel("");
		tableauEntier[1][0] = new JLabel("");
		tableauEntier[1][1] = new JLabel("");

		// ajout de tout dans le tableau entier à la bonne place
		//chekbox entete des ligne
		for(int i =0 ;i< checkboxHeadRows.length; i++) {
			tableauEntier[i+2][0] = checkboxHeadRows[i]; 
		}
		//checkbox entete des colonnes
		System.arraycopy(checkboxHeadCols, 0, tableauEntier[0], 2, checkboxHeadCols.length);
		//titre des lignes
		for(int i=0; i< titleRows.length; i++) {
			JLabel m = new JLabel(titleRows[i]);
			m.setBorder(BorderFactory.createLineBorder(Color.black,1));
			m.setBackground(Color.LIGHT_GRAY);
			m.setOpaque(true);
			tableauEntier[i+2][1] = m;
		}
		//titre des colonnes
		for(int i=0; i< titleCols.length; i++) {
			JLabel m = new JLabel(titleCols[i]);
			m.setBorder(BorderFactory.createLineBorder(Color.black,1));
			m.setBackground(Color.LIGHT_GRAY);
			m.setOpaque(true);
			tableauEntier[1][i+2] = m;
		}
		//checkbox l'interieur du tableau
		for(int i =2; i< titleRows.length+2; i++) {
			System.arraycopy(checkboxInterior[i - 2], 0, tableauEntier[i], 2, titleCols.length + 2 - 2);
		}
		
		
		// add finale 
		JPanel tab = new JPanel(new GridLayout(tableauEntier.length, tableauEntier[0].length));

		tab.setBorder(BorderFactory.createLineBorder(Color.black,1));

		for (Component[] components : tableauEntier) {
			for (Component component : components) {
				if (component == null) {
					tab.add(new JLabel("null"));
				} else {
					tab.add(component);
				}
			}
		}
		this.add(tab);
	}

	public JCheckBox getCheckbox(int x, int y) {
		return checkboxInterior[x][y];
	}

	public int getNbRows() {
		return this.checkboxInterior.length;
	}

	public int getNbCols() {
		return this.checkboxInterior[0].length;
	}

	private void setStateCheckboxHeadRows(int ligne, boolean state) {
		for(int i =0; i< checkboxInterior[ligne].length; i++) {
			checkboxInterior[ligne][i].setSelected(state);
		}
	}
	
	private void setStateCheckboxHeadCols(int ligne, boolean state) {
		for (JCheckBox[] jCheckBoxes : checkboxInterior) {
			jCheckBoxes[ligne].setSelected(state);
		}
	}

	public void setTable(boolean[][] table) {
		for(int i =0; i<checkboxInterior.length; i++) {
			for(int j =0; j<checkboxInterior[i].length; j++) {
				checkboxInterior[i][j].setSelected(table[i][j]);
			}
		}
	}

	public void setFirstColumn() {
		for (JCheckBox[] jCheckBoxes : checkboxInterior) {
			for (int j = 0; j < jCheckBoxes.length; j++) {
				jCheckBoxes[j].setSelected(j == 0);
			}
		}
	}
}
