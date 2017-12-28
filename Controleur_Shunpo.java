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


import java.awt.Button;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

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
		
		if (b == laVue.lesBoutons.get("Suivant")) {
			//IJ.log("Suivant, Etat"+etat.toString());
			switch(etat) {
			case PoumonD_Post:
				addRoi("Right Lung Post");
				leModele.calculerCoups("PDP",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();;
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				laVue.lesBoutons.get("Precedent").setEnabled(true);
				String tag = DicomTools.getTag(laVue.win.getImagePlus(), "0010,0010");
				leModele.setPatient(tag,laVue.win.getImagePlus());
				tagCapture=Modele_Shunpo.genererDicomTagsPartie1(laVue.win.getImagePlus(), nomProgramme);
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				break;
			case PoumonG_Post:
				addRoi("Left Lung Post");
				leModele.calculerCoups("PGP",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				break;
			case ReinD_Post:
				addRoi("Right Kidney");
				leModele.calculerCoups("RDP",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();;
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				break;
			case ReinG_Post:
				addRoi("Left Kidney");
				leModele.calculerCoups("RGP",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();;
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				//Methode gen猫re la ROI BDF
				genererBDF();
				//On affiche toutes les ROI dessin茅es, l'overlay est deja en set et le DG impl茅mente pour cette 1ere fois
				laVue.overlay.add(laVue.leRoi.getRoi(0));
				laVue.overlay.add(laVue.leRoi.getRoi(1));
				laVue.overlay.add(laVue.leRoi.getRoi(2));
				laVue.overlay.add(laVue.leRoi.getRoi(3));
				laVue.overlay.add(laVue.leRoi.getRoi(4));
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				
				break;
			case BDF :
				if (laVue.win.getImagePlus().getRoi()==null){
					genererBDF();
					IJ.showMessage("Regenerating missing background");
					//Voir comment 脿 fait ping pour revenir dans la boucle
				}
				addRoi("Background");
				leModele.calculerCoups("BDFP",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();
				//On prend la capture
				capture[0]=Modele_Shunpo.captureImage(laVue.win.getImagePlus(),512,512);
				//On efface l'overlay pour repartir 脿 zero
				laVue.overlay.clear();
				//On remet le Droit Gauche
				laVue.setInstructions(listeInstructions[index]);
				laVue.win.getImagePlus().deleteRoi();
				//On copie la ROI 0 qui correpondait au poumon en posterieur
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(0));
				laVue.win.showSlice(1);
				etat = etat.next() ;
				laVue.overlayDG();
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				break;
			case PoumonD_Ant:
				addRoi("Right Lung Ant");
				leModele.calculerCoups("PDA",laVue.win.getImagePlus());
				laVue.win.getImagePlus().deleteRoi();
				// On copie la ROI 1 qui correspondait au poumon en posterieur
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(1).clone());
				etat = etat.next();
				laVue.setInstructions(listeInstructions[index]);
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
				};
				break;
			case PoumonG_Ant:
				addRoi("Left Lung Ant");
				leModele.calculerCoups("PGA",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(2).clone());
				//On verifie que la ROI suivante n'est pas deja pr茅sente dans le ROI manager(cas d'un retour) auquel cas on l'affiche
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
					};
				break;
			case ReinD_Ant:
				addRoi("Right Kidney Ant");
				leModele.calculerCoups("RDA",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(3).clone());
				laVue.setInstructions(listeInstructions[index]);
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
					};
				break;
			case ReinG_Ant:
				addRoi("Left Kidney Ant");
				leModele.calculerCoups("RGA",laVue.win.getImagePlus());
				laVue.win.getImagePlus().killRoi();
				laVue.setInstructions(listeInstructions[index]);
				etat = etat.next() ;
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(4).clone());
				//Ajouter les Overlay ici
				laVue.overlay.add(laVue.leRoi.getRoi(5));
				laVue.overlay.add(laVue.leRoi.getRoi(6));
				laVue.overlay.add(laVue.leRoi.getRoi(7));
				laVue.overlay.add(laVue.leRoi.getRoi(8));
				if (laVue.leRoi.getRoi((index))!= null) {
					laVue.leRoi.select(index);
					};
				break;
			case Poumon_valide:
				addRoi("Background Ant");
				leModele.calculerCoups("BDFA",laVue.win.getImagePlus());
				//On ne peut plus revenir en arriere
				laVue.lesBoutons.get("Precedent").setEnabled(false);
				//On prend la capture
				capture[1]=Modele_Shunpo.captureImage(laVue.win.getImagePlus(),512,512);
				//On efface l'overlay pour repartir 脿 zero dans l'overlay
				laVue.overlay.clear();
				laVue.win.getImagePlus().killRoi();
				etat = etat.next() ;
				//On demande d'ouvrir l'image Cerveau , attention source de bug, interface doit 锚tre 脿 l'arret pour laisser l'utilisateur ouvrir l'image et inserer l'imageplus dans la fenetre courante
				laVue.ouvrirImage("Brain");
				break;
			case Cerveau_Post:
				laVue.win.showSlice(2);
				etat = etat.next() ;
				laVue.setInstructions(listeInstructions[index]);
				laVue.lesBoutons.get("Precedent").setEnabled(false);
				IJ.setTool(Toolbar.POLYGON);
				break;
			case Cerveau_Ant:
				laVue.lesBoutons.get("Precedent").setEnabled(true);
				addRoi("Brain Post");
				leModele.calculerCoups("CP",laVue.win.getImagePlus());
				//Label Ne s'affiche pas probablement un refresh 脿 forcer==> Probleme de draw de l'overlay avant capture // EDT
				laVue.overlay.add(laVue.leRoi.getRoi(10));
				//FIN A DEBEUGER SALIM
				laVue.win.getImagePlus().setOverlay(laVue.overlay);;
				capture[2]=Modele_Shunpo.captureImage(laVue.win.getImagePlus(),512,512);
				etat = etat.next() ;
				laVue.overlay.clear();
				laVue.win.getImagePlus().setRoi((Roi) laVue.leRoi.getRoi(10));
				laVue.win.showSlice(1);
				laVue.overlayDG();
				laVue.win.getImagePlus().setOverlay(laVue.overlay);
				laVue.setInstructions(listeInstructions[index]);
				break;
			case Fin:
				addRoi("Brain Ant");
				leModele.calculerCoups("CA",laVue.win.getImagePlus());
				laVue.overlay.add(laVue.leRoi.getRoi(12));
				capture[3]=Modele_Shunpo.captureImage(laVue.win.getImagePlus(),512,512);
				laVue.win.getImagePlus().deleteRoi();
				laVue.overlay.clear();
				String[] resultats = leModele.resultats();
				laVue.labelsResultats(resultats) ;
				//On passe les capture en stack
				ImageStack stackCapture=Modele_Shunpo.captureToStack(capture);
				//on fait le montage du stack et on g茅n猫re l'interface resultat
				laVue.UIResultats(leModele.montage(stackCapture, nomProgramme));
				laVue.win.close();
				laVue.leRoi.close();
			}
		}
		if (b == laVue.lesBoutons.get("Capture")){
				laVue.lesBoutons.get("Capture").setVisible(false);
				laVue.Csv.setText("Provided By Petctviewer.org");
				ImagePlus captureFinale =Modele_Shunpo.captureFenetre(WindowManager.getCurrentImage(),0,0);
				WindowManager.getCurrentWindow().getImagePlus().changes=false;
				WindowManager.getCurrentWindow().close();
				//On genere la 2eme partie des tag dicom et on l'ajoute 脿 la 1ere partie dans le property de l'image finale
				captureFinale.setProperty("Info", tagCapture+=(Modele_Shunpo.genererDicomTagsPartie2(captureFinale)));
				//On affiche et on agrandie la fenetre de la capture finale
				captureFinale.show();
				captureFinale.getCanvas().setScaleToFit(true);
				//On met un zoom a 80%
				captureFinale.getCanvas().setMagnification(0.8);
				//On sauve les resultats en CSV et ZIP
				try {
					String[] resultatscsv= leModele.buildCSVResultats();
					Modele_Shunpo.exportAll(resultatscsv,2,laVue.leRoi, nomProgramme,captureFinale);
					} catch (FileNotFoundException e) {}
				//On fait la capture finale
				captureFinale.getWindow().toFront();
				//On propose de sauver la capture en DICOM
				IJ.run("myDicom...");
				//fin du programme ici
					
		}

		if (b == laVue.lesBoutons.get("Precedent")) {
			switch(etat) {
			
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
				laVue.overlay.clear();
				laVue.overlayDG();
				break;
				
			case PoumonD_Ant:
				retour();
				laVue.win.showSlice(2);
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
				laVue.overlay.clear();
				laVue.overlayDG();
				break;
				
			case Cerveau_Post:
				break;
				
			case Cerveau_Ant:
				break;
				
			case Fin:
				retour();
				laVue.win.showSlice(1);
				break;
			
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
	
	private void genererBDF() {
		Roi[] rois = laVue.leRoi.getRoisAsArray() ;
		Rectangle r = new Rectangle(15, 30);
		int x = (int) ((rois[2].getBounds().getLocation().x + rois[3].getBounds().getLocation().x + rois[3].getBounds().getWidth() ) / 2) ;
		int y = (rois[2].getBounds().getLocation().y + rois[3].getBounds().getLocation().y ) / 2 ;
		r.setLocation(x, y);
		laVue.win.getImagePlus().setRoi(r);
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

	private void retour() {
		etat = etat.previous() ;
		index -- ;
		laVue.setInstructions(listeInstructions[index]);
		laVue.leRoi.select(index);	
	}
}
