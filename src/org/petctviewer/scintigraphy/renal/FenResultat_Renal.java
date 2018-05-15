package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenResultat_Renal extends FenResultatSidePanel {

	private static final long serialVersionUID = 5670592335800832792L;

	private List<XYSeries> series;
	private Modele_Renal modele;
	private BasicStroke stroke = new BasicStroke(5.0F);

	/**
	 * affiche les resultats de l'examen renal
	 * @param vueScin la vue
	 * @param capture capture du rein projetee
	 * @param chartPanel chartpanel avec l'overlay d'ajustation
	 */
	public FenResultat_Renal(VueScin vueScin, BufferedImage capture, ChartPanel chartPanel) {
		super("Renal scintigraphy", vueScin, capture, "");
		JPanel grid = new JPanel(new GridLayout(2, 1));

		// on affiche la capture
		JLabel lbl_capture = new JLabel();
		lbl_capture.setIcon(new ImageIcon(capture));

		// on recupere le modele et les series
		modele = (Modele_Renal) vueScin.getFen_application().getControleur().getModele();
		this.series = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood pool fitted R", "Final KR", "Output KR" },
				{ "Blood pool fitted L", "Final KL", "Output KL" } };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, series);

		// largeur et hauteur des graphiques
		int w = capture.getWidth();
		int h = capture.getHeight();

		// creation du panel du haut
		JPanel panel_top = new JPanel();

		// ajout de la capture
		panel_top.add(lbl_capture);

		// on grossit les traits
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartPanel.getChart().getXYPlot().getRenderer();
		renderer.setDefaultStroke(this.stroke);

		// redimension
		chartPanel.setPreferredSize(new Dimension(w * 2, h));

		// on renomme les series
		this.renameSeries(chartPanel, "Final KR", "Right Kidney");
		this.renameSeries(chartPanel, "Final KL", "Left Kidney");
		panel_top.add(chartPanel);

		// creation du panel du bas
		JPanel panel_bottom = new JPanel();
		
		//graphique rein droit
		ChartPanel c = cPanels[0];
		c.setPreferredSize(new Dimension(3 * w / 2, h));
		this.renameSeries(c, "Blood pool fitted R", "Blood Pool");
		this.renameSeries(c, "Final KR", "Right Kidney");
		this.renameSeries(c, "Output KR", "Output");
		c.getChart().getXYPlot().getRenderer().setDefaultStroke(stroke);
		c.getChart().setTitle("Right Kidney");

		//graphique rein gauche
		ChartPanel c1 = cPanels[1];
		c1.setPreferredSize(new Dimension(3 * w / 2, h));
		this.renameSeries(c1, "Output KL", "Output");
		this.renameSeries(c1, "Blood pool fitted L", "Blood Pool");
		this.renameSeries(c1, "Final KL", "Left Kidney");
		c1.getChart().getXYPlot().getRenderer().setDefaultStroke(stroke);
		c1.getChart().setTitle("Left Kidney");

		// ajout des chart panels dans le panel du ba
		panel_bottom.add(c);
		panel_bottom.add(c1);

		// on ajoute les panels a la grille principale
		grid.add(panel_top);
		grid.add(panel_bottom);

		// ajout de la grille a la fenetre
		this.add(grid, BorderLayout.WEST);

		this.finishBuildingWindow();
	}

	//renomme la serie
	private void renameSeries(ChartPanel chartPanel, String oldKey, String newKey) {
		XYSeriesCollection dataset = ((XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset());
		try {
			dataset.getSeries(oldKey).setKey(newKey);
		} catch (Exception e) {
		}
	}

	@Override
	public Component[] getSidePanelContent() {
		JPanel flow_wrap = new JPanel();

		// minutes a observer pour la capacite d'excretion
		int[] mins = new int[] { 20, 22, 30 };

		//on recupere les series
		XYSeries serieRK = modele.getSerie("Output KR");
		XYSeries serieLK = modele.getSerie("Output KL");

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();

		res.add(Box.createVerticalStrut(50));

		//affichage des roe pour le rein droit
		res.add(new JLabel("Renal Output Efficiency R. kidney :"));
		for (int min : mins) {
			res.add(new JLabel("   - ROE " + min + " min : " + modele.getPercentage(min, serieRK, "R") + "%"));
		}

		res.add(Box.createVerticalStrut(20));

		//affichage des roe pour le rein gauche
		res.add(new JLabel("Renal Output Efficiency L. kidney :"));
		for (int min : mins) {
			res.add(new JLabel("   - ROE " + min + " min : " + modele.getPercentage(min, serieLK, "L") + "%"));
		}

		res.add(Box.createVerticalStrut(50));

		//creation du tableau
		String[] columnNames = { "Parameter", "Left", "Right" };
		String[][] tableData = modele.getTableData();
		JScrollPane scrollPane = new JScrollPane();
		JTable table = new JTable(tableData, columnNames);
		
		//on desactive l'edition
		table.setDefaultEditor(Object.class, null);
		table.setFocusable(false);

		scrollPane.setViewportView(table);

		//on change la taille des colonnes
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(1).setPreferredWidth(30);
		columnModel.getColumn(2).setPreferredWidth(30);

		//on redimentionne le tableau afin d'avoir un border
		// TODO valeur en dur
		scrollPane.setPreferredSize(new Dimension(this.side.getWidth(), 87));

		flow_wrap.add(res);

		return new Component[] { flow_wrap, scrollPane, Box.createVerticalStrut(100) };
	}

}
