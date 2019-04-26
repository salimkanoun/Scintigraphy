package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;

public class TabMainResult extends TabResult {

	private ImagePlus capture;

	public TabMainResult(FenResults parent, ImagePlus capture) {
		super(parent, "Result");
		this.capture = capture;
		this.reloadDisplay();
	}

	// TODO: change this method to have only desired data in input
	private JTable tablesResultats(String[] resultats) {
		JTable table = new JTable(0, 4);
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		String[] arr = new String[tableModel.getColumnCount()];
		for (int i = 0; i < ((Model_Gastric) this.parent.getModel()).nbAcquisitions() + 1; i++) { // +1 for the title
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				arr[j] = resultats[i * tableModel.getColumnCount() + j];
			}
			tableModel.insertRow(i, arr);
		}
		table.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		table.setBorder(border);
		return table;
	}

	// TODO: change this method to have only desired data in input
	private JPanel infoResultats(String[] resultats) {
		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));
		// la deuxime partir du resultats contient 13 ligne
		for (int i = ((Model_Gastric) this.parent.getModel()).nbAcquisitions() * 4 + 4; i < resultats.length; i++) {
			infoRes.add(new JLabel(resultats[i]));
		}
		return infoRes;
	}

	@Override
	public Component getSidePanelContent() {
		String[] results = ((Model_Gastric) this.parent.getModel()).resultats();
		JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.add(this.tablesResultats(results));
		panel.add(this.infoResultats(results));
		return panel;
	}

	@Override
	public JPanel getResultContent() {
		if (capture == null)
			return null;
		
		ImageStack ims = Library_Capture_CSV
				.captureToStack(new ImagePlus[] { capture, ((Model_Gastric) this.parent.getModel()).createGraph_3(),
						((Model_Gastric) this.parent.getModel()).createGraph_1(),
						((Model_Gastric) this.parent.getModel()).createGraph_2() });

		return new DynamicImage(((Model_Gastric) this.parent.getModel()).montage(ims).getImage());
	}

}
