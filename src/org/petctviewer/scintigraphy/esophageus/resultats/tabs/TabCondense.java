package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.esophageus.resultats.Modele_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import ij.process.ImageStatistics;

public class TabCondense extends TabResult implements ChangeListener {

	// Spinners
	private JSpinner spinnerRight;
	private JSpinner spinnerLeft;
	private DynamicImage imageCondensePanel;

	private int rightRognageValue[];
	private int leftRognageValue[];
	private int contrastValue[];

	private JSlider contrastSlider;

	private DynamicImage imageProjeterEtRoiPanel;

	private JRadioButton[] radioButtonCondense;

	private static int numAcquisitionCondense = 0;

	private Modele_Resultats_EsophagealTransit modeleApp;

	private Integer nbAcquisition;

	public TabCondense(int nbAcquisition, FenResults parent, Modele_Resultats_EsophagealTransit model) {
		super(parent, "Condensed Dynamic images");
		this.modeleApp = model;
		this.nbAcquisition = nbAcquisition;

//		this.createCaptureButton("Condense");
		this.setadditionalInfo("Condense");

		this.reloadDisplay();

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Modele_Resultats_EsophagealTransit modele = (Modele_Resultats_EsophagealTransit) modeleApp;

		if (e.getSource() instanceof JSpinner) {
			JSpinner spinner = (JSpinner) e.getSource();
			if (spinner.equals(spinnerRight)) {

				modele.rognerDicomCondenseRight(
						(int) spinner.getValue() - this.rightRognageValue[numAcquisitionCondense],
						numAcquisitionCondense);
				this.rightRognageValue[numAcquisitionCondense] = (int) spinner.getValue();

			} else if (spinner.equals(spinnerLeft)) {

				modele.rognerDicomCondenseLeft((int) spinner.getValue() - this.leftRognageValue[numAcquisitionCondense],
						numAcquisitionCondense);
				this.leftRognageValue[numAcquisitionCondense] = (int) spinner.getValue();

			}

			modele.calculImagePlusAndRoi(numAcquisitionCondense);
			modele.getImagePlusAndRoi(numAcquisitionCondense).getProcessor().setMinAndMax(0,
					(contrastSlider.getModel().getMaximum() - contrastValue[numAcquisitionCondense]) + 1);
			imageProjeterEtRoiPanel.setImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

			modele.calculCond(numAcquisitionCondense);
			modele.getCondense(numAcquisitionCondense).getProcessor().setMinAndMax(0,
					(contrastSlider.getModel().getMaximum() - contrastValue[numAcquisitionCondense]) + 1);
			// SK PROBLEME APPLICAION DE LA LUT A VOIR
			// MARCHE SI LANCE IJ DEBUG MAIS PAS SOUS FIJI
			// FAIRE MINIMAL DEBUG SAMPLE
			// ImagePlus temp=modele.getCondense(numAcquisitionCondense);
			// temp.draw();
			imageCondensePanel.setImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());

		}
	}

	@Override
	public Component getSidePanelContent() {

		this.getResultContent();

		Modele_Resultats_EsophagealTransit modele = (Modele_Resultats_EsophagealTransit) modeleApp;

		JPanel spinnerPanel = new JPanel();
		spinnerPanel.add(new JLabel("Left side"));
		spinnerLeft = new JSpinner();
		spinnerLeft.addChangeListener(this);// obliger de le faire dans la classe car à n moment donné, on a besoin de
											// le supprimer
		spinnerPanel.add(spinnerLeft);
		spinnerPanel.add(new JLabel("Right side"));
		spinnerRight = new JSpinner();
		spinnerRight.addChangeListener(this);
		spinnerPanel.add(spinnerRight);

		modele.calculAllImagePlusAndRoi();
		imageProjeterEtRoiPanel = new DynamicImage(
				modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
		imageProjeterEtRoiPanel.setLayout(new BorderLayout());

		JPanel imagePlusRognagePanel = new JPanel();
		imagePlusRognagePanel.setLayout(new BorderLayout());
		imagePlusRognagePanel.add(spinnerPanel, BorderLayout.NORTH);
		imagePlusRognagePanel.add(imageProjeterEtRoiPanel, BorderLayout.CENTER);

		JPanel radioButtonCondensePanel = new JPanel();
		radioButtonCondensePanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroupCondense = new ButtonGroup();
		radioButtonCondense = new JRadioButton[nbAcquisition];
		for (int i = 0; i < nbAcquisition; i++) {
			radioButtonCondense[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButtonCondense[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					TabCondense tab = TabCondense.this;
					for (int i = 0; i < tab.radioButtonCondense.length; i++) {
						if (((JRadioButton) e.getSource()).equals(radioButtonCondense[i])) {
							numAcquisitionCondense = i;

							spinnerLeft.removeChangeListener(tab);
							spinnerRight.removeChangeListener(tab);

							spinnerLeft.setValue(leftRognageValue[numAcquisitionCondense]);
							spinnerRight.setValue(rightRognageValue[numAcquisitionCondense]);

							spinnerLeft.addChangeListener(tab);
							spinnerRight.addChangeListener(tab);

							imageProjeterEtRoiPanel
									.setImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
							imageCondensePanel.setImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());

							contrastSlider.setValue(contrastValue[numAcquisitionCondense]);
						}
					}
				}
			});
			buttonGroupCondense.add(radioButtonCondense[i]);
			radioButtonCondensePanel.add(radioButtonCondense[i]);
		}

		JPanel radioButtonCondensePanelFlow = new JPanel();
		radioButtonCondensePanelFlow.setLayout(new FlowLayout());
		radioButtonCondensePanelFlow.add(radioButtonCondensePanel);

		// slider de contraste
		contrastSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 20, 4);
		JLabel contrastLabel = new JLabel("Contrast");

		contrastSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JSlider) {
					// changement de contraste
					ImageStatistics stat = modele.getImagePlusAndRoi(numAcquisitionCondense).getStatistics();

					int max = (int) Math.round(stat.max);
					contrastSlider.getModel().setMaximum(max);

					// creation du contraste
					modele.getImagePlusAndRoi(numAcquisitionCondense).getProcessor().setMinAndMax(0,
							(max - ((JSlider) e.getSource()).getValue()) + 1);
					imageProjeterEtRoiPanel
							.setImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

					modele.getCondense(numAcquisitionCondense).getProcessor().setMinAndMax(0,
							(max - ((JSlider) e.getSource()).getValue()) + 1);
					imageCondensePanel.setImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());

					contrastValue[numAcquisitionCondense] = ((JSlider) e.getSource()).getValue();
				}
			}
		});

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.add(radioButtonCondensePanelFlow, BorderLayout.NORTH);
		sidePanel.add(imagePlusRognagePanel, BorderLayout.CENTER);

		// Prepare panel contrast avec label et slider contrast
		JPanel contrastPanel = new JPanel();
		contrastPanel.add(contrastLabel);
		contrastPanel.add(contrastSlider);

		JPanel contrastCapture = new JPanel();
		contrastCapture.setLayout(new GridLayout(4, 1));
		contrastCapture.add(contrastPanel);
		JButton tempsFenButton = new JButton("Get Time");
		tempsFenButton.setHorizontalAlignment(SwingConstants.CENTER);
		tempsFenButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] temps = modele.getTime(numAcquisitionCondense);
				JFrame timeFen = new JFrame();
				timeFen.setLayout(new GridLayout(temps.length, 2));
				for (int i = 0; i < temps.length; i++) {
					timeFen.add(new JLabel("Image :" + i));
					timeFen.add(new JLabel("Time :" + temps[i] + ""));
				}
				timeFen.pack();
				timeFen.setVisible(true);

			}
		});

		JPanel encapsulate = new JPanel();
		encapsulate.add(tempsFenButton);

		contrastCapture.add(encapsulate);

		sidePanel.add(contrastCapture, BorderLayout.SOUTH);

		radioButtonCondense[0].setSelected(true);

		return sidePanel;
	}

	@Override
	public JPanel getResultContent() {
		Modele_Resultats_EsophagealTransit modele = (Modele_Resultats_EsophagealTransit) modeleApp;

		this.rightRognageValue = new int[nbAcquisition];
		this.leftRognageValue = new int[nbAcquisition];
		this.contrastValue = new int[nbAcquisition];
		for (int i = 0; i < contrastValue.length; i++) {
			contrastValue[i] = 4;
		}

		((Modele_Resultats_EsophagealTransit) modeleApp).calculAllCondense();

		JPanel titleAndCondensePanel = new JPanel();
		titleAndCondensePanel.setLayout(new BorderLayout());

		imageCondensePanel = new DynamicImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());
		imageCondensePanel.setLayout(new BorderLayout());
		titleAndCondensePanel.add(imageCondensePanel, BorderLayout.CENTER);

		// SidePanel sidePanelScin = new SidePanel(null,
		// modeleApp.esoPlugIn.getStudyName(), modele.getImagesPlus()[0]);
		// titleAndCondensePanel.add(sidePanelScin, BorderLayout.NORTH);

		titleAndCondensePanel
				.setPreferredSize(new Dimension((int) (this.parent.getWidth() * 0.75), imageCondensePanel.getHeight()));

		return titleAndCondensePanel;
	}

}
