package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.LineBorder;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import java.awt.image.BufferedImage;

public class FenResultat_Cardiac extends FenResultatSidePanel {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Cardiac(VueScin vueScin, BufferedImage capture) {
		super("DPD Quant", vueScin, capture, "");
		this.resultats = ((Modele_Cardiac) vueScin.getFenApplication().getControleur().getModele()).getResultsHashMap();
		this.finishBuildingWindow(true);
		this.setVisible(true);
	}

	@Override
	public Component getSidePanelContent() {
		Box returnBox = Box.createVerticalBox();
		
		JPanel resultRouge = new JPanel(new GridLayout(3, 1, 10, 10));
		
		String key = "Ratio H/WB %";
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.get(key));
		
		if(Double.parseDouble(this.resultats.get(key)) > 7.5) {
			lbl_hwb.setForeground(Color.RED);
		}else {
			lbl_hwb.setForeground(new Color(128, 51, 0));
		}
		this.resultats.remove(key);		
		resultRouge.add(lbl_hwb);
		
		//on ajoute le pourcentage de retention cardiaque si il existe
		key = "Cardiac retention %";
		if (this.resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + this.resultats.remove(key));
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}			
		
		//idem pour la retention du corps entier
		key = "WB retention %";
		if (this.resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + this.resultats.remove(key));
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}
		
		//on utilise un flow layout pour centrer le panel
		JPanel flow2 = new JPanel(new FlowLayout());
		flow2.add(resultRouge);
		returnBox.add(flow2);

		// ajout de la table avec les resultats des rois
		DefaultTableModel modelRes = new DefaultTableModel();
		JTable tabRes = new JTable(modelRes);
		modelRes.addColumn("Organ");
		modelRes.addColumn("Count avg");
		for (String k : this.resultats.keySet()) {
			modelRes.addRow(this.getTabRes(k));
		}
		
		//tri des valeurs
		tabRes.setAutoCreateRowSorter(true);
		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) tabRes.getRowSorter());
		ArrayList<SortKey> list = new ArrayList<>();
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();
		
		//ajout d'une bordure
		tabRes.setBorder(LineBorder.createBlackLineBorder());
		
		// desactive l'edition
		tabRes.setDefaultEditor(Object.class, null);
		
		//on empeche l'edition
		tabRes.setFocusable(false);
		tabRes.setRowSelectionAllowed(false);
		returnBox.add(tabRes);
		
		JPanel flowRef = new JPanel();
		flowRef.add( new JLabel("Rapezzi et al. JACC 2011"));
		returnBox.add(flowRef);
		
		return returnBox;
	}
	
	private String[] getTabRes(String key) {
		String v = "";
		if (this.resultats.containsKey(key)) {
			v = this.resultats.get(key);
		}
		return new String[] { " " + key, v };
	}

}
