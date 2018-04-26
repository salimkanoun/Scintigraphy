package org.petctviewer.scintigraphy.hepatic.statique;

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

	public FenResultat_Hepatic(VueScin vueScin, BufferedImage capture) {
		super(vueScin.getExamType(), vueScin, capture);
		this.resultats = vueScin.getFen_application().getControleur().getModele().getResultsHashMap();
		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		Component[] c = new Component[1];
		
		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		
		panel.add(new JLabel(""));
		for(String k : this.resultats.keySet()) {
			panel.add(getLabel(k));
		}
		
		JPanel flow = new JPanel();
		flow.add(panel);
		
		c[0] = flow;
		return c;
	}
	
	private JLabel getLabel(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.get(key));
		return lbl_hwb;
	}

}
