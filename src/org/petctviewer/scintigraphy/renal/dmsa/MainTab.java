package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.ContrastSlider;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.gui.Overlay;

public class MainTab extends TabResult {

	private ContrastSlider slider;
	private DynamicImage result;
	private JLabel contrastSliderLabel;

	public MainTab(FenResults parent, ImageSelection capture, Overlay overlay) {
		super(parent, "DMSA", true);

		this.result = new DynamicImage(capture.getImagePlus().getBufferedImage());

		capture.getImagePlus().setOverlay(overlay);
		this.slider = new ContrastSlider(new ImageState(Orientation.POST, 2,
														((ControllerWorkflow) this.parent.getController()).getCurrentImageState().getLateralisation(),
														capture), this.result, this.parent);
		this.contrastSliderLabel = new JLabel("Contrast");
		
		this.setComponentToHide(new ArrayList<>(Arrays.asList(new Component[] {slider, contrastSliderLabel})));

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
		panSlider.add(this.contrastSliderLabel);
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
