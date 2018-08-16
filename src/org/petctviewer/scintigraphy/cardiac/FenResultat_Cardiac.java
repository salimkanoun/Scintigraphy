package org.petctviewer.scintigraphy.cardiac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public class FenResultat_Cardiac extends JFrame {

	private static final long serialVersionUID = -5261203439330504164L;

	public FenResultat_Cardiac(Scintigraphy scin, BufferedImage capture) {
		this.setLayout(new BorderLayout());
		//super("DPD Quant", vueScin, capture, "");
		HashMap<String, String> resultats = ((Modele_Cardiac) scin.getFenApplication().getControleur().getModele()).getResultsHashMap();
		SidePanel side = new SidePanel(getSidePanelContent(resultats), "DPD Quant", scin.getImp());
		side.addCaptureBtn(scin, "");
		
		this.add(new DynamicImage(capture), BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.setTitle("DPD Quant results");
		this.pack();
		this.setMinimumSize(side.getSize());
		this.setLocationRelativeTo(scin.getFenApplication());
		this.setVisible(true);
	}

	public Component getSidePanelContent(HashMap<String, String> resultats) {
		Box returnBox = Box.createVerticalBox();
		
		JPanel resultRouge = new JPanel(new GridLayout(3, 1, 10, 10));
		
		String key = "Ratio H/WB %";
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.get(key));
		
		if(Double.parseDouble(resultats.get(key)) > 7.5) {
			lbl_hwb.setForeground(Color.RED);
		}else {
			lbl_hwb.setForeground(new Color(128, 51, 0));
		}
		resultats.remove(key);		
		resultRouge.add(lbl_hwb);
		
		//on ajoute le pourcentage de retention cardiaque si il existe
		key = "Cardiac retention %";
		if (resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.remove(key));
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}			
		
		//idem pour la retention du corps entier
		key = "WB retention %";
		if (resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.remove(key));
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
		for (String k : resultats.keySet()) {
			modelRes.addRow(this.getTabRes(k, resultats));
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
	
	private String[] getTabRes(String key, HashMap<String, String> resultats) {
		String v = "";
		if (resultats.containsKey(key)) {
			v = resultats.get(key);
		}
		return new String[] { " " + key, v };
	}

}
