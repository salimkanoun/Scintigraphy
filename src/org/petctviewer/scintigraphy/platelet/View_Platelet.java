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

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.StackReverser;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

public class View_Platelet extends Scintigraphy {

	// Si acquisition antPost
	protected Boolean antPost = false;
	private Date dateHeureDebut;

	// Nombre de series disponibles a l'ouverture
	protected int nombreAcquisitions;

	public View_Platelet() {
		super("Platelet");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException, ReadTagException {
		// Check number of images
		if (selectedImages.length == 0)
			throw new WrongNumberImagesException(selectedImages.length, 1, Integer.MAX_VALUE);

		ArrayList<ImagePlus> series = new ArrayList<>();

		for (ImageSelection selectedImage : selectedImages) {

			ImagePlus imp = selectedImage.getImagePlus().duplicate();
			selectedImage.getImagePlus().close();

			if (selectedImage.getImageOrientation() == Orientation.ANT_POST) {
				// On inverse pour avoir l'image post en 1er
				StackReverser reverser = new StackReverser();
				reverser.flipStack(imp);
				series.add(Library_Dicom.sortImageAntPost(imp));
			} else if (selectedImage.getImageOrientation() == Orientation.POST_ANT) {
				series.add(Library_Dicom.sortImageAntPost(imp));

			} else if (selectedImage.getImageOrientation() == Orientation.POST) {
				imp.getProcessor().flipHorizontal();
				series.add(imp);

			} else {
				throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
						selectedImage.getImageOrientation(),
						new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.POST});
			}

		}
		this.nombreAcquisitions = series.size();
		// IJ.log(String.valueOf(antPost));

		series.sort(new ChronologicalAcquisitionComparator.ImagePlusComparator());
		ImagePlus[] seriesTriee = new ImagePlus[series.size()];
		seriesTriee = series.toArray(seriesTriee);

		// On recupere la date et le jour de la 1ere image
		this.dateHeureDebut = Library_Dicom.getDateAcquisition(seriesTriee[0]);

		Concatenator enchainer = new Concatenator();
		// enchaine les images
		ImagePlus imp = enchainer.concatenate(seriesTriee, false);
		imp.show();

		HyperStackConverter convert = new HyperStackConverter();
		convert.run("hstostack");

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(imp.duplicate(), null, null);
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		this.setFenApplication(new FenApplication(selectedImages[0].getImagePlus(), this.getStudyName()));

		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus());
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		selectedImages[0].getImagePlus().setOverlay(overlay);

		Controller_Plaquettes ctrl = new Controller_Plaquettes(this, this.getDateDebut(), selectedImages, "Platelet");
		this.getFenApplication().setController(ctrl);
		this.getFenApplication().getImagePlus().getCanvas().setScaleToFit(true);
		this.getFenApplication().getImagePlus().getCanvas().setSize(512, 512);
		this.getFenApplication().pack();
		this.getFenApplication().setSize(this.getFenApplication().getPreferredSize());
	}

	public Date getDateDebut() {
		return this.dateHeureDebut;
	}

} // fin