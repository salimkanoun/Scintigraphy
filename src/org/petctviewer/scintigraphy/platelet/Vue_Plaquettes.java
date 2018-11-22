/**
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.StackReverser;

public class Vue_Plaquettes extends Scintigraphy {
	
	// Si acquisition antPost
	protected Boolean antPost = false;
	private Date dateHeureDebut;

	// Nombre de series disponibles a l'ouverture
	protected int nombreAcquisitions;
	
	public Vue_Plaquettes() {
		super("Platelet");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {

		ArrayList<ImagePlus> series = new ArrayList<>();

		for (int i = 0; i < images.length; i++) {

			ImagePlus imp = images[i];
			if (imp.getStackSize() == 2) {
				this.antPost = true;
				Boolean ant = Library_Dicom.isAnterieur(imp);
				// Si l'image 1 est anterieur on inverse le stack pour avoir d'abord l'image
				// post
				if (ant != null && ant) {
					StackReverser reverser = new StackReverser();
					reverser.flipStack(imp);
				}
			}
			// Si uniquement une image on verifie qu'elle est post et on la flip
			else if (imp.getStackSize() == 1) {
				// SK Pas propre necessite de mieux orienter les Image pour Ant/Post
				imp.getProcessor().flipHorizontal();
			}
			series.add(Library_Dicom.sortImageAntPost(imp));
			imp.close();
		}
		this.nombreAcquisitions = series.size();
		// IJ.log(String.valueOf(antPost));

		ImagePlus[] seriesTriee = Library_Dicom.orderImagesByAcquisitionTime(series);

		// On recupere la date et le jour de la 1ere image
		this.dateHeureDebut=Library_Dicom.getDateAcquisition(seriesTriee[0]);

		Concatenator enchainer = new Concatenator();
		// enchaine les images
		ImagePlus imp = enchainer.concatenate(seriesTriee, false);
		imp.show();

		HyperStackConverter convert = new HyperStackConverter();
		convert.run("hstostack");
		
		return imp.duplicate();
	}

	@Override
	public void lancerProgramme() {
		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		
		Overlay overlay = Library_Gui.initOverlay(this.getImp());
		Library_Gui.setOverlayDG(overlay, getImp(), Color.YELLOW);
		this.getImp().setOverlay(overlay);
		
		Controleur_Plaquettes ctrl = new Controleur_Plaquettes(this);
		this.getFenApplication().setControleur(ctrl);
		this.getFenApplication().getImagePlus().getCanvas().setScaleToFit(true);
		this.getFenApplication().getImagePlus().getCanvas().setSize(512,512);
		this.getFenApplication().pack();
		this.getFenApplication().setSize(this.getFenApplication().getPreferredSize());
		
		Modele_Plaquettes leModele = new Modele_Plaquettes(this.getDateDebut());
		this.setModele(leModele);
	}
	
	public Date getDateDebut() {
		return this.dateHeureDebut;
	}

} // fin
