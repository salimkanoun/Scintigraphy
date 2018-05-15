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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.util.DicomTools;

public class Controleur_VG_Dynamique implements ActionListener {

	private Vue_VG_Dynamique laVue;

	private Modele_VG_Dynamique.Etat etat;

	private Modele_VG_Dynamique leModele;

	private String[] listeInstructions = { "Delimit the stomac", "Delimit the intestine", "Adjuste the stomac",
			"Adjuste the intestine and to next image", "Next to correct the result by background noise",
			"Next to set default ROIs ", "Duplicate the ROI adjuste it to the area of egg",
			"Next to correct the result by ingested eggs ", "Fin" };

	private int index_Roi;// index de la Roi

	private int index_Instru;// index de l'instruction

	protected ImagePlus ze;

	private int index_Ingestion;// index de l'ingestion (index de la serie)

	protected static int oeufsIngere;// le nombre de oeufs ingeres

	private int index_Oeuf;// index de l'oeuf

	private boolean estAntreCorrect;// signifie si'l y a intersection entre la ROI de l'estomac et de l'intestin
									// pour fabriquer la ROi de l'antre

	private int reponseFinBDFAntre;// la reponse de l'utilisateur quand il est demande si sur la serie courant la
									// region de l'antre est bruit de fond

	private int reponseFinBDFIntestin;// la reponse de l'utilisateur quand il est demande si sur la serie courant la
										// region de l'intestin est bruit de fond

