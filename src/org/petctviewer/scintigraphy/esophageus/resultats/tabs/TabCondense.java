package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import java.awt.BorderLayout;
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

import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.Modele_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.plugin.ContrastEnhancer;

public class TabCondense extends JPanel implements ChangeListener{
	 
	//Spinners
	 private JSpinner spinnerRight;
	 private JSpinner spinnerLeft;
	 private DynamicImage imageCondensePanel;
	 
	 private int rightRognageValue[];
	 private int leftRognageValue[];
	 private int contrastValue[];
	 
	 private JSlider contrastSlider ;
	 
	 private DynamicImage imageProjeterEtRoiPanel;
	 
	 private JRadioButton [] radioButtonCondense;
	 
	 private static int numAcquisitionCondense = 0;
	
	 
	 private Modele_Resultats_EsophagealTransit modele;
	 
	public TabCondense(int nbAcquisition , Modele_Resultats_EsophagealTransit modele, Modele_EsophagealTransit modeleApp) {
		this.modele = modele;
		
		this.setLayout(new BorderLayout());

		this.rightRognageValue = new int[nbAcquisition];
		this.leftRognageValue = new int[nbAcquisition];
		this.contrastValue = new int[nbAcquisition];
		for(int i =0; i< contrastValue.length; i++) {
			contrastValue[i]=4;
		}
		
		
		modele.calculAllCondense();
		
		imageCondensePanel = new DynamicImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());
		imageCondensePanel.setLayout( new BorderLayout());
		this.add(imageCondensePanel, BorderLayout.CENTER);
		
		JPanel spinnerPanel = new JPanel();
		spinnerPanel.add(new JLabel("Left side"));
		spinnerLeft = new JSpinner();    
		spinnerLeft.addChangeListener(this);// obliger de le faire dans la classe car à n moment donné, on a besoin de le supprimer
		spinnerPanel.add(spinnerLeft);
		spinnerPanel.add(new JLabel("Right side"));
		spinnerRight = new JSpinner();
		spinnerRight.addChangeListener(this);
		spinnerPanel.add(spinnerRight);
		
		modele.calculAllImagePlusAndRoi();
		imageProjeterEtRoiPanel = new DynamicImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
		imageProjeterEtRoiPanel.setLayout(new BorderLayout());
		
		JPanel imagePlusRognagePanel = new JPanel();
		imagePlusRognagePanel.setLayout(new BorderLayout());
		imagePlusRognagePanel.add(spinnerPanel, BorderLayout.NORTH);
		imagePlusRognagePanel.add(imageProjeterEtRoiPanel, BorderLayout.CENTER);

		JPanel radioButtonCondensePanel = new JPanel();
		radioButtonCondensePanel.setLayout(new GridLayout(nbAcquisition, 1));
		
