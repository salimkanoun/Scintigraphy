package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.Vue_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class TabPrincipal extends FenResultatSidePanel {

	private static final long serialVersionUID = 5670592335800832792L;

	private Modele_Renal modele;

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
	public TabPrincipal(Vue_Renal vue, BufferedImage capture, int w, int h) {
		super("Renal scintigraphy", vue, capture, "");
		
		JValueSetter chartNephrogram = vue.getNephrogramChart();

		HashMap<Comparable, Double> adjusted = ((Modele_Renal) vue.getFenApplication().getControleur().getModele()).getAdjustedValues();
		// l'intervalle est defini par l'utilisateur
		Double x1 = adjusted.get("start");
		Double x2 = adjusted.get("end");
		Double debut = Math.min(x1, x2);
		Double fin = Math.max(x1, x2);

		int slice1 = ModeleScinDyn.getSliceIndexByTime(debut * 60 * 1000, vue.getFrameDurations());
		int slice2 = ModeleScinDyn.getSliceIndexByTime(fin * 60 * 1000, vue.getFrameDurations());

		ImagePlus proj = ZProjector.run(vue.getImp(), "sum", slice1, slice2);
		proj.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);

		BufferedImage imgProj = proj.getBufferedImage();

		imgProj = resizeImage(imgProj, capture.getWidth(), capture.getHeight());

		this.modele = (Modele_Renal) vue.getFenApplication().getControleur().getModele();

		JPanel grid = new JPanel(new GridLayout(2, 1));

		// on affiche les capture
		DynamicImage lbl_capture = new DynamicImage(capture);
		DynamicImage lbl_proj = new DynamicImage(proj.getImage());

		// creation du panel du haut
		JPanel panel_top = new JPanel(new GridLayout(1, 2));

		// ajout de la capture et du montage
		panel_top.add(lbl_capture);
		panel_top.add(lbl_proj);

		// creation du panel du bas
		chartNephrogram.setPreferredSize(new Dimension(w, h / 2));

		// on ajoute les panels a la grille principale
		grid.add(panel_top);
		grid.add(chartNephrogram);

		// ajout de la grille a la fenetre
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(grid, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));

		this.finishBuildingWindow(true);
		this.setVisible(false);
	}

	@Override
	public Component getSidePanelContent() {
		boolean[] kidneys = this.modele.getKidneys();

		JPanel flow_wrap = new JPanel();

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();

		if (kidneys[0] && kidneys[1]) {
			res.add(this.getPanelSep());
		}

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

		return flow_wrap;
	}

	private Component getPanelROE() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// minutes a observer pour la capacite d'excretion
		int[] mins = new int[] { 20, 22, 30 };

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);

		boolean[] kidneys = this.modele.getKidneys();
		XYSeries serieRK = null, serieLK = null;

		for (int i = 0; i < 3; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			JLabel lbl_g = null;
			if (kidneys[0]) {
				serieLK = this.modele.getSerie("Output KL");
				lbl_g = new JLabel(modele.getPercentage(mins[i], serieLK, "L") + " %");
			} else {
				lbl_g = new JLabel("N/A");
			}
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_g);

			JLabel lbl_d = null;
			if (kidneys[1]) {
				serieRK = this.modele.getSerie("Output KR");
				lbl_d = new JLabel(modele.getPercentage(mins[i], serieRK, "R") + " %");
			} else {
				lbl_d = new JLabel("N/A");
			}
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

			if (nora[1][i] != Double.NaN) {
				JLabel lbl_g = new JLabel(nora[1][i] + " %");
				lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_nora.add(lbl_g);
			} else {
				pnl_nora.add(new JLabel("" + Double.NaN));
			}

			if (nora[2][i] != Double.NaN) {
				JLabel lbl_d = new JLabel(nora[2][i] + " %");
				lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_nora.add(lbl_d);
			} else {
				pnl_nora.add(new JLabel("" + Double.NaN));
			}
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

	private Component getPanelSep() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		// panel de fonction separee
		Double[] sep = modele.getSeparatedFunction();
		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_sep.add(new JLabel("Relative function"));
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);

		pnl_sep.add(new JLabel("integral"));
		JLabel lbl_d = new JLabel(sep[0] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_d);

		JLabel lbl_g = new JLabel(sep[1] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_g);
		
		if(modele.getPatlakPente() != null) {
			double[] patlak = modele.getPatlakPente();
			pnl_sep.setLayout(new GridLayout(3, 3));
			
			pnl_sep.add(new JLabel("patlak"));
			JLabel lbl_pd = new JLabel(patlak[0] + " %");
			lbl_pd.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_sep.add(lbl_pd);

			JLabel lbl_pg = new JLabel(patlak[1] + " %");
			lbl_pg.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_sep.add(lbl_pg);
		}
		
		return pnl_sep;
	}

}
