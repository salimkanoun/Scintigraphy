package org.petctviewer.scintigraphy.hepatic;

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

public class FenResultat_Hepatic extends FenResultatSidePanel {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Hepatic(VueScin vueScin, HashMap<String, String> resultats, HashMap<String, String> infoPatient) {
		super("Bilary Scintigraphy", vueScin, infoPatient);
		this.resultats = resultats;
		this.finishBuildingWindow(1.5);
	}

	@Override
	public Component[] getSidePanelContent() {
		Component[] c = new Component[1];
		
		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		
		panel.add(new JLabel(""));
		for(String k : this.resultats.keySet()) {
			panel.add(getLabel(k));
		}
		
		c[0] = panel;
		return c;
	}
	
	private String[] getTabRes(String key) {
		String v = "";
		if (this.resultats.containsKey(key)) {
			v = this.resultats.get(key);
		}
		return new String[] { " " + key, v };
	}
	
	private JLabel getLabel(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.get(key));
		return lbl_hwb;
	}

}
