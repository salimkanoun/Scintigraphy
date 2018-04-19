package org.petctviewer.scintigraphy.cardiac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.util.DicomTools;

public class FenResultatCardiac extends JFrame {

	private static final long serialVersionUID = -5261203439330504164L;

	HashMap<String, String> resultats;
	JPanel side;
	BufferedImage capture;

	public FenResultatCardiac(BufferedImage capture, HashMap<String, String> resultats, HashMap<String, String> infoPatient) {
		this.resultats = resultats;
		this.capture = capture;

		this.setLayout(new BorderLayout());

		side = new JPanel(new GridLayout(10, 1));

		//ajout du titre de la fenetre
		JLabel titreFen = new JLabel("<html><h1>DPDQuant</h1><html>");
		side.add(titreFen);
		
		side.add(new JPanel());
		side.add(new JPanel());
		
		// ajout des informations du patient
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("nom")));		
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));		
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));		
		JPanel flow = new JPanel(new FlowLayout());
		flow.add(patientInfo);
		side.add(flow);
		
		JPanel resultRouge = new JPanel(new GridLayout(3, 1, 10, 10));		
		String key = "Ratio H/WB (for a 1000)";
		resultRouge.add(getLabelRed(key));		
		key = "Cardiac retention %";
		if(this.resultats.containsKey(key))
			resultRouge.add(getLabelRed(key));		
		key = "Full body retention %";
		if(this.resultats.containsKey(key))
			resultRouge.add(getLabelRed(key));
		JPanel flow2 = new JPanel(new FlowLayout());
		flow2.add(resultRouge);
		side.add(flow2);

		/// ajout de la table avec les resultats des rois
		DefaultTableModel modelRes = new DefaultTableModel();
		JTable tabRes = new JTable(modelRes);
		modelRes.addColumn("Organ");
		modelRes.addColumn("Count avg");
		
		for (String k : this.resultats.keySet()) {
			modelRes.addRow(this.getTabRes(k));
		}
		
		tabRes.setAutoCreateRowSorter(true);
		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>)tabRes.getRowSorter());
		ArrayList<SortKey> list = new ArrayList<SortKey>();
        list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();
		
		side.add(tabRes);

		JLabel img = new JLabel();
		img.setIcon(new ImageIcon(capture));

		this.add(img, BorderLayout.WEST);
		this.add(side, BorderLayout.EAST);

		this.pack();
		this.setVisible(true);
		this.setSize(this.getPreferredSize());
		this.setLocationRelativeTo(null);
	}

	private String[] getTabRes(String key) {
		String v = "";
		if (this.resultats.containsKey(key)) {
			v = this.resultats.get(key);
		}
		return new String[] { key, v };
	}
	
	private JLabel getLabelRed(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.remove(key));
		lbl_hwb.setForeground(Color.RED);
		return lbl_hwb;
	}

}
