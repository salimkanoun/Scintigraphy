package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.EventListener;
import ij.util.DicomTools;

public class FenSelectionDicom extends JFrame implements ActionListener, ImageListener {

	private static final long serialVersionUID = 6706629497515318270L;

	private String[] titresDicoms;
	private String[] selectedWindowsTitles;
	private JButton btn_select, btn_selectAll;
	private Scintigraphy vue;
	private DefaultTableModel dataModel;
	private JTable table;

	/**
	 * Permet de selectionner les dicom utilisees par le plugin
	 * 
	 * @param examType
	 *            : type d'examen
	 */
	public FenSelectionDicom(String examType, Scintigraphy vue) {
		this.vue = vue;
		this.titresDicoms = WindowManager.getImageTitles().clone();
		ImagePlus.addImageListener(this);

		// on ajoute le titre a la fenetre
		this.setTitle("Select Series");

		// creation du tableau
		String[] columnNames = { "Patient", "Study", "Date", "Series", "Dimensions", "Stack Size" };
		this.dataModel = new DefaultTableModel(this.getTableData(), columnNames);
		table = new JTable();
		table.setModel(this.dataModel);
		resizeColumnWidth(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setFocusable(false);
		table.setDefaultEditor(Object.class, null);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(table);

		JPanel jp = new JPanel();

		this.btn_select = new JButton("Select");
		this.btn_select.addActionListener(this);

		this.btn_selectAll = new JButton("Select All");
		this.btn_selectAll.addActionListener(this);

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

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == this.btn_select) {
			// recuperation des lignes selectionnees
			int[] rows = this.table.getSelectedRows();

			// construction du tableau de nm de dicoms selectionnees
			String[] titresFenSelected = new String[rows.length];
			for (int i = 0; i < rows.length; i++) {
				titresFenSelected[i] = this.titresDicoms[rows[i]];
			}

			this.selectedWindowsTitles = titresFenSelected;
		} else if (b == this.btn_selectAll) {
			// on selectionne toutes les fenetres
			this.selectedWindowsTitles = this.titresDicoms;
		}

		// on ferme la fenetre de selection
		if (this.selectedWindowsTitles.length >= 1) {
			this.startExam();
		}
	}

	public void startExam() {
		ImagePlus[] images = new ImagePlus[this.selectedWindowsTitles.length];
		for (int i = 0; i < selectedWindowsTitles.length; i++) {
			images[i] = WindowManager.getImage(selectedWindowsTitles[i]);
		}

		try {
			this.vue.startExam(images);

			// on ferme toutes les fenetres choisies
			for (String s : selectedWindowsTitles) {
				ImagePlus impOuverte = WindowManager.getImage(s);
				if (impOuverte != null) {
					impOuverte.close();
				}
			}

			ImagePlus.removeImageListener(this);
			this.dispose();
		} catch (Exception e) {
			System.err.println("The selected DICOM are not fit for this exam");
		}
	}
	
	private void updateTable() {
		String[] nouveauxTitres = WindowManager.getImageTitles();

		// si la liste n'est pas la même
		if (!Arrays.deepEquals(nouveauxTitres, this.titresDicoms)) {
			this.titresDicoms = nouveauxTitres;
			this.dataModel.setRowCount(0);
			for (String[] s : this.getTableData()) {
				this.dataModel.addRow(s);
			}
		}
	}

	@Override
	public void imageOpened(ImagePlus imp) {
		this.updateTable();
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		this.updateTable();
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}

}