	    ButtonGroup buttonGroupCondense = new ButtonGroup();    
	    radioButtonCondense = new JRadioButton[nbAcquisition];
	    for(int i =0; i< nbAcquisition; i++) {
	    	radioButtonCondense[i] = new JRadioButton("Acquisition "+(i+1));
	    	radioButtonCondense[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					TabCondense tab =  TabCondense.this;
					for(int i =0; i<tab.radioButtonCondense.length; i++) {
						if(((JRadioButton)e.getSource()).equals(radioButtonCondense[i])) {
							numAcquisitionCondense = i;
						
							spinnerLeft.removeChangeListener(tab);
							spinnerRight.removeChangeListener(tab);
							
							spinnerLeft.setValue(leftRognageValue[numAcquisitionCondense]);
							spinnerRight.setValue(rightRognageValue[numAcquisitionCondense]);
							
							spinnerLeft.addChangeListener(tab);
							spinnerRight.addChangeListener(tab);
							
							imageProjeterEtRoiPanel.setImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
							imageCondensePanel.setImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());
							
							
							contrastSlider.setValue(contrastValue[i]);
						}
					}
				}
			});
	    	buttonGroupCondense.add(radioButtonCondense[i]);
	    	radioButtonCondensePanel.add( radioButtonCondense[i]);
	    }
	   
	    
	    JPanel radioButtonCondensePanelFlow = new JPanel();
		radioButtonCondensePanelFlow.setLayout(new FlowLayout());
		radioButtonCondensePanelFlow.add(radioButtonCondensePanel);
		
		
		//slider de contraste
		 contrastSlider = new JSlider(SwingConstants.HORIZONTAL,0,20,4);
		JLabel contrastLabel = new JLabel("Contrast");
		
		contrastSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(e.getSource() instanceof JSlider) {
					System.out.println("slider :"+((JSlider)e.getSource()).getValue());

					//changement de contraste
					ContrastEnhancer ce = new ContrastEnhancer();
							
					ce.stretchHistogram(modele.getImagePlusAndRoi(numAcquisitionCondense), ((JSlider)e.getSource()).getValue());
					imageProjeterEtRoiPanel.setImage(modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

					ce.stretchHistogram(modele.getCondense(numAcquisitionCondense), ((JSlider)e.getSource()).getValue());
					imageCondensePanel.setImage(modele.getCondense(numAcquisitionCondense).getBufferedImage());
					
					contrastValue[numAcquisitionCondense] = ((JSlider)e.getSource()).getValue();
				}
			}
		});
		
	    
		JPanel  sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.add(radioButtonCondensePanelFlow, BorderLayout.NORTH);
		sidePanel.add(imagePlusRognagePanel, BorderLayout.CENTER);
		
		JPanel contrastCapture = new JPanel();
		contrastCapture.setLayout(new GridLayout(3, 1));
		contrastCapture.add(contrastSlider);	
		JButton captureButton = new JButton("Capture");
		JLabel lblCredit = new JLabel("Provided by petctviewer.org");
		lblCredit.setVisible(false);
		contrastCapture.add(captureButton);
		contrastCapture.add(lblCredit);
		JButton tempsFenButton = new JButton("Time");
		tempsFenButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				double [] temps = TabCondense.this.modele.getTime(numAcquisitionCondense);
				JFrame timeFen = new JFrame();
				timeFen.setLayout(new GridLayout(temps.length, 1));
				for(int i =0; i< temps.length; i++) {
					timeFen.add(new JLabel(temps[i]+""));
				}
				timeFen.pack();
				timeFen.setVisible(true);
				
			}
		});
		contrastCapture.add(tempsFenButton);
		
		
		sidePanel.add(contrastCapture,BorderLayout.SOUTH);
		
		this.add(sidePanel, BorderLayout.EAST);
		 
		modeleApp.esoPlugIn.setCaptureButton(captureButton, lblCredit , this, modele, "Condense");

		
		radioButtonCondense[0].setSelected(true);

	}

	@Override
	public void stateChanged(ChangeEvent e) {

		 if( e.getSource() instanceof JSpinner) {
				JSpinner spinner = (JSpinner)e.getSource();
				if(spinner.equals(spinnerRight)) {

					//System.out.println("pinner right "+((int)spinner.getValue()- this.rightRognageValue));
					
					this.modele.rognerDicomCondenseRight((int)spinner.getValue()- this.rightRognageValue[numAcquisitionCondense],numAcquisitionCondense);
					this.rightRognageValue[numAcquisitionCondense] = (int)spinner.getValue();
					
					 this.modele.calculCond(numAcquisitionCondense);
					 imageCondensePanel.setImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());

					 
					 this.modele.calculImagePlusAndRoi(numAcquisitionCondense);
					 imageProjeterEtRoiPanel.setImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

				}else if(spinner.equals(spinnerLeft)) {
					//System.out.println("pinner left "+spinner.getValue());
					
					this.modele.rognerDicomCondenseLeft((int)spinner.getValue()- this.leftRognageValue[numAcquisitionCondense],numAcquisitionCondense);
					this.leftRognageValue[numAcquisitionCondense] = (int)spinner.getValue();
					 
					this.modele.calculCond(numAcquisitionCondense);
					 imageCondensePanel.setImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());

					 this.modele.calculImagePlusAndRoi(numAcquisitionCondense);
					 imageProjeterEtRoiPanel.setImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
				}
				
			
			}
	}
	
}
