package org.petctviewer.scintigraphy.cardiac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import java.awt.image.BufferedImage;

public class FenResultat_Cardiac extends FenResultatSidePanel {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Cardiac(VueScin vueScin, BufferedImage capture) {
		super("DPD Quant", vueScin, capture, "");
		this.resultats = vueScin.getFen_application().getControleur().getModele().getResultsHashMap();
		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		Component[] panels = new Component[3];
		
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
		
		key = "Cardiac retention %";
		if (this.resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.remove(key));
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}			
		
		key = "WB retention %";
		if (this.resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.remove(key));
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}
		
		JPanel flow2 = new JPanel(new FlowLayout());
		flow2.add(resultRouge);
		panels[0] = flow2;

		/// ajout de la table avec les resultats des rois
		DefaultTableModel modelRes = new DefaultTableModel();
		JTable tabRes = new JTable(modelRes);
		modelRes.addColumn("Organ");
		modelRes.addColumn("Count avg");
		for (String k : this.resultats.keySet()) {
			modelRes.addRow(this.getTabRes(k));
		}
		tabRes.setAutoCreateRowSorter(true);
		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) tabRes.getRowSorter());
		ArrayList<SortKey> list = new ArrayList<SortKey>();
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();
		tabRes.setBorder(LineBorder.createBlackLineBorder());
		// desactive l'edition
		tabRes.setDefaultEditor(Object.class, null);
		tabRes.setFocusable(false);
		tabRes.setRowSelectionAllowed(false);
		panels[2] = tabRes;
		
		JPanel flowRef = new JPanel();
		flowRef.add( new JLabel("Rapezzi et al. JACC 2011"));
		panels[1] = flowRef;
		
		return panels;
	}
	
	private String[] getTabRes(String key) {
		String v = "";
		if (this.resultats.containsKey(key)) {
			v = this.resultats.get(key);
		}
		return new String[] { " " + key, v };
	}

}