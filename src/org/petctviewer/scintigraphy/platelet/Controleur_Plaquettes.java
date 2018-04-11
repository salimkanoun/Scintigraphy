/*
Copyright (C) 2017 KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.platelet;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import org.petctviewer.scintigraphy.shunpo.Modele_Shunpo;
import org.petctviewer.scintigraphy.shunpo.Vue_Shunpo;
import org.petctviewer.scintigraphy.view.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.CanvasResizer;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class Controleur_Plaquettes implements ActionListener {
	
	private Vue_Plaquettes laVue ;
	
	private String etat ;
	
	private String[] listeInstructions = {"Delimit the Spleen", "Delimit the liver", "Delimit the heart", "Validate"} ;
			
	private Modele_Plaquettes leModele ;

	private int index=0;
	
	protected static boolean showLog;
	
	private String tagCapture;
	
	private int cycles=0;
	
	
	//Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue, Modele_Plaquettes leModele) {
		this.laVue = vue ;
		this.leModele = leModele ;
		this.etat = "Spleen Post";
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource() ;
		
		if (b == laVue.lesBoutons.get("Suivant")) {
			//IJ.log("Suivant, Etat"+etat.toString());
			
			if (etat.equals("Spleen Post")) {
				etat= "Liver Post";
				tagCapture=Modele_Shunpo.genererDicomTagsPartie1(laVue.win.getImagePlus(), "Platelet");
				addRoi("Spleen Post" + cycles);
				leModele.enregisterMesure("Spleen Post",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.setInstructions(listeInstructions[1]);
				laVue.win.getImagePlus().killRoi();
				
				// Si la ROI suivante est deja presente on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				}
				//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
				else if (cycles>0 && laVue.leRoi.getRoi((index))== null){
					laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
				}
			}
				
				
			else if (etat.equals("Liver Post")) {
				etat="Heart Post";
				addRoi("Liver Post" + cycles);
				leModele.enregisterMesure("Liver Post",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.setInstructions(listeInstructions[2]);
				laVue.win.getImagePlus().killRoi();
				// Si la ROI suivante est deja presente on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				}
				//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
				else if (cycles>0 && laVue.leRoi.getRoi((index))== null){
					laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
				}
			}
				
			else if (etat.equals("Heart Post")) {
				addRoi("Heart Post" + cycles);
				leModele.enregisterMesure("Heart Post",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.win.getImagePlus().killRoi();
				// Si on traite une seule vue
				if (!laVue.antPost){
					//Tant qu'on est pas sur la dernière image on revient sur spleen
					if ( (cycles+1) < laVue.nombreAcquisitions){
						//On recommence tout un cycle
						etat="Spleen Post";
						laVue.overlay.clear();
						VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
						//On ouvre le noveau cycle
						cycles++;
						laVue.win.showSlice(laVue.win.getImagePlus().getCurrentSlice() + 1);
						laVue.setInstructions(listeInstructions[0]);
						//On regarde disponibilite de la ROI suivante ou copie de ROI du cycle n-1
						if (laVue.leRoi.getRoi((index))!= null) {
							laVue.leRoi.select(index);
						}
						//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
						else {
							laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
						}
					}
					//Si on est sur la derniere image c'est fini
					else {
						//On a Fini
						etat="Fin";
					}
				}
				//Si imageAntPost on continue de traiter l'imageAnt dans le meme cycle avant de l'incrementer
				else {
					//On fait les etapes Ant 
					etat="Spleen Ant";
					laVue.overlay.clear();
					VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
					//On note qu'on traitre l'image Ant
					laVue.win.showSlice(laVue.win.getImagePlus().getCurrentSlice() + 1);
					
					if (laVue.leRoi.getRoi((index))!= null) {
						laVue.leRoi.select(index);
					}
					//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
					else if (laVue.leRoi.getRoi((index))== null){
						laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
					}
					
					laVue.setInstructions(listeInstructions[0]);
				}
				
				
			}
			
			else if (etat.equals("Spleen Ant")) {
				etat="Liver Ant";
				laVue.setInstructions(listeInstructions[1]);
				addRoi("Spleen Ant" + cycles);
				leModele.enregisterMesure("Spleen Ant",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.win.getImagePlus().killRoi();
				
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				}
				//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
				else {
					laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
				}
				
			}
			
			else if (etat.equals("Liver Ant")) {
				etat="Heart Ant";
				laVue.setInstructions(listeInstructions[2]);
				addRoi("Liver Ant" + cycles);
				leModele.enregisterMesure("Liver Ant",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.win.getImagePlus().killRoi();
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
				
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				}
				//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
				else {
					laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
				}
			}
			
			else if (etat.equals("Heart Ant")) {
				addRoi("Heart Ant" + cycles);
				leModele.enregisterMesure("Heart Ant",laVue.win.getImagePlus());
				//index -1 car add roi incremente l'index de 1
				laVue.overlay.add(laVue.leRoi.getRoi(index-1));
				laVue.win.getImagePlus().killRoi();

				if ( (cycles+1) < laVue.nombreAcquisitions){
					//On recommence tout un cycle
					etat="Spleen Post";
					laVue.overlay.clear();
					VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
					laVue.win.showSlice( laVue.win.getImagePlus().getCurrentSlice() + 1);
					laVue.setInstructions(listeInstructions[0]);
					if (laVue.leRoi.getRoi((index))!= null) {
						laVue.leRoi.select(index);
					}
					//Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
					else {
						laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(index-3).clone());
					}
					cycles++;
				}
				else {
					//On a Fini
					etat="Fin";
					laVue.setInstructions(listeInstructions[3]);
				}
				
			}
				
			else if (etat.equals("Fin")) {
				ImagePlus capture =Modele_Shunpo.captureImage(laVue.win.getImagePlus(), 512,512);
				//On resize le canvas pour etre a la meme taille que les courbes
				ImageProcessor ip=capture.getProcessor();
				CanvasResizer canvas = new CanvasResizer();
				ImageProcessor iptemp=canvas.expandImage(ip, 640, 512, (640-512)/2, 0);
				capture.setProcessor(iptemp);
				IJ.log("avant get results");
				JTable tableResultats=leModele.getResults();
				IJ.log("apres get results");
				ImagePlus[] courbes=leModele.createDataset(tableResultats);
				
				ImageStack stack=new ImageStack(640,512);
				stack.addSlice(capture.getProcessor());
				for (int i =0 ; i<courbes.length; i++) {
					stack.addSlice(courbes[i].getProcessor());
				}
				IJ.log("Apres add image stack");
				
				ImagePlus courbesStackImagePlus=new ImagePlus();
				courbesStackImagePlus.setStack(stack);
				
				ImagePlus courbesFinale=new ImagePlus();
				IJ.log("Avan Montage");
				MontageMaker mm = new MontageMaker();
				courbesFinale = mm.makeMontage2(courbesStackImagePlus, 2, 2, 1, 1, courbesStackImagePlus.getStackSize(), 1, 0, false);
				IJ.log("apres Montage");
				laVue.UIResultats(courbesFinale, tableResultats);
			}
			
		}
		
		if (b == laVue.lesBoutons.get("Capture")){
				laVue.lesBoutons.get("Capture").setVisible(false);
				laVue.Csv.setText("Provided By Petctviewer.org");
				ImagePlus captureFinale =Modele_Shunpo.captureFenetre(WindowManager.getCurrentImage(),0,0);
				WindowManager.getCurrentWindow().getImagePlus().changes=false;
				WindowManager.getCurrentWindow().close();
				//On genere la 2eme partie des tag dicom et on l'ajoute a la 1ere partie dans le property de l'image finale
				captureFinale.setProperty("Info", tagCapture+=(Modele_Shunpo.genererDicomTagsPartie2(captureFinale)));
				//On affiche et on agrandie la fenetre de la capture finale
				captureFinale.show();
				//On met un zoom a 80%
				captureFinale.getCanvas().setMagnification(0.8);
				// SK FAIRE GENERATION CSV?
				
				//On fait la capture finale
				captureFinale.getWindow().toFront();
				//On propose de sauver la capture en DICOM
				IJ.run("myDicom...");
				//fin du programme ici
					
		}

		if (b == laVue.lesBoutons.get("Precedent")) {
			switch(etat) {
			
			//SK A FAIRE
			//TODO
			
			}
		}
		
		if (b == laVue.lesBoutons.get("Draw ROI")) {
			laVue.lesBoutons.get("Draw ROI").setBackground(Color.LIGHT_GRAY);
			laVue.lesBoutons.get("Contrast").setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}
			
		
		if (b == laVue.lesBoutons.get("Contrast")) {
			laVue.lesBoutons.get("Draw ROI").setBackground(null);
			laVue.lesBoutons.get("Contrast").setBackground(Color.LIGHT_GRAY);
			IJ.run("Window Level Tool");
		}
			
			
		
		if (b == laVue.lesBoutons.get("Quitter")) {
			laVue.end("") ;
			return;
			}
		
		if (b == laVue.lesBoutons.get("Show Log"))
			
			//Regarder methode de Ping pour changer le libelle des bouttons
			if (!showLog){
				showLog=true;
				laVue.lesBoutons.get("Show Log").setLabel("Hide Log");
				laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}
			
			else{
				showLog=false;
				laVue.lesBoutons.get("Show Log").setLabel("Show Log");
				laVue.lesBoutons.get("Show").setBackground(null);
			}
		
	}
	

	private void addRoi(String nom){
		// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter pour eviter les doublons
		if (laVue.leRoi.getRoi(index)==null){
		laVue.leRoi.add(laVue.win.getImagePlus(), laVue.win.getImagePlus().getRoi(), index);
		laVue.leRoi.rename(index, nom);
		}
		// Si elle existe on fait un update. Si elle a 閠� perdue dans l'imagePlus on revient a la ROI sauvegardee et on notifie l'utilisateur
		else {
			if (laVue.win.getImagePlus().getRoi()==null){
				IJ.showMessage("Roi lost, restoring previous saved ROI");
				laVue.leRoi.select(index);
			}
			laVue.leRoi.runCommand("Update");
		}
		index++;
	}

	/*private void retour() {
		//A GERER
		//TODO
		//etat = etat.previous() ;
		//index -- ;
		//laVue.setInstructions(listeInstructions[index]);
		//laVue.leRoi.select(index);	
	}
	*/
}
