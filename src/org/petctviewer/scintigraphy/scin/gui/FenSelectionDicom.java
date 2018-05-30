package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.util.DicomTools;

public class FenSelectionDicom extends JDialog {

	private static final long serialVersionUID = 6706629497515318270L;

	JTable table;
	String[] titresDicoms;
	String[] selectedWindowsTitles;
	JButton btn_select, btn_selectAll;

	/**
	 * Permet de selectionner les dicom utilisees par le plugin
	 * 
	 * @param examType
	 *            : type d'examen
	 */
	public FenSelectionDicom(String examType) {
		this.titresDicoms = WindowManager.getImageTitles();

		// on ajoute le titre a la fenetre
		this.setTitle("Select Series");

		// creation du tableeau
		String[] columnNames = { "Patient", "Study", "Date", "Series", "Dimensions", "Stack Size" };
		String[][] tableData = getTableData();

		this.table = new JTable(tableData, columnNames);
		resizeColumnWidth(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.table.setFocusable(false);
		this.table.setDefaultEditor(Object.class, null);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(this.table);

		JPanel jp = new JPanel();

		controleurDialog ctrl = new controleurDialog();

		this.btn_select = new JButton("Select");
		this.btn_select.addActionListener(ctrl);

		this.btn_selectAll = new JButton("Select All");
		this.btn_selectAll.addActionListener(ctrl);

		panel.add(tablePane, BorderLayout.CENTER);

		panel.add(new JLabel("Select the dicoms for " + examType), BorderLayout.NORTH);

		jp.add(this.btn_select);
		jp.add(this.btn_selectAll);

		panel.add(jp, BorderLayout.SOUTH);

		this.add(panel);
		this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(null);
		this.pack();
	}

	// renvoie les informations a afficher dans le tableau
	private String[][] getTableData() {
		String[][] data = new String[this.titresDicoms.length][6];
		for (int i = 0; i < this.titresDicoms.length; i++) {
			ImagePlus imp = WindowManager.getImage(this.titresDicoms[i]);
			HashMap<String, String> hm = ModeleScin.getPatientInfo(imp);

			data[i][0] = replaceNull(hm.get("name"));

			if (DicomTools.getTag(imp, "0008,1030") != null) {
				data[i][1] = replaceNull(DicomTools.getTag(imp, "0008,1030").trim());
			} else {
				data[i][1] = "N/A";
			}

			data[i][2] = replaceNull(hm.get("date"));

			if (DicomTools.getTag(imp, "0008,103E") != null) {
				data[i][3] = replaceNull(DicomTools.getTag(imp, "0008,103E").trim());
			} else {
				data[i][3] = "N/A";
			}

			data[i][4] = imp.getDimensions()[0] + "x" + imp.getDimensions()[1];
			data[i][5] = "" + imp.getStack().getSize();
		}
		return data;
	}

	// controleur prive pour les boutons "select" et "select all"
	private class controleurDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();

			if (b == FenSelectionDicom.this.btn_select) {
				// recuperation des lignes selectionnees
				int[] rows = FenSelectionDicom.this.table.getSelectedRows();

				// construction du tableau de nm de dicoms selectionnees
				String[] titresFenSelected = new String[rows.length];
				for (int i = 0; i < rows.length; i++) {
					titresFenSelected[i] = FenSelectionDicom.this.titresDicoms[rows[i]];
				}

				FenSelectionDicom.this.selectedWindowsTitles = titresFenSelected;
			} else if (b == FenSelectionDicom.this.btn_selectAll) {
				// on selectionne toutes les fenetres
				FenSelectionDicom.this.selectedWindowsTitles = FenSelectionDicom.this.titresDicoms;
			}

			// on ferme la fenetre de selection
			if (FenSelectionDicom.this.selectedWindowsTitles.length >= 1) {
				FenSelectionDicom.this.dispose();
			}
		}
	}

	public String[] getSelectedWindowsTitles() {
		return this.selectedWindowsTitles;
	}

	// si le string est null, on renvoie un string vide
	private static String replaceNull(String s) {
		if (s == null || s == "") {
			return "N/A";
		}
		return s;
	}

	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 15; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

}
