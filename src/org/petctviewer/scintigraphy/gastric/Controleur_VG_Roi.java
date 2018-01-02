/**
Copyright (C) 2017 PING Xie and KANOUN Salim

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

package org.petctviewer.scintigraphy.gastric;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.text.ParseException;

import org.petctviewer.scintigraphy.shunpo.Modele_Shunpo;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.CanvasResizer;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

public class Controleur_VG_Roi implements ActionListener {

	private Vue_VG_Roi laVue;

	private Modele_VG_Roi.Etat etat;

	private Modele_VG_Roi leModele;

	private String[] listeInstructions = { "Delimit the stomac", "Delimit the intestine", "Adjuste the stomac",
			"Adjuste the intestine and to next image", "Next to display results", "Fin" };

	private int index_Roi;// index des Rois

	private int index_Instru;// index des instructions

	private int index_Image;// index des images

	private String acquisitionTime;

	private String[] resultats;

	private boolean estAntreCorrect;//signifie si'l y a intersection entre la ROI de  l'estomac et de l'intestin pour fabriquer la ROi de l'antre

	private String tagCaptureFinale;

	private ImagePlus[] capture = new ImagePlus[4];

	public Controleur_VG_Roi(Vue_VG_Roi vue, Modele_VG_Roi leModele) {
		index_Roi = 0;
		index_Instru = 0;
		laVue = vue;
		this.leModele = leModele;
		etat = Modele_VG_Roi.Etat.ESTOMAC_ANT;
		index_Image = 1;
		acquisitionTime = "";
		estAntreCorrect = true;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();
		if (b == laVue.lesBoutons.get("Suivant")) {
			switch (etat) {
			
			case ESTOMAC_ANT:	
				// si le ROI n'est pas present on demande a l'utilisateur de le faire
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please delimite the stomach!");
					//si le Roi exist deja on affiche le ROI existante pour que l'utilisateur puisse la modifier
					if(laVue.leRoi.getRoi(index_Roi)!=null){
						laVue.leRoi.deselect();
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					addRoi("Stomach_Ant" + index_Image);
				}

				leModele.calculerCoups("Estomac_Ant", index_Image, laVue.imp);
				laVue.imp.deleteRoi();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				etat = etat.next();
				laVue.lesBoutons.get("Precedent").setEnabled(true);
				leModele.initResultat(laVue.imp);
				
				String tagNom = DicomTools.getTag(laVue.imp, "0010,0010").substring(1);
				leModele.setPatient(tagNom, laVue.imp);
				//On genere la 1ere partie du Header qui servira a la capture finale
				tagCaptureFinale=Modele_Shunpo.genererDicomTagsPartie1(laVue.imp, laVue.nomProgramme);
				// on regarde si la ROI suivante est deja dans le ROI manager, si oui on l'affiche 
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.deselect();
					laVue.leRoi.select(index_Roi);
				}
				break;

			case INTESTIN_ANT:
				// si le ROI n'est pas delimite, demande a l'utilisateur de
				// delimiter le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please delimite the intestine!");
					//si le Roi exist deja on affiche le ROI existante pour que l'utilisateur puisse la modifier
					if(laVue.leRoi.getRoi(index_Roi)!=null){
						laVue.leRoi.deselect();
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					addRoi("Intestine_Ant" + index_Image);
				}
				//on fait le calcule
				leModele.calculerCoups("Intes_Ant", index_Image,laVue.imp);
				laVue.imp.deleteRoi();
				//On genere la roiAntre
				getAntreFundus("Ant");
				//si'l y a pas de intersection entre la ROI de  l'estomac et de l'intestin pour fabriquer la ROi de l'antre
				//on refait la ROI de l'intestin
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				etat = etat.next();
				laVue.windowstack.showSlice(2);
				//On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

			case ESTOMAC_POS:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Stomach_Pos", "Estomac_Pos");
				}
				
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				//On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}break;

				
			case INTESTIN_POS:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Pos", "Intes_Pos");
				}
				getAntreFundus("Pos");
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				//On appelle la methode de SunPo pour capture l'image (sans l'interface)
				ImagePlus captureTemp=Modele_Shunpo.captureImage(laVue.imp, 512, 512);
				//On met dans un canvas de meme taille que les courbes et on stock dans le tableau d'imagePlus capture
				ImageProcessor ip=captureTemp.getProcessor();
				CanvasResizer canvas = new CanvasResizer();
				ImageProcessor iptemp=canvas.expandImage(ip, 640, 512, (640-512)/2, 0);
				captureTemp.setProcessor(iptemp);
				capture[0]=captureTemp;
				acquisitionTime = DicomTools.getTag(laVue.windowstack.getImagePlus(), "0008,0032");
				try {
					leModele.tempsImage(index_Image, acquisitionTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				leModele.pourcVGImage(index_Image);
				etat = etat.next();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.windowstack.showSlice(index_Image * 2 + 1);
				///On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				index_Image++;
				break;

			
			case CIR_ESTOMAC_ANT:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Stomach_Ant", "Estomac_Ant");
				}
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				//On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

			
			case CIR_INTESTIN_ANT:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Ant", "Intes_Ant");
				}
				getAntreFundus("Ant");
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				etat = etat.next();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.windowstack.showSlice((index_Image  * 2 ));
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

				
			case CIR_ESTOMAC_POS:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Stomach_Pos", "Estomac_Pos");
				}
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi)!=null){
					laVue.leRoi.select(index_Roi);
				}
				else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

				
			case CIR_INTESTIN_POS:
				// si le ROI n'est pas Clique, demande a l'utilisateur de
				// cliquer et adjust le ROI
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it! ");
					laVue.leRoi.deselect();
					//si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante pour qu'on puisse le modifier
					if(laVue.leRoi.getRoi(index_Roi)==null){
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					else{
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Pos", "Intes_Pos");
				}
				getAntreFundus("Pos");
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				acquisitionTime = DicomTools.getTag(laVue.imp, "0008,0032");
				try {
					leModele.tempsImage(index_Image, acquisitionTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				leModele.pourcVGImage(index_Image);
				if (index_Image == laVue.imp.getStackSize()/2) {
					etat = etat.next();
					index_Instru++;

				} else {
					// si'l est pas la derniere image, on va etre dans la boucle "CIR_ESTOMAC_ANT---CIR_INTESTIN_ANT---CIR_ESTOMAC_POS---CIR_INTESTIN_POS"
					for (int i = 0; i < 3; i++) {
						etat = etat.previous();

					}
					index_Instru--;
					laVue.windowstack.showSlice((index_Image) * 2 + 1);
					laVue.leRoi.deselect();
					if (laVue.leRoi.getRoi(index_Roi)!=null){
						laVue.leRoi.select(index_Roi);
					}
					else {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
					index_Image++;
				}
				laVue.setInstructions(listeInstructions[index_Instru]);
				break;

			
			case FIN:
				// on obtient les resultats et cree ue tableau et un panel et
				// des graphiques pour afficher les resultats
				resultats = leModele.resultats(laVue.imp);
				if (Modele_VG_Roi.logOn) {
					for(int i=0; i<resultats.length;i++){
						IJ.log(resultats[i]);
					}
				}
				laVue.tablesResultats(resultats);
				laVue.infoResultats(resultats);
				capture[1]=leModele.createCourbeTrois("Retention (% meal)", Modele_VG_Roi.temps,
						Modele_VG_Roi.estomacPourcent, Color.RED, "Stomach", Modele_VG_Roi.fundusPourcent, new Color(0,100,0),
						"Fundus", Modele_VG_Roi.antrePourcent, Color.BLUE, "Antrum");
				capture[2]=leModele.createCourbeUn("Fundus/Stomach (%)", new Color(0,100,0), "Intragastric Distribution",
						Modele_VG_Roi.temps, Modele_VG_Roi.funDevEsto, 100.0);
				capture[3]=leModele.createCourbeUn("% meal in the interval", Color.RED, "Gastrointestinal flow",
						Modele_VG_Roi.tempsInter, Modele_VG_Roi.estoInter, 50.0);
				//On cree le stack a partir du tableau d'ImagePlus en utilisant la methode de Modele_Shunpo
				ImageStack stackCapture=Modele_Shunpo.captureToStack(capture);
				laVue.UIResultats(leModele.montage(stackCapture,laVue.nomProgramme));
				laVue.lesBoutons.get("Suivant").setEnabled(false);
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				break;

			default:
				break;
			}
		}
		
		if(b==laVue.lesBoutons.get("Precedent")) {
			switch (etat) {
			
		case INTESTIN_ANT:
			retour();
			laVue.lesBoutons.get("Precedent").setEnabled(false);
			break;

		
		case ESTOMAC_POS:
			retour();
			laVue.windowstack.showSlice(1);
			break;

		case INTESTIN_POS:
			retour();
			break;

		case CIR_ESTOMAC_ANT:
			
			index_Instru++;
			index_Instru++;
			retour();
			index_Image--;
			// si'l est pas la premiere image, on va etre dans la boucle "CIR_INTESTIN_POS---CIR_ESTOMAC_POS---CIR_INTESTIN_ANT---CIR_ESTOMAC_ANT"
			if (index_Image != 1) {
				etat = etat.next();
				etat = etat.next();
				etat = etat.next();
				etat = etat.next();
			}
			laVue.windowstack.showSlice(index_Image  * 2 );
			break;

		
		case CIR_INTESTIN_ANT:
			retour();
			break;

		
		case CIR_ESTOMAC_POS:
			index_Instru++;
			index_Instru++;
			retour();
			laVue.windowstack.showSlice((index_Image - 1) * 2 + 1);
			break;

		case CIR_INTESTIN_POS:
			retour();
			break;

		
		case FIN:
			retour();
			break;

		
		case RESULTAT:
			laVue.lesBoutons.get("Suivant").setEnabled(true);
			IJ.setTool(Toolbar.POLYGON);
			etat = etat.previous();
			index_Instru--;
			laVue.setInstructions(listeInstructions[index_Instru]);
			ActionEvent e = new ActionEvent(laVue.lesBoutons.get("Precedent"),ActionEvent.ACTION_PERFORMED,"Precedent");
			this.actionPerformed(e);
			break;
		
		default:
			break;
		}
	}

	if(b==laVue.lesBoutons.get("Sauvegarder")) {
		laVue.csv.setText("Provided By Petctviewer.org");
		// mettre les buttons "save result" et "return to adjust Rois" invisible
		laVue.setNonVisibleButtons();
		// on obtient la partie "MG %" des resultats pour exporter au CSV
		String[] resultatsExporte = new String[(laVue.imp.getStackSize() / 2 + 2) * 4];
		for (int i = 0; i < resultatsExporte.length; i++) {
			resultatsExporte[i] = resultats[i];
		}
		// exporter les resultats en une image
		ImagePlus ze = Modele_Shunpo.captureFenetre(laVue.res.getImagePlus(), 0, 0);
		laVue.leRoi.close();
		//Evite le promt voulez vous sauver les modif en cas d edition de l image
		laVue.windowstack.close();
		//On genere la fin du Header DICOM de la capture finale via la methode disponible dans Shunpo
		tagCaptureFinale += Modele_Shunpo.genererDicomTagsPartie2(ze);
		// On applique le Header a l'ImagePlus
		ze.setProperty("Info", tagCaptureFinale);
		// On affiche et on redimensionne
		ze.show();
		ze.getWindow().setSize(ze.getWidth() + 15, ze.getHeight() + 50);
		ze.getCanvas().setScaleToFit(true);
		// exporter les resultats en CSV et du Roi Manager via la methode de Shunpo
		try {
			Modele_Shunpo.exportAll(resultats, 4, laVue.leRoi, laVue.nomProgramme, ze);

		} catch (FileNotFoundException e) {
		}
		laVue.res.getImagePlus().changes=false;
		laVue.res.close();

		// On lance l'utilitaire d'export en Dicom d'Ilan
		ImageWindow win = ze.getWindow();
		ze.killRoi();
		win.toFront();
		IJ.runMacro("run(\"myDicom...\");");

	}
	
	if(b==laVue.lesBoutons.get("Return")) {
		laVue.res.close();
	}

	if(b==laVue.lesBoutons.get("Draw ROI")) {
		laVue.lesBoutons.get("Draw ROI").setBackground(Color.LIGHT_GRAY);
		laVue.lesBoutons.get("Contrast").setBackground(null);
		IJ.setTool(Toolbar.POLYGON);
	}
	
	if(b==laVue.lesBoutons.get("Contrast")) {
		laVue.lesBoutons.get("Contrast").setBackground(Color.LIGHT_GRAY);
		laVue.lesBoutons.get("Draw ROI").setBackground(null);
		IJ.run("Window Level Tool");
	}
	
	if(b==laVue.lesBoutons.get("Quitter")) {
		laVue.end(null);
		return;
	}
	
	if(b==laVue.lesBoutons.get("Show")) {
		Modele_VG_Roi.logOn = !Modele_VG_Roi.logOn;
		if (!Modele_VG_Roi.logOn) {
			laVue.lesBoutons.get("Show").setLabel("Show MG%");
			laVue.lesBoutons.get("Show").setBackground(null);
		} 
		else {
			if (index_Image > 1) {
				for (int i = 1; i < index_Image; i++) {
					IJ.log("image " + (i) + ": " + " Stomach " + Modele_VG_Roi.estomacPourcent[i] + " Intestine "
							+ Modele_VG_Roi.intestinPourcent[i] + " Fundus " + Modele_VG_Roi.fundusPourcent[i]
							+ " Antre " + Modele_VG_Roi.antrePourcent[i]);
				}
			}
			laVue.lesBoutons.get("Show").setLabel("Close MG%");
			laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			
			
		}
	}

	}

	private void addRoi(String nom) {
		//si la ROI exist deja, on le update, sinon on ajoute une nouvelle ROI a ROI manager
		if(laVue.leRoi.getRoi(index_Roi)!=null){
			laVue.leRoi.runCommand("Update");
			laVue.leRoi.runCommand("Remove Slice Info");
		}
		else {
		laVue.leRoi.add(laVue.imp, laVue.imp.getRoi(), index_Roi);
		laVue.leRoi.deselect();
		laVue.leRoi.select(index_Roi);
		laVue.leRoi.rename(index_Roi, nom);
		laVue.leRoi.deselect();
		}
		laVue.overlay.clear();
		laVue.addOverlayGD();
	
		if (nom.contains("Stomach")){
			laVue.overlay.add(laVue.leRoi.getRoi(index_Roi));
		}
		if (nom.contains("Intestine")){
			//TESTSK//laVue.overlay.add(laVue.leRoi.getRoi(index_Roi-1));
		}

		index_Roi++;
	}

	// permet de revenir en arriere
	private void retour() {
			index_Roi--;
			laVue.leRoi.deselect();
			laVue.leRoi.select(index_Roi);
			//On clear l'Overlay
			laVue.overlay.clear();
			laVue.addOverlayGD();
			//On affiche l'overlay de la ROI n-1 si intestin ou n+1 si estomac
			if (laVue.leRoi.getRoi(index_Roi).getName().contains("Intestine")){
				laVue.overlay.add(laVue.leRoi.getRoi(index_Roi-1));
			}
			if (laVue.leRoi.getRoi(index_Roi).getName().contains("Stomach")){
				laVue.overlay.add(laVue.leRoi.getRoi(index_Roi+1));
			}
			
			index_Instru--;
			laVue.setInstructions(listeInstructions[index_Instru]);
			etat = etat.previous();
			}

	// permet de faire les instructions "AND" sur les ROIs
	// "Estomache" et "Intestine"
	// pour obtenir le ROI "Antre" et calculer ses coups
	private void getAntreFundus(String cote) {
		
		laVue.leRoi.setSelectedIndexes(new int[] { index_Roi - 1, index_Roi - 2 });
		laVue.leRoi.runCommand("AND");
		laVue.leRoi.runCommand("Deselect");
		// si il n'y a pas de intersection, redemande a l'utilisateur de adjuster le ROI
		if (WindowManager.getCurrentImage().getRoi()== null) {
			//si la ROI de l'estomac de image suivante existe pas, on delete la ROI de l'intestin de l'image courant, et on remet une ROI precedente pour modifier 
			if(laVue.leRoi.getRoi(index_Roi)==null){
				laVue.leRoi.deselect();
				laVue.leRoi.select(index_Roi-1);
				laVue.leRoi.runCommand("Delete");
				//TESTSK//laVue.overlay.add(laVue.leRoi.getRoi(index_Roi-2));
				//Si on n'est pas dans la 1ere image on remet la ROI intestin precedente  
				if (index_Roi > 2) {
					laVue.leRoi.deselect();
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 3).clone());
				}
			}else{
				laVue.leRoi.deselect();
				laVue.leRoi.select(index_Roi-1);
			}
			index_Roi--;
			IJ.showMessage(
					"please adjust the intestine So that there is an intersection between the estomac and the intestine !");
			estAntreCorrect = false;
		} else {
			leModele.calculerCoups("Antre_" + cote, index_Image,laVue.imp);
			laVue.windowstack.getImagePlus().deleteRoi();
			leModele.setCoups("Fundus_" + cote, index_Image,
					leModele.getCoups("Estomac_" + cote, index_Image)
							- leModele.getCoups("Antre_" + cote, index_Image));
			leModele.setCoups("Intestin_" + cote, index_Image,
					leModele.getCoups("Intes_" + cote, index_Image)
							- leModele.getCoups("Antre_" + cote, index_Image));
		}

	}

	// permet de modifier la ROI
	private void modifierRoi(String roiNouvNom, String coupNouveNom) {
		addRoi(roiNouvNom + index_Image);
		leModele.calculerCoups(coupNouveNom, index_Image, laVue.imp);
		laVue.imp.deleteRoi();
	}

}
