package org.petctviewer.scintigraphy.cardiac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.petctviewer.scintigraphy.scin.FenetreResultatScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import java.awt.image.BufferedImage;

import java.io.FileNotFoundException;

import ij.ImagePlus;
import ij.gui.ImageWindow;

public class FenResultatCardiac extends JFrame {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;
	private Box side;
	private BufferedImage capture;

	private JButton btn_capture;

	public FenResultatCardiac(VueScin vueScin, HashMap<String, String> resultats, HashMap<String, String> infoPatient) {
		this.resultats = resultats;
		
		this.capture = ModeleScin
				.captureImage(vueScin.getImp(), vueScin.getImp().getWidth(), vueScin.getImp().getHeight())
				.getBufferedImage();

		this.setLayout(new BorderLayout());

		side = Box.createVerticalBox();
		side.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>DPDQuant</h1><html>");
		titreFen.setHorizontalAlignment(JLabel.CENTER);
		flow.add(titreFen);
		side.add(flow);

		// ajout des informations du patient
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("nom")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));
		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		side.add(flow1);

		JPanel resultRouge = new JPanel(new GridLayout(3, 1, 10, 10));
		String key = "Ratio H/WB (for a 1000)";
		resultRouge.add(getLabelRed(key));
		key = "Cardiac retention %";
		if (this.resultats.containsKey(key))
			resultRouge.add(getLabelRed(key));
		key = "Full body retention %";
		if (this.resultats.containsKey(key))
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
		side.add(tabRes);

		side.add(Box.createVerticalStrut(400));

		side.add(Box.createVerticalGlue());

		this.btn_capture = new JButton("Capture");
		btn_capture.setAlignmentX(Component.CENTER_ALIGNMENT);
		side.add(btn_capture);

		JLabel credits = new JLabel("Provided by petctviewer.org");
		credits.setVisible(false);
		side.add(credits);
		
		FenetreResultatScin.setCaptureButton(btn_capture, credits, vueScin, this);

		JLabel img = new JLabel();
		Image dimg = capture.getScaledInstance((int) (capture.getWidth() * 0.8), (int) (capture.getHeight() * 0.8),
				Image.SCALE_SMOOTH);
		img.setIcon(new ImageIcon(dimg));

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
		return new String[] { " " + key, v };
	}

	private JLabel getLabelRed(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.remove(key));
		lbl_hwb.setForeground(Color.RED);
		return lbl_hwb;
	}

}