	public Controleur_VG_Dynamique(Vue_VG_Dynamique vue, Modele_VG_Dynamique leModele) {
		this.index_Roi = 0;
		this.index_Instru = 0;
		this.index_Oeuf = 1;
		this.laVue = vue;
		this.leModele = leModele;
		this.etat = Modele_VG_Dynamique.Etat.ESTOMAC_ANT;
		this.estAntreCorrect = true;
		this.reponseFinBDFAntre = 1;
		this.reponseFinBDFIntestin = 1;
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
					// si le Roi exist deja on affiche le ROI existante pour que l'utilisateur
					// puisse la modifier
					if (laVue.leRoi.getRoi(index_Roi) != null) {
						laVue.leRoi.deselect();
						laVue.leRoi.select(index_Roi);
					}

					break;
				} else {
					leModele.initModele(laVue.imp);
					oeufsIngere = laVue.imp.getStackSize() / 2;
					index_Ingestion = laVue.imp.getStackSize() / 2;
					addRoi("Stomach_Ant" + index_Ingestion);
				}
				leModele.calculerCoupsBrut(index_Ingestion, 0, 3, laVue.imp);
				laVue.imp.killRoi();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				etat = etat.next();
				laVue.lesBoutons.get("Precedent").setEnabled(true);
				leModele.setAcquisitionTime(index_Ingestion, DicomTools.getTag(laVue.imp, "0008,0032"));
				leModele.setNomSerie(index_Ingestion, DicomTools.getTag(laVue.imp, "0008,103E"));
				// on regarde si la ROI suivante est deja dans le ROI manager, si oui on
				// l'affiche
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.deselect();
					laVue.leRoi.select(index_Roi);
				}
				break;

			case INTESTIN_ANT:
				// si le ROI n'est pas present on demande a l'utilisateur de le faire
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage("please delimite the intestine!");
					// si le Roi exist deja on affiche le ROI existante pour que l'utilisateur
					// puisse la modifier
					if (laVue.leRoi.getRoi(index_Roi) != null) {
						laVue.leRoi.deselect();
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					addRoi("Intestine_Ant" + index_Ingestion);
				}
				// On fait les calculs
				leModele.calculerCoupsBrut(index_Ingestion, 0, 2, laVue.imp);
				laVue.imp.killRoi();
				// On genere la roiAntre
				getAntreFundus("Ant");
				// Verification d'une intrestection entre les 2 ROIs
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				etat = etat.next();
				demandeFinBDFAntre();
				demandeFinBDFIntestin();
				// On passe a l'image post
				laVue.windowstack.showSlice(2);
				laVue.imp.setSlice(2);
				// On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

			case ESTOMAC_POS:
				// On verifie que la ROI a ete dessinee
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}

					break;
				} else {
					modifierRoi("Stomach_Pos", index_Ingestion, 1, 3);
				}
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				// On regarde si la ROI suivante est presente et sinon on met la ROI n-2
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;
			case INTESTIN_POS:
				// On verifie que la ROI est presente
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Pos", index_Ingestion, 1, 2);
				}
				// On genere l'antre
				getAntreFundus("Pos");
				// On verifie qu'il existe une intersection
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				etat = etat.next();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				index_Ingestion--;
				// On change de coupe
				laVue.windowstack.showSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);
				laVue.imp.setSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;

			case CIR_ESTOMAC_ANT:
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {

					modifierRoi("Stomach_Ant", index_Ingestion, 0, 3);
				}
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				leModele.setAcquisitionTime(index_Ingestion, DicomTools.getTag(laVue.imp, "0008,0032"));
				leModele.setNomSerie(index_Ingestion, DicomTools.getTag(laVue.imp, "0008,103E"));
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;
			case CIR_INTESTIN_ANT:
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Ant", index_Ingestion, 0, 2);
				}
				getAntreFundus("Ant");
				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				etat = etat.next();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				demandeFinBDFAntre();
				demandeFinBDFIntestin();
				laVue.windowstack.showSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 2);
				laVue.imp.setSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 2);
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;
			case CIR_ESTOMAC_POS:
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it!");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Stomach_Pos", index_Ingestion, 1, 3);
				}
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.leRoi.deselect();
				if (laVue.leRoi.getRoi(index_Roi) != null) {
					laVue.leRoi.select(index_Roi);
				} else {
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
				}
				break;
			case CIR_INTESTIN_POS:
				if (laVue.imp.getRoi() == null) {
					IJ.showMessage(
							"please click on roi " + laVue.leRoi.getRoi(index_Roi - 2).getName() + " and adjust it! ");
					laVue.leRoi.deselect();
					// si la Roi exite pas, on met la Roi precedente, sinon on met la Roi existante
					// pour qu'on puisse le modifier
					if (laVue.leRoi.getRoi(index_Roi) == null) {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					} else {
						laVue.leRoi.select(index_Roi);
					}
					break;
				} else {
					modifierRoi("Intestine_Pos", index_Ingestion, 1, 2);
				}
				getAntreFundus("Pos");

				if (!estAntreCorrect) {
					estAntreCorrect = true;
					break;
				}
				// Si c'est la premiere image
				if (index_Ingestion == 1) {
					etat = etat.next();
					index_Instru++;
					leModele.calculerTempsImages();
				} else {
					// si'l est pas la derniere image, on va etre dans la boucle
					// "CIR_ESTOMAC_ANT---CIR_INTESTIN_ANT---CIR_ESTOMAC_POS---CIR_INTESTIN_POS"
					for (int i = 0; i < 3; i++) {
						etat = etat.previous();
					}
					index_Instru--;
					index_Ingestion--;
					laVue.windowstack.showSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);
					laVue.imp.setSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);

					laVue.leRoi.deselect();
					if (laVue.leRoi.getRoi(index_Roi) != null) {
						laVue.leRoi.select(index_Roi);
					} else {
						laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 2).clone());
					}
				}
				laVue.setInstructions(listeInstructions[index_Instru]);

				break;

			case OEUFOUVERT:
				laVue.lesBoutons.get("Suivant").setEnabled(false);
				// on calcule les resultats corriges par BF
				leModele.corrigerParBDF();
				// on demande l'utilisateur de preciser si'l veut ouvrir l'image des oeufs
				int reponse = JOptionPane.showConfirmDialog(laVue.windowstack,
						"Would you like to open the image of eggs ?", "", JOptionPane.YES_NO_OPTION);
				// tant que on repond pas
				while (reponse != 0 && reponse != 1) {
					// par exemple si on ferme la dialogue
					reponse = JOptionPane.showConfirmDialog(laVue.windowstack,
							"Would you like to open the image of eggs ?", "", JOptionPane.YES_NO_OPTION);
				}
				if (reponse == 0) {
					// si on reponds "YES", on met l'image des oeufs au fenetre principal pour
					// compter les oeufs
					laVue.ouvrirImage("Oeufs");
					laVue.overlay.clear();
					VueScin.setOverlayDG(laVue.overlay, laVue.imp);
					laVue.lesBoutons.get("Precedent").setEnabled(false);
					etat = etat.next();
					index_Instru++;
					laVue.setInstructions(listeInstructions[index_Instru]);
				} else if (reponse == 1) {
					// si on reponds "No", on passe a l'etat CORRIGER, et on tenir compte que tous
					// les oeufs ont la m��me taille pour corriger les resultats
					etat = etat.next();
					index_Instru++;
					etat = etat.next();
					index_Instru++;
					etat = etat.next();
					index_Instru++;
					laVue.lesBoutons.get("Precedent").setEnabled(false);
					laVue.lesBoutons.get("Suivant").setEnabled(true);
					laVue.setInstructions(listeInstructions[index_Instru]);
					// On envoi le message "next"
					ActionEvent e = new ActionEvent(laVue.lesBoutons.get("Suivant"), ActionEvent.ACTION_PERFORMED,
							"Suivant");
					this.actionPerformed(e);
				}
				break;

			case ROIDEFAULT:
				// on met un ROI par default en gauche-haut de la fenetre principal
				Rectangle r = new Rectangle(15, 15);
				r.setLocation(10, 10);
				laVue.leRoi.deselect();
				laVue.imp.setRoi(r);
				addRoi("oeuf");
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				break;

			case OEUFS:
				// si aucunne ROI est selecte, on remet la ROI precedente pour qu'on puisse le
				// modifie
				if (laVue.imp.getRoi() == null) {
					Roi roiTemp = (Roi) laVue.leRoi.getRoi(index_Roi - 1).clone();
					roiTemp.setLocation(10, 10);
					laVue.leRoi.deselect();
					laVue.imp.setRoi(roiTemp);
					break;
				}
				modifierRoiOeuf();

				// si le nombre de coups de tous les oeufs sont calcule
				if (index_Oeuf > oeufsIngere) {
					// on passe a l'etat suivant
					etat = etat.next();
					index_Instru++;
					laVue.setInstructions(listeInstructions[index_Instru]);
					// on calcule le % de chaque oeuf par rapport au repas total
					leModele.calculerOeufsPourcent();
					// on demande l'utilisateur de verifier le % de chaque oeuf
					laVue.confirmerOeufPourc();

				}
				break;

			case CORRIGER:
				// corriger les resultats par les ouefs non ingeres
				leModele.corrigerParOeuf();
				String resultat = leModele.getResultat();
				etat = etat.next();
				index_Instru++;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.lesBoutons.get("Suivant").setEnabled(false);
				ModeleScin.exportRoiManager(laVue.leRoi, laVue.nomProgramme, laVue.windowstack.getImagePlus());
				laVue.windowstack.close();
				laVue.leRoi.close();
				IJ.runMacro("run(\"Gastric Emptying software\"," + "\"" + resultat + "\"" + ");");
				break;
			default:
				break;
			}
		}
		// ESTOMAC_ANT, INTESTIN_ANT, ESTOMAC_POS, INTESTIN_POS, CIR_ESTOMAC_ANT,
		// CIR_INTESTIN_ANT, CIR_ESTOMAC_POS, CIR_INTESTIN_POS, OEUFOUVERT, ROIDEFAULT,
		// OEUFES, CORRIGER,FIN;

		if (b == laVue.lesBoutons.get("Precedent")) {
			switch (etat) {

			case INTESTIN_ANT:
				retour();
				laVue.lesBoutons.get("Precedent").setEnabled(false);
				break;

			case ESTOMAC_POS:
				retour();
				laVue.windowstack.showSlice(1);
				laVue.imp.setSlice(1);
				// si la reponse etait "YES" a ce etat, on remet la reponse a "no", pour qu'on
				// puisse redemander l'utilisateur
				if (leModele.getFinBDFAntre() == index_Ingestion) {
					reponseFinBDFAntre = 1;
				}
				if (leModele.getFinBDFIntestin() == index_Ingestion) {
					reponseFinBDFIntestin = 1;
				}
				break;

			case INTESTIN_POS:
				retour();
				break;

			case CIR_ESTOMAC_ANT:
				retour();
				index_Instru++;
				index_Instru++;
				index_Ingestion++;
				// si'l est pas la premiere image, on va etre dans la boucle
				// "CIR_INTESTIN_POS---CIR_ESTOMAC_POS---CIR_INTESTIN_ANT---CIR_ESTOMAC_ANT"
				if (index_Ingestion != laVue.imp.getStackSize() / 2) {
					etat = etat.next();
					etat = etat.next();
					etat = etat.next();
					etat = etat.next();
				}
				laVue.windowstack.showSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 2);
				laVue.imp.setSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 2);
				break;

			case CIR_INTESTIN_ANT:
				retour();
				break;

			case CIR_ESTOMAC_POS:
				retour();
				index_Instru++;
				index_Instru++;
				laVue.windowstack.showSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);
				laVue.imp.setSlice((laVue.imp.getStackSize() / 2 - index_Ingestion) * 2 + 1);
				if (leModele.getFinBDFAntre() == index_Ingestion) {
					reponseFinBDFAntre = 1;
				}
				if (leModele.getFinBDFIntestin() == index_Ingestion) {
					reponseFinBDFIntestin = 1;
				}
				break;

			case CIR_INTESTIN_POS:
				retour();
				break;

			case OEUFOUVERT:
				retour();
				laVue.leRoi.deselect();
				laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi).clone());
				laVue.lesBoutons.get("Suivant").setEnabled(true);
				break;

			case ROIDEFAULT:
				retour();
				break;

			case OEUFS:
				retourOeuf();
				break;

			case CORRIGER:
				etat = etat.previous();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				retourOeuf();
				break;

			case FIN:
				etat = etat.previous();
				index_Instru--;
				laVue.setInstructions(listeInstructions[index_Instru]);
				laVue.lesBoutons.get("Suivant").setEnabled(true);
				break;
			default:
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
			laVue.end();
			return;
		}

		if (b == laVue.lesBoutons.get("Show")) {
			Modele_VG_Dynamique.logOn = !Modele_VG_Dynamique.logOn;
			if (!Modele_VG_Dynamique.logOn) {
				laVue.lesBoutons.get("Show").setLabel("Show MG%");
				laVue.lesBoutons.get("Show").setBackground(null);
			} else {
				laVue.lesBoutons.get("Show").setLabel("Close MG%");
				laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}
		}

	}

	private void addRoi(String nom) {
		// si la ROI exist deja, on le update, sinon on ajoute une nouvelle ROI a ROI
		// manager
		if (laVue.leRoi.getRoi(index_Roi) != null) {
			laVue.leRoi.runCommand("Update");
			laVue.leRoi.runCommand("Remove Slice Info");
		} else {
			laVue.leRoi.add(laVue.imp, laVue.imp.getRoi(), index_Roi);
			laVue.leRoi.runCommand("Remove Slice Info");
			laVue.leRoi.deselect();
			laVue.leRoi.select(index_Roi);
			laVue.leRoi.rename(index_Roi, nom);
		}

		// en cas de ingestion, on update l'Overlay toujours, mais en cas de oeufs, on
		// update l'Overlay juste la premire oeuf
		if (index_Oeuf == 1) {
			// on update l'Overlay
			laVue.overlay.clear();
			VueScin.setOverlayDG(laVue.overlay, laVue.imp);
		}

		if (nom.contains("Stomach")) {
			laVue.overlay.add(laVue.leRoi.getRoi(index_Roi));
		}
		if (nom.contains("Intestine")) {
			// TESTSK//laVue.overlay.add(laVue.leRoi.getRoi(index_Roi-1));
		}

		index_Roi++;
	}

	// permet de revenir en arriere, le parametre i signifie le nombre de ROIs a
	// revenir en arriere
	private void retour() {
		laVue.imp.killRoi();
		index_Roi--;
		laVue.leRoi.deselect();
		laVue.leRoi.select(index_Roi);
		// On clear l'Overlay
		laVue.overlay.clear();
		VueScin.setOverlayDG(laVue.overlay, laVue.imp);
		// On affiche l'overlay de la ROI n-1 si intestin ou n+1 si estomac
		if (laVue.leRoi.getRoi(index_Roi).getName().contains("Intestine")) {
			laVue.overlay.add(laVue.leRoi.getRoi(index_Roi - 1));
		}
		if (laVue.leRoi.getRoi(index_Roi).getName().contains("Stomach")) {
			laVue.overlay.add(laVue.leRoi.getRoi(index_Roi + 1));
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
		if (WindowManager.getCurrentImage().getRoi() == null) {
			// si la ROI de l'estomac de image suivante existe pas, on delete la ROI de
			// l'intestin de l'image courant, et on remet une ROI precedente pour modifier
			if (laVue.leRoi.getRoi(index_Roi) == null) {
				laVue.leRoi.deselect();
				laVue.leRoi.select(index_Roi - 1);
				laVue.leRoi.runCommand("Delete");
				// TESTSK//laVue.overlay.add(laVue.leRoi.getRoi(index_Roi-2));
				// Si on n'est pas dans la 1ere image on remet la ROI intestin precedente
				if (index_Roi > 2) {
					laVue.leRoi.deselect();
					laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 3).clone());
				}
			} else {
				laVue.leRoi.deselect();
				laVue.leRoi.select(index_Roi - 1);
			}
			index_Roi--;
			IJ.showMessage(
					"please adjust the intestine So that there is an intersection between the estomac and the intestine !");
			estAntreCorrect = false;
		} else {

			// pour la deuxieme dimension de tableau coupsBrut, j est 0: image ANT, j est 1:
			// image POS
			int j = 1;
			if (cote == "Ant") {
				j = 0;
			}

			leModele.calculerCoupsBrut(index_Ingestion, j, 1, laVue.imp);
			laVue.imp.killRoi();
			leModele.setCoupsBrut(index_Ingestion, j, 0,
					leModele.getCoupsBrut(index_Ingestion, j, 3) - leModele.getCoupsBrut(index_Ingestion, j, 1));
			leModele.setCoupsBrut(index_Ingestion, j, 2,
					leModele.getCoupsBrut(index_Ingestion, j, 2) - leModele.getCoupsBrut(index_Ingestion, j, 1));
			leModele.setCoupsBrut(index_Ingestion, j, 4,
					leModele.getCoupsBrut(index_Ingestion, j, 2) + leModele.getCoupsBrut(index_Ingestion, j, 3));

		}

	}

	// permet de modifier le Roi
	private void modifierRoi(String roiNouvNom, int i, int j, int k) {
		addRoi(roiNouvNom + index_Ingestion);
		leModele.calculerCoupsBrut(i, j, k, laVue.imp);
		laVue.imp.killRoi();
	}

	// permet de modifier le Roi d'oeuf
	private void modifierRoiOeuf() {
		addRoi("" + index_Oeuf);
		leModele.calculerCoupsOeufs(index_Oeuf, laVue.imp);
		if (index_Oeuf == 1) {
			laVue.lesBoutons.get("Precedent").setEnabled(true);
		}
		index_Oeuf++;
		// Voir probleme d'overlay eviter de la vider dans add ROI pour les oeufs...
		laVue.overlay.add(laVue.leRoi.getRoi(index_Roi - 1));
		laVue.leRoi.deselect();
		laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 1).clone());
		laVue.imp.getRoi().setLocation(10, 10);
	}

	// permet de revenir en arriere pour modifier le Roi d'oeuf
	private void retourOeuf() {
		index_Oeuf--;
		index_Roi--;
		// on supprime l'overlay et la ROI qu'on a ajoute a ce etat
		laVue.overlay.remove(laVue.leRoi.getRoi(index_Roi));
		laVue.leRoi.deselect();
		laVue.leRoi.select(index_Roi);
		laVue.leRoi.runCommand("Delete");
		if (index_Oeuf == 1) {
			laVue.lesBoutons.get("Precedent").setEnabled(false);
			etat = etat.previous();
			index_Instru--;
			laVue.setInstructions(listeInstructions[index_Instru]);
		} else {
			// Voir probleme d'overlay eviter de la vider dans add ROI pour les oeufs...
			laVue.leRoi.deselect();
			laVue.imp.setRoi((Roi) laVue.leRoi.getRoi(index_Roi - 1).clone());
			laVue.imp.getRoi().setLocation(10, 10);
		}
	}

	// permet de demander a l'utilisateur de preciser sur la serie courant si la
	// region de l'antre est bruit de fond
	private void demandeFinBDFAntre() {
		if (reponseFinBDFAntre == 1) {
			reponseFinBDFAntre = JOptionPane.showConfirmDialog(laVue.windowstack, "Antrum region is background noise ?",
					"", JOptionPane.YES_NO_OPTION);
			if (reponseFinBDFAntre == 0) {
				leModele.setFinBDFAntre(index_Ingestion);
			}

		}
	}

	// permet de demander a l'utilisateur de preciser sur la serie courant si la
	// region de l'intestin est bruit de fond
	private void demandeFinBDFIntestin() {
		if (reponseFinBDFIntestin == 1) {
			reponseFinBDFIntestin = JOptionPane.showConfirmDialog(laVue.windowstack,
					"Intestine region is background noise ?", "", JOptionPane.YES_NO_OPTION);
			if (reponseFinBDFIntestin == 0) {
				leModele.setFinBDFIntestin(index_Ingestion);
			}

		}
	}
}
