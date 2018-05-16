package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

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
	 * 
	 * @param vueScin
	 *            la vue
	 * @param capture
	 *            capture du rein projetee
	 * @param chartPanel
	 *            chartpanel avec l'overlay d'ajustation
	 */
	public FenResultat_Renal(VueScin vueScin, BufferedImage capture, ChartPanel chartPanel) {
		super("Renal scintigraphy", vueScin, capture, "");
		JPanel grid = new JPanel(new GridLayout(2, 1));

		// on affiche la capture
		JLabel lbl_capture = new JLabel();
		lbl_capture.setIcon(new ImageIcon(capture));

		// on recupere le modele et les series
		this.modele = (Modele_Renal) vueScin.getFen_application().getControleur().getModele();
		this.series = this.modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood pool fitted L", "Final KL", "Output KL" },
				{ "Blood pool fitted R", "Final KR", "Output KR" } };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, this.series);

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

		// redimension et ajout
		chartPanel.setPreferredSize(new Dimension(w * 2, h));
		panel_top.add(chartPanel);

		// creation du panel du bas
		JPanel panel_bottom = new JPanel();

		// graphique rein droit
		ChartPanel c = cPanels[1];
		c.setPreferredSize(new Dimension(3 * w / 2, h));
		FenResultat_Renal.renameSeries(c, "Blood pool fitted R", "Blood Pool");
		FenResultat_Renal.renameSeries(c, "Final KR", "Right Kidney");
		FenResultat_Renal.renameSeries(c, "Output KR", "Output");
		c.getChart().getXYPlot().getRenderer().setDefaultStroke(this.stroke);
		c.getChart().setTitle("Right Kidney");

		// graphique rein gauche
		ChartPanel c1 = cPanels[0];
		c1.setPreferredSize(new Dimension(3 * w / 2, h));
		FenResultat_Renal.renameSeries(c1, "Output KL", "Output");
		FenResultat_Renal.renameSeries(c1, "Blood pool fitted L", "Blood Pool");
		FenResultat_Renal.renameSeries(c1, "Final KL", "Left Kidney");
		c1.getChart().getXYPlot().getRenderer().setDefaultStroke(this.stroke);
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

	// renomme la serie
	private static void renameSeries(ChartPanel chartPanel, String oldKey, String newKey) {
		XYSeriesCollection dataset = ((XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset());
		try {
			dataset.getSeries(oldKey).setKey(newKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Component[] getSidePanelContent() {
		JPanel flow_wrap = new JPanel();

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();

		res.add(this.getPanelSep());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelRet());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelTiming());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelNoRa());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelROE());

		res.add(Box.createVerticalStrut(50));

		flow_wrap.add(res);

		return new Component[] { flow_wrap };
	}

	private Component getPanelROE() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// minutes a observer pour la capacite d'excretion
		int[] mins = new int[] { 20, 22, 30 };

		// on recupere les series
		XYSeries serieRK = this.modele.getSerie("Output KR");
		XYSeries serieLK = this.modele.getSerie("Output KL");

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			JLabel lbl_g = new JLabel(modele.getPercentage(mins[i], serieLK, "L") + " %");
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_g);
			
			JLabel lbl_d = new JLabel(modele.getPercentage(mins[i], serieRK, "R") + " %");
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_d);

		}

		return pnl_roe;
	}

	private Component getPanelNoRa() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// panel nora
		Double[][] nora = modele.getNoRA();
		JPanel pnl_nora = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_nora.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_nora.add(new JLabel(" NORA "));
		pnl_nora.add(lbl_L);
		pnl_nora.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			pnl_nora.add(new JLabel(nora[0][i] + "  min"));
			
			JLabel lbl_g = new JLabel(nora[1][i] + " %");
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_nora.add(lbl_g);
			
			JLabel lbl_d = new JLabel(nora[2][i] + " %");
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_nora.add(lbl_d);
		}
		return pnl_nora;
	}

	private Component getPanelTiming() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// panel de timing
		Double[][] timing = modele.getTiming();
		JPanel pnl_timing = new JPanel(new GridLayout(3, 3, 0, 3));
		pnl_timing.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_timing.add(new JLabel(" Timing "));
		pnl_timing.add(lbl_L);
		pnl_timing.add(lbl_R);
		JLabel lbl_tmax = new JLabel("TMax (min)");
		pnl_timing.add(lbl_tmax);
		
		JLabel lbl_gMax = new JLabel("" + timing[0][0]);
		lbl_gMax.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_gMax);
		
		JLabel lbl_dMax = new JLabel("" + timing[0][1]);
		lbl_dMax.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_dMax);
		
		JLabel lbl_tdemi = new JLabel("T1/2 (min)");
		pnl_timing.add(lbl_tdemi);
		
		String s = "" + timing[1][0];
		if (timing[1][0] == -1)
			s = "N/A";
		
		JLabel lbl_g = new JLabel(s);
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_g);
		
		s = "" + timing[1][1];
		if (timing[1][1] == -1)
			s = "N/A";
		
		JLabel lbl_d = new JLabel(s);
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_d);
		
		pnl_timing.add(lbl_d);

		return pnl_timing;
	}

	private Component getPanelRet() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// panel de retention
		Double[] ret = modele.getRetention();
		JPanel pnl_ret = new JPanel(new GridLayout(2, 3));
		pnl_ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_ret.add(new JLabel(" Renal ret. "));
		pnl_ret.add(lbl_L);
		pnl_ret.add(lbl_R);
		
		pnl_ret.add(new JLabel(""));

		JLabel lbl_g = new JLabel(ret[0] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_ret.add(lbl_g);

		JLabel lbl_d = new JLabel(ret[1] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_ret.add(lbl_d);

		return pnl_ret;
	}

	private Component getPanelSep() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		// panel de fonction separee
		Double[] sep = modele.getSeparatedFunction();
		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_sep.add(new JLabel(" Separated fun. "));
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);
		
		pnl_sep.add(new JLabel(""));
		JLabel lbl_d = new JLabel(sep[0] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_d);

		JLabel lbl_g = new JLabel(sep[1] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_g);

		return pnl_sep;
	}

}
