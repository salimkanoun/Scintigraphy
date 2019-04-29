package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

public class TabTest3 extends TabResult implements ActionListener {

	private ImagePlus imp;

	private JLabel label;

	public TabTest3(FenResults parent, String title, boolean captureBtn) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		JPanel container = new JPanel(new BorderLayout());
		this.label = new JLabel();
		JButton button = new JButton("Get counts");
		button.addActionListener(this);
		container.add(this.label, BorderLayout.CENTER);
		container.add(button, BorderLayout.NORTH);
		return container;
	}

	@Override
	public JPanel getResultContent() {
		this.imp = parent.getModel().getImagePlus().duplicate();

		JPanel borderLayout = new JPanel(new BorderLayout());

		ImageCanvas canvas = TabTest3.getConnectedImageCanvas(imp);
		JPanel pan_center = new JPanel();
		pan_center.add(canvas);
		borderLayout.add(pan_center, BorderLayout.CENTER);
		return borderLayout;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.label.setText("Nombre de coups : " + Library_Quantif.getCounts(this.imp));
	}

	public static ImageCanvas getConnectedImageCanvas(ImagePlus imp) {

		ImageCanvas canvas = new ImageCanvas(imp);

		StackWindow fenApplication = new StackWindow(imp, canvas);
		fenApplication.setVisible(false);

		int canvasW, canvasH;

		int width = 512;

		int w = imp.getWidth();
		int h = imp.getHeight();
		Double ratioImagePlus = w * 1.0 / h * 1.0;

		if (ratioImagePlus < 1) {
			canvasW = (int) (width * ratioImagePlus);
			canvasH = (int) (width);

		} else {
			canvasW = width;
			canvasH = (int) (width / ratioImagePlus);
		}

		// this.getCanvas().setBounds(0,0,canvasW,canvasH);
		canvas.setSize(canvasW, canvasH);

		// on calcule le facteur de magnification
		List<Double> magnifications = new ArrayList<Double>();
		magnifications.add(canvasW / (1.0 * imp.getWidth()));
		magnifications.add(canvasH / (1.0 * imp.getHeight()));

		Double magnification = Collections.min(magnifications);

		canvas.setMagnification(magnification);

		return (canvas);
	}

}
