package org.petctviewer.scintigraphy.cardiac.tab;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.cardiac.Model_Cardiac;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabMainCardiac extends TabResult implements ActionListener, WindowListener {

	private final HashMap<String, String> resultats;

	private final BufferedImage capture;

	private popupVisualCalibration popup;

	public TabMainCardiac(FenResults parent, String title, HashMap<String, String> resultats, BufferedImage capture,
						  int fullBodyImages) {
		super(parent, title, true);

		this.resultats = resultats;

		this.capture = capture;

		this.reloadDisplay();
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

		JPanel resultRouge = new JPanel(new GridLayout(0, 1, 10, 10));
		
		String key = "Ratio H/WB %";
		JLabel lbl_hwb = new JLabel(key + " : " + resultats.get(key));
		lbl_hwb.setFont(new Font("Arial", Font.BOLD, 20));
		lbl_hwb.setHorizontalAlignment(JLabel.CENTER);

		// System.out.println(resultats.get(key) == null);
		// for (String s : resultats.keySet())
		// System.out.println(s + " : " + resultats.get(s));

		// System.out.println("\n\n" + key + " : " + resultats.get(key) + "\n\n");
		if (Double.parseDouble(resultats.get(key)) > 7.5) {
			lbl_hwb.setForeground(Color.RED);
		} else {
			lbl_hwb.setForeground(new Color(128, 51, 0));

		}

		// resultats.remove(key);
		resultRouge.add(lbl_hwb);
		
		
		key = "Heart to contralateral";
		JLabel labelHeartToControlateral = new JLabel(key + " : " + resultats.get(key));
		labelHeartToControlateral.setHorizontalAlignment(JLabel.CENTER);
		labelHeartToControlateral.setForeground(new Color(128, 51, 0));
		resultRouge.add(labelHeartToControlateral);
		
		
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

		// Button and ComboBox for visualGradation
		JPanel visualGradation = new JPanel(new GridLayout(0, 1));
		JPanel labelCombo = new JPanel();
		labelCombo.add(new JLabel("Visual Gradation : "));
		labelCombo.add(this.getCombo());
		visualGradation.add(labelCombo);
		JPanel panelButton = new JPanel();
		JButton btn_VisualGradation = new JButton("Visual Gradation info");
		btn_VisualGradation.addActionListener(this);
		panelButton.add(btn_VisualGradation);
		visualGradation.add(panelButton);

		returnBox.add(visualGradation);



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
		// returnBox.add(tabRes);

		return returnBox;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(capture);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			if (this.popup == null) {
				this.popup = new popupVisualCalibration();
				this.popup.setLocationRelativeTo(this.getSidePanelContent());
				this.popup.setVisible(true);
				this.popup.setResizable(false);
				this.popup.addWindowListener(this);
				this.popup.pack();
			} else this.popup.requestFocus();
		}
		
		if(e.getSource() instanceof JComboBox)
			((Model_Cardiac)this.getParent().getModel()).setFullBodyGradation((Integer)((JComboBox<?>)e.getSource()).getSelectedItem());
		// if(e.getSource() instanceof JComboBox)
		// System.out.println((this.btn_VisualGradation == null)+",
		// "+this.btn_VisualGradation.getLabel());

		// else if (e.getSource() == this.gradeList) {
		// System.out.println(this.btn_VisualGradation == null);
		// }
	}

	public JPanel getCombo() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new GridLayout(1, 1));

		Integer[] gradeString = {0, 1, 2, 3};

		JComboBox<Integer> gradeList = new JComboBox<>(gradeString);
		gradeList.setSelectedIndex(0);
		JPanel comboContainer = new JPanel();
		comboContainer.add(gradeList);
		comboContainer.setPreferredSize(new Dimension(45, 25));

		globalPane.add(comboContainer);
		
		gradeList.addActionListener(this);

		return globalPane;
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (arg0.getSource() == this.popup) {
			this.popup = null;
		}
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	private class popupVisualCalibration extends JFrame {

		private static final long serialVersionUID = 1L;

		public popupVisualCalibration() {

			JPanel listPane = new JPanel();
			listPane.setLayout(new GridLayout(2, 2));
			listPane.setBorder(BorderFactory.createTitledBorder("Visual Gradation"));

			String grade0 = "No myocardial uptake";
			String grade1 = "Myocardial uptake &lsaquo; bone uptake";
			String grade2 = "Myocardial uptake equal to bone uptake";
			String grade3 =
					"Myocardal uptake &rsaquo; bone uptake (with attenuatuon of bone uptake on whole body images";

			listPane.add(new JLabel("<html> &nbsp; Grade 0 : " + grade0 + "</html>"));
			listPane.add(new JLabel("<html>  Grade 1 : " + grade1 + "</html>"));
			listPane.add(new JLabel("<html> &nbsp; Grade 2 : " + grade2 + "</html>"));
			listPane.add(new JLabel("<html>  Grade 3 : " + grade3 + "</html>"));

			listPane.setPreferredSize(new Dimension(600, 200));

			this.add(listPane);

		}
	}
}