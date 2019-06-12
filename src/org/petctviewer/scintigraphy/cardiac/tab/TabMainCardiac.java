package org.petctviewer.scintigraphy.cardiac.tab;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class TabMainCardiac extends TabResult implements ActionListener {

	private final HashMap<String, String> resultats;

	private final BufferedImage capture;

	private JButton btn_VisualGradation;

	public TabMainCardiac(FenResults parent, String title, HashMap<String, String> resultats, BufferedImage capture,
						  int fullBodyImages) {
		super(parent, title, true);

		this.resultats = resultats;

		this.capture = capture;

		this.reloadDisplay();
		System.out.println("TabMainCardiac");
	}

	private String[] getTabRes(String key, HashMap<String, String> resultats) {
		String v = "";
		if (resultats.containsKey(key)) {
			v = resultats.get(key);
		}
		return new String[]{" " + key, v};
	}

	@Override
	public Component getSidePanelContent() {
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

		JPanel visualGradation = new JPanel(new GridLayout(1, 2));
		this.btn_VisualGradation = new JButton("Visual Gradation");
		this.btn_VisualGradation.addActionListener(this);

		visualGradation.add(this.btn_VisualGradation);
		visualGradation.add(this.getCombo());

		returnBox.add(visualGradation);

		return returnBox;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(capture);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == this.btn_VisualGradation) {
			popupVisualCalibration popup = new popupVisualCalibration();
			popup.setLocationRelativeTo(this.getSidePanelContent());
			popup.setVisible(true);

		}
	}

	public JPanel getCombo() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new GridLayout(1, 1));

		Integer[] gradeString = {0, 1, 2, 3};

		JComboBox<Integer> gradeList = new JComboBox<>(gradeString);
		gradeList.setSelectedIndex(0);
		JPanel comboContainer = new JPanel();
		comboContainer.add(gradeList);

		globalPane.add(comboContainer);

		return globalPane;
	}

	private class popupVisualCalibration extends JFrame {

		private static final long serialVersionUID = 1L;

		public popupVisualCalibration() {

			JPanel listPane = new JPanel();

			listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));

			String grade0 = "No myocardial uptake";
			String grade1 = "Myocardial uptake &lsaquo; bone uptake";
			String grade2 = "Myocardial uptake equal to bone uptake";
			String grade3 =
					"Myocardal uptake &rsaquo; bone uptake (with attenuatuon of bone uptake on whole body images";

			listPane.setLayout(new GridLayout(2, 2));
			listPane.add(new JLabel("<html> &nbsp; Grade 0 : " + grade0 + "</html>"));
			listPane.add(new JLabel("<html>  Grade 1 : " + grade1 + "</html>"));
			listPane.add(new JLabel("<html> &nbsp; Grade 2 : " + grade2 + "</html>"));
			listPane.add(new JLabel("<html>  Grade 3 : " + grade3 + "</html>"));

			listPane.setPreferredSize(new Dimension(600, 200));

			this.add(listPane);

			this.pack();
		}
	}
}
