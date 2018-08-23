package org.petctviewer.scintigraphy.esophageus;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.petctviewer.scintigraphy.esophageus.application.Controleur_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.application.FenApplication_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class EsophagealTransit_FenDynamique extends Scintigraphy{

	private ImagePlus[] originalImp;
	private ArrayList<ImagePlus> impAnt;
	private int nbAcquisition;
	private int numAcquisiton;
	
	public EsophagealTransit_FenDynamique() {
		super("Esophageal Transit");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		numAcquisiton=0;
		
		impAnt = new ArrayList<>();
		
		// on ne prend que la ant
		for(int i =0; i< images.length; i++){
			if(Scintigraphy.sortDynamicAntPost(images[i])[0] != null) {
				impAnt.add( Scintigraphy.sortDynamicAntPost(images[i])[0].duplicate());
			}
		}
		
		// les imp original avec ant et post pour la fen application
		originalImp = images;
		
		nbAcquisition = impAnt.size();
		
		
		//on y met la première image ant
		return impAnt.get(0);
	}

	
	
	
	
	
	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication fen = new FenApplication(this.getImp(), "Oesophageus");
		fen.getPanel_Quit_Draw_Contrast_btns().remove(fen.getBtn_drawROI());
		fen.getPanelInstructionsTextBtn().removeAll();
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(nbAcquisition, 1));
		
	    ButtonGroup buttonGroup = new ButtonGroup();    
	    JRadioButton[] radioButton = new JRadioButton[nbAcquisition];
	    for( int i =0; i< nbAcquisition; i++) {
	    	int num=i;
	    	radioButton[numAcquisiton] = new JRadioButton("Acquisition "+(i+1));
	    	radioButton[numAcquisiton].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					System.out.println("numacqui"+num);
					fen.setImp(impAnt.get(num));
				}
			});
	    	buttonGroup.add(radioButton[numAcquisiton]);
	    	radioButtonPanel.add(radioButton[numAcquisiton]);
	    }
	   
	    
	    JPanel radioButtonPanelFlow = new JPanel();
		radioButtonPanelFlow.setLayout(new FlowLayout());
		radioButtonPanelFlow.add(radioButtonPanel);
		
		radioButton[0].setEnabled(true);
		
		fen.getPanelInstructionsTextBtn().add(radioButtonPanelFlow);
		
		
		
		
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		
		/*
		ControleurDynamique_EsophagealTransit cdet = new ControleurDynamique_EsophagealTransit(this);
		this.getFenApplication().setControleur(cdet);*/
		this.getFenApplication().setVisible(true);
		
	}

	
	
}
