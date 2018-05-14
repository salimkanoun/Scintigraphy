package org.petctviewer.scintigraphy.renal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenResultat_Renal extends FenResultatSidePanel {

	private static final long serialVersionUID = 5670592335800832792L;

	private List<XYSeries> series;
	Modele_Renal modele;

	public FenResultat_Renal(VueScin vueScin, BufferedImage capture, ChartPanel chartPanel) {
		super("Renal scintigraphy", vueScin, capture, "");
		JPanel grid = new JPanel(new GridLayout(2, 3));

		// on affiche la capture
		JLabel lbl_capture = new JLabel();
		lbl_capture.setIcon(new ImageIcon(capture));
		grid.add(lbl_capture);

		// on recupere le modele et les series
		modele = (Modele_Renal) vueScin.getFen_application().getControleur().getModele();
		this.series = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood Pool" }, { "Bladder" }, { "Final KR", "Final KL" },
				{ "Blood pool fitted R", "Final KR", "Output KR" }, { "Blood pool fitted L", "Final KL", "Output KL" } };
		
		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, series);
		
		//new Test(cPanels[1]);
		
		cPanels[2] = chartPanel;

		// ajout des chart panels dans la grille
		for (int i = 0; i < cPanels.length; i++) {
			ChartPanel c = cPanels[i];
			c.setPreferredSize(new Dimension(capture.getWidth() + 1 / 3 * capture.getWidth(), capture.getHeight()));
			c.getChart().getPlot().setBackgroundPaint(null);
			grid.add(c);
		}
		

		// ajout de la grille a la fenetre
		this.add(grid, BorderLayout.WEST);

		this.finishBuildingWindow();
	}

	private XYSeries getSeries(String key, List<XYSeries> series) {
		for (int i = 0; i < series.size(); i++) {
			if (series.get(i).getKey().equals(key)) {
				return series.get(i);
			}
		}
		return null;
	}

	@Override
	public Component[] getSidePanelContent() {
		JPanel flow_wrap = new JPanel();

		// minutes a observer pour la capacite d'excretion
		int[] mins = new int[] { 20, 22, 30 };

		// panel des calculs du rein droit

		XYSeries serieRK = modele.getSerie("Output KR");
		XYSeries serieLK = modele.getSerie("Output KL");

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();
		
		res.add(Box.createVerticalStrut(50));
		
		res.add(new JLabel("Renal output efficiency Right Kidney :"));
		for (int min : mins) {
			res.add(new JLabel("   - ROE " + min + " min : " + modele.getPercentage(min, serieRK, "R") + "%"));
		}

		res.add(Box.createVerticalStrut(20));

		res.add(new JLabel("Renal output efficiency Left Kidney :"));
		for (int min : mins) {
			res.add(new JLabel("   - ROE " + min + " min : " + modele.getPercentage(min, serieLK, "L") + "%"));
		}
		
		res.add(Box.createVerticalStrut(50));

		String[] columnNames = { "Parameter", "Left", "Right" };
		String[][] tableData = modele.getTableData();

		JScrollPane scrollPane = new JScrollPane();
		
		JTable table = new JTable(tableData, columnNames);
		table.setDefaultEditor(Object.class, null);
		table.setFocusable(false);

		scrollPane.setViewportView(table);

		TableColumnModel columnModel = table.getColumnModel();

		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(1).setPreferredWidth(30);
		columnModel.getColumn(2).setPreferredWidth(30);
		
		//TODO valeur en dur
		scrollPane.setPreferredSize(new Dimension(this.side.getWidth(), 87));
	
		flow_wrap.add(res);
		
		return new Component[] { flow_wrap, scrollPane, Box.createVerticalStrut(100)};
	}

}
