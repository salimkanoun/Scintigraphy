/**
Copyright (C) 2017 MOHAND Mathis and KANOUN Salim
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

package org.petctviewer.scintigraphy.shunpo;

import java.awt.Button;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.util.DicomTools;

public class Controleur_Shunpo implements ActionListener {
	
	private Vue_Shunpo laVue ;
	
	private Modele_Shunpo.Etat etat ;
	
	private String[] listeInstructions = {"Delimit the right lung.", "Delimit the left lung", "Delimit the right kidney", "Delimit the left kidney", "Modify the background if needed", "Adjust the right lung", "Adjust the left lung", "Adjust the right kidney", "Adjust the left kidney","Adjust the Background", "Delimit the brain", "Adjust brain"} ;
			
	private Modele_Shunpo leModele ;

	private int index;
	
	protected static boolean showLog;
	
	private String tagCapture;
	
	private String nomProgramme="Pulmonary Shunt";
	
	protected ImagePlus[] capture=new ImagePlus[4];
	
	//Sert au restart
	protected Controleur_Shunpo(Vue_Shunpo vue, Modele_Shunpo leModele) {
		this.index = 0 ;
		this.laVue = vue ;
		this.leModele = leModele ;
		this.etat = Modele_Shunpo.Etat.PoumonD_Post;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource() ;
		
		if (b == this.laVue.lesBoutons.get("Suivant")) {
			//IJ.log("Suivant, Etat"+etat.toString());
			switch(this.etat) {
			case PoumonD_Post:
				addRoi("Right Lung Post");
				this.leModele.calculerCoups("PDP",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();;
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				this.laVue.lesBoutons.get("Precedent").setEnabled(true);
				String tag = DicomTools.getTag(this.laVue.win.getImagePlus(), "0010,0010");
				this.leModele.setPatient(tag,this.laVue.win.getImagePlus());
				this.tagCapture=ModeleScin.genererDicomTagsPartie1(this.laVue.win.getImagePlus(), this.nomProgramme);
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				break;
			case PoumonG_Post:
				addRoi("Left Lung Post");
				this.leModele.calculerCoups("PGP",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				break;
			case ReinD_Post:
				addRoi("Right Kidney");
				this.leModele.calculerCoups("RDP",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();;
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				break;
			case ReinG_Post:
				addRoi("Left Kidney");
				this.leModele.calculerCoups("RGP",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();;
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				//Methode gen猫re la ROI BDF
				genererBDF();
				//On affiche toutes les ROI dessin茅es, l'overlay est deja en set et le DG impl茅mente pour cette 1ere fois
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(0));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(1));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(2));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(3));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(4));
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				
				break;
			case BDF :
				if (this.laVue.win.getImagePlus().getRoi()==null){
					genererBDF();
					IJ.showMessage("Regenerating missing background");
					//Voir comment 脿 fait ping pour revenir dans la boucle
				}
				addRoi("Background");
				this.leModele.calculerCoups("BDFP",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();
				//On prend la capture
				this.capture[0]=ModeleScin.captureImage(this.laVue.win.getImagePlus(),512,512);
				//On efface l'overlay pour repartir 脿 zero
				this.laVue.overlay.clear();
				//On remet le Droit Gauche
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.laVue.win.getImagePlus().deleteRoi();
				//On copie la ROI 0 qui correpondait au poumon en posterieur
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(0));
				this.laVue.win.showSlice(1);
				this.etat = this.etat.next() ;
				VueScin.setOverlayDG(this.laVue.overlay, this.laVue.win.getImagePlus());
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				break;
			case PoumonD_Ant:
				addRoi("Right Lung Ant");
				this.leModele.calculerCoups("PDA",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().deleteRoi();
				// On copie la ROI 1 qui correspondait au poumon en posterieur
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(1).clone());
				this.etat = this.etat.next();
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
				};
				break;
			case PoumonG_Ant:
				addRoi("Left Lung Ant");
				this.leModele.calculerCoups("PGA",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(2).clone());
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
					};
				break;
			case ReinD_Ant:
				addRoi("Right Kidney Ant");
				this.leModele.calculerCoups("RDA",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(3).clone());
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
					};
				break;
			case ReinG_Ant:
				addRoi("Left Kidney Ant");
				this.leModele.calculerCoups("RGA",this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().killRoi();
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.etat = this.etat.next() ;
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(4).clone());
				//Ajouter les Overlay ici
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(5));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(6));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(7));
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(8));
				if (this.laVue.leRoi.getRoi((this.index))!= null) {
					this.laVue.leRoi.select(this.index);
					};
				break;
			case Poumon_valide:
				addRoi("Background Ant");
				this.leModele.calculerCoups("BDFA",this.laVue.win.getImagePlus());
				//On ne peut plus revenir en arriere
				this.laVue.lesBoutons.get("Precedent").setEnabled(false);
				//On prend la capture
				this.capture[1]=ModeleScin.captureImage(this.laVue.win.getImagePlus(),512,512);
				//On efface l'overlay pour repartir 脿 zero dans l'overlay
				this.laVue.overlay.clear();
				this.laVue.win.getImagePlus().killRoi();
				this.etat = this.etat.next() ;
				//On demande d'ouvrir l'image Cerveau , attention source de bug, interface doit 锚tre 脿 l'arret pour laisser l'utilisateur ouvrir l'image et inserer l'imageplus dans la fenetre courante
				this.laVue.ouvrirImage("Brain");
				break;
			case Cerveau_Post:
				this.laVue.win.showSlice(2);
				this.etat = this.etat.next() ;
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				this.laVue.lesBoutons.get("Precedent").setEnabled(false);
				IJ.setTool(Toolbar.POLYGON);
				break;
			case Cerveau_Ant:
				this.laVue.lesBoutons.get("Precedent").setEnabled(true);
				addRoi("Brain Post");
				this.leModele.calculerCoups("CP",this.laVue.win.getImagePlus());
				//Label Ne s'affiche pas probablement un refresh 脿 forcer==> Probleme de draw de l'overlay avant capture // EDT
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(10));
				//FIN A DEBEUGER SALIM
				this.laVue.win.getImagePlus().setOverlay(this.laVue.overlay);
				this.capture[2]=ModeleScin.captureImage(this.laVue.win.getImagePlus(),512,512);
				this.etat = this.etat.next() ;
				this.laVue.overlay.clear();
				this.laVue.win.getImagePlus().setRoi((Roi) this.laVue.leRoi.getRoi(10));
				this.laVue.win.showSlice(1);
				VueScin.setOverlayDG(this.laVue.overlay, this.laVue.win.getImagePlus());
				this.laVue.win.getImagePlus().setOverlay(this.laVue.overlay);
				this.laVue.setInstructions(this.listeInstructions[this.index]);
				break;
			case Fin:
				addRoi("Brain Ant");
				this.leModele.calculerCoups("CA",this.laVue.win.getImagePlus());
				this.laVue.overlay.add(this.laVue.leRoi.getRoi(12));
				this.capture[3]=ModeleScin.captureImage(this.laVue.win.getImagePlus(),512,512);
				this.laVue.win.getImagePlus().deleteRoi();
				this.laVue.overlay.clear();
				String[] resultats = this.leModele.resultats();
				this.laVue.labelsResultats(resultats) ;
				//On passe les capture en stack
				ImageStack stackCapture=ModeleScin.captureToStack(this.capture);
				//on fait le montage du stack et on g茅n猫re l'interface resultat
				this.laVue.UIResultats(this.leModele.montage(stackCapture, this.nomProgramme));
				this.laVue.win.close();
				this.laVue.leRoi.close();
			}
		}
		if (b == this.laVue.lesBoutons.get("Capture")){
				this.laVue.lesBoutons.get("Capture").setVisible(false);
				this.laVue.Csv.setText("Provided By Petctviewer.org");
				ImagePlus captureFinale =ModeleScin.captureFenetre(WindowManager.getCurrentImage(),0,0);
				WindowManager.getCurrentWindow().getImagePlus().changes=false;
				WindowManager.getCurrentWindow().close();
				//On genere la 2eme partie des tag dicom et on l'ajoute 脿 la 1ere partie dans le property de l'image finale
				captureFinale.setProperty("Info", this.tagCapture+=(ModeleScin.genererDicomTagsPartie2(captureFinale)));
				//On affiche et on agrandie la fenetre de la capture finale
				captureFinale.show();
				captureFinale.getCanvas().setScaleToFit(true);
				//On met un zoom a 80%
				captureFinale.getCanvas().setMagnification(0.8);
				//On sauve les resultats en CSV et ZIP
				try {
					String[] resultatscsv= this.leModele.buildCSVResultats();
					ModeleScin.exportAll(resultatscsv,2,this.laVue.leRoi, this.nomProgramme,captureFinale);
					} catch (FileNotFoundException e) {}
				//On fait la capture finale
				captureFinale.getWindow().toFront();
				//On propose de sauver la capture en DICOM
				IJ.run("myDicom...");
				//fin du programme ici
					
		}

		if (b == this.laVue.lesBoutons.get("Precedent")) {
			switch(this.etat) {
			
			case PoumonD_Post:
				break;
				
			case PoumonG_Post:
				retour();
				break;
				
			case ReinD_Post:
				retour();
				break;
				
			case ReinG_Post:
				retour();
				break;
				
			case BDF:
				retour();
				this.laVue.overlay.clear();
				VueScin.setOverlayDG(this.laVue.overlay, this.laVue.win.getImagePlus());
				break;
				
			case PoumonD_Ant:
				retour();
				this.laVue.win.showSlice(2);
				break;
				
			case PoumonG_Ant:
				retour();
				break;
				
			case ReinD_Ant:
				retour();
				break;
				
			case ReinG_Ant:
				retour();
				break;
				
			case Poumon_valide:
				retour();
				this.laVue.overlay.clear();
				VueScin.setOverlayDG(this.laVue.overlay, this.laVue.win.getImagePlus());
				break;
				
			case Cerveau_Post:
				break;
				
			case Cerveau_Ant:
				break;
				
			case Fin:
				retour();
				this.laVue.win.showSlice(1);
				break;
			
			}
		}
		if (b == this.laVue.lesBoutons.get("Draw ROI")) {
			this.laVue.lesBoutons.get("Draw ROI").setBackground(Color.LIGHT_GRAY);
			this.laVue.lesBoutons.get("Contrast").setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}
			
		
		if (b == this.laVue.lesBoutons.get("Contrast")) {
			this.laVue.lesBoutons.get("Draw ROI").setBackground(null);
			this.laVue.lesBoutons.get("Contrast").setBackground(Color.LIGHT_GRAY);
			IJ.run("Window Level Tool");
		}
			
			
		
		if (b == this.laVue.lesBoutons.get("Quitter")) {
			this.laVue.end("") ;
			return;
			}
		
		if (b == this.laVue.lesBoutons.get("Show Log"))
			
			//Regarder methode de Ping pour changer le libelle des bouttons
			if (!showLog){
				showLog=true;
				this.laVue.lesBoutons.get("Show Log").setLabel("Hide Log");
				this.laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}
			
			else{
				showLog=false;
				this.laVue.lesBoutons.get("Show Log").setLabel("Show Log");
				this.laVue.lesBoutons.get("Show").setBackground(null);
			}
		
	}
	
	private void genererBDF() {
		Roi[] rois = this.laVue.leRoi.getRoisAsArray() ;
		Rectangle r = new Rectangle(15, 30);
		int x = (int) ((rois[2].getBounds().getLocation().x + rois[3].getBounds().getLocation().x + rois[3].getBounds().getWidth() ) / 2) ;
		int y = (rois[2].getBounds().getLocation().y + rois[3].getBounds().getLocation().y ) / 2 ;
		r.setLocation(x, y);
		this.laVue.win.getImagePlus().setRoi(r);
	}

	private void addRoi(String nom){
		// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter pour eviter les doublons
		if (this.laVue.leRoi.getRoi(this.index)==null){
		this.laVue.leRoi.add(this.laVue.win.getImagePlus(), this.laVue.win.getImagePlus().getRoi(), this.index);
		this.laVue.leRoi.rename(this.index, nom);
		}
		// Si elle existe on fait un update. Si elle a 閠� perdue dans l'imagePlus on revient a la ROI sauvegardee et on notifie l'utilisateur
		else {
			if (this.laVue.win.getImagePlus().getRoi()==null){
				IJ.showMessage("Roi lost, restoring previous saved ROI");
				this.laVue.leRoi.select(this.index);
			}
			this.laVue.leRoi.runCommand("Update");
		}
		this.index++;
	}

	private void retour() {
		this.etat = this.etat.previous() ;
		this.index -- ;
		this.laVue.setInstructions(this.listeInstructions[this.index]);
		this.laVue.leRoi.select(this.index);	
	}
}
