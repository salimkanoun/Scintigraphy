package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.gui.ContrastSlider;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.Overlay;

public class MainTab extends TabResult {

	private ContrastSlider slider;
	private DynamicImage result;

	public MainTab(FenResults parent, ImagePlus capture, Overlay overlay) {
		super(parent, "DMSA");

		this.result = new DynamicImage(capture.getBufferedImage());

		capture.setOverlay(overlay);
		this.slider = new ContrastSlider(ContrastSlider.HORIZONTAL, capture, this.result);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		Model_Dmsa modele = (Model_Dmsa) this.parent.getController().getModel();
		Double pctL = modele.getPct()[0] * 100;
		Double pctR = modele.getPct()[1] * 100;

		JLabel lbl_dmsaL = new JLabel("" + Library_Quantif.round(pctL, 1) + "%");
		JLabel lbl_dmsaR = new JLabel("" + Library_Quantif.round(pctR, 1) + "%");

		if (pctL > 55 | pctL < 45) {
			lbl_dmsaL.setForeground(Color.RED);
			lbl_dmsaR.setForeground(Color.RED);
		}

		JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));

		grid.add(new JPanel());
		JLabel lbl_l = new JLabel("L");
		lbl_l.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_l);

		JLabel lbl_r = new JLabel("R");
		lbl_r.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_r);

		grid.add(new JLabel("DMSA"));

		lbl_dmsaL.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_dmsaL);

		lbl_dmsaR.setHorizontalAlignment(SwingConstants.CENTER);
		grid.add(lbl_dmsaR);

		// Slider
		JPanel panSlider = new JPanel();
		panSlider.setLayout(new BoxLayout(panSlider, BoxLayout.Y_AXIS));
		panSlider.add(new JLabel("Contrast"));
		panSlider.add(this.slider);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(panSlider, BorderLayout.NORTH);
		panel.add(grid, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public Container getResultContent() {
		return this.result;
	}

}
