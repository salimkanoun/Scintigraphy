package org.petctviewer.scintigraphy.cardiac;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class FenResultat_Cardiac extends FenResults {

	private static final long serialVersionUID = -5261203439330504164L;

	public FenResultat_Cardiac(BufferedImage capture, ControllerScin controller) {
		super(controller);

		HashMap<String, String> resultats = ((Model_Cardiac) controller.getModel()).getResultsHashMap();

		this.setMainTab(new TabMainCardiac(this, "DPD Quant", true, resultats, capture));

		this.setTitle("DPD Quant results");
	}

	public Component getSidePanelContent(HashMap<String, String> resultats) {
		Box returnBox = Box.createVerticalBox();

		JPanel resultRouge = new JPanel(new GridLayout(3, 1, 10, 10));

		String key = "Ratio H/WB %";
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.get(key));
		lbl_hwb.setFont(new Font("Arial", Font.BOLD, 20));
		lbl_hwb.setHorizontalAlignment(JLabel.CENTER);

		System.out.println(resultats.get(key) == null);
		for (String s : resultats.keySet())
			System.out.println(s + " : " + resultats.get(s));

		System.out.println("\n\n" + key + " : " + resultats.get(key) + "\n\n");
		if (Double.parseDouble(resultats.get(key)) > 7.5) {
			lbl_hwb.setForeground(Color.RED);
		} else {
			lbl_hwb.setForeground(new Color(128, 51, 0));

		}

		// resultats.remove(key);
		resultRouge.add(lbl_hwb);

		// on ajoute le pourcentage de retention cardiaque si il existe
		key = "Cardiac retention %";
		if (resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.get(key));
			lbl.setHorizontalAlignment(JLabel.CENTER);
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}

		// idem pour la retention du corps entier
		key = "WB retention %";
		if (resultats.containsKey(key)) {
			JLabel lbl = new JLabel(key + " : " + resultats.get(key));
			lbl.setHorizontalAlignment(JLabel.CENTER);
			lbl.setForeground(new Color(128, 51, 0));
			resultRouge.add(lbl);
		}

		// on utilise un flow layout pour centrer le panel
		JPanel flow2 = new JPanel(new FlowLayout());
		flow2.add(resultRouge);
		returnBox.add(flow2);

		JPanel flowRef = new JPanel();
		JPanel gridRef = new JPanel(new GridLayout(0, 1));
		JLabel value = new JLabel("H/WB > 7.5% is associated with higher risk of cardiac event");
		value.setHorizontalAlignment(JLabel.CENTER);
		JLabel ref = new JLabel("Rapezzi et al. JACC 2011");
		ref.setHorizontalAlignment(JLabel.CENTER);
		gridRef.add(value);
		gridRef.add(ref);
		flowRef.add(gridRef);

		returnBox.add(flowRef);

		// ajout de la table avec les resultats des rois
		DefaultTableModel modelRes = new DefaultTableModel();
		JTable tabRes = new JTable(modelRes);
		modelRes.addColumn("Organ");
		modelRes.addColumn("Count avg");
		for (String k : resultats.keySet()) {
			modelRes.addRow(this.getTabRes(k, resultats));
		}

		// tri des valeurs
		tabRes.setAutoCreateRowSorter(true);
		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) tabRes.getRowSorter());
		ArrayList<SortKey> list = new ArrayList<>();
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();

		// ajout d'une bordure
		tabRes.setBorder(LineBorder.createBlackLineBorder());

		// desactive l'edition
		tabRes.setDefaultEditor(Object.class, null);

		// on empeche l'edition
		tabRes.setFocusable(false);
		tabRes.setRowSelectionAllowed(false);

		// Add popupMenu to hide the Table if Wanted
		JPopupMenu popMenuHide = new JPopupMenu();
		JMenuItem hide = new JMenuItem("Hide");
		popMenuHide.add(hide);
		hide.addActionListener(arg0 -> tabRes.setVisible(false));
		tabRes.setComponentPopupMenu(popMenuHide);
		// Add to the main panel
		returnBox.add(tabRes);

		return returnBox;
	}

	private String[] getTabRes(String key, HashMap<String, String> resultats) {
		String v = "";
		if (resultats.containsKey(key)) {
			v = resultats.get(key);
		}
		return new String[] { " " + key, v };
	}

	private class TabMainCardiac extends TabResult {

		private final HashMap<String, String> resultats;

		private final BufferedImage capture;

		public TabMainCardiac(FenResults parent, String title, boolean captureBtn, HashMap<String, String> resultats,
				BufferedImage capture) {
			super(parent, title, captureBtn);

			this.resultats = resultats;

			this.capture = capture;

			this.reloadDisplay();
			System.out.println("TabMainCardiac");
		}

		@Override
		public Component getSidePanelContent() {
			return FenResultat_Cardiac.this.getSidePanelContent(resultats);
		}

		@Override
		public JPanel getResultContent() {
			return new DynamicImage(capture);
		}

	}

}
