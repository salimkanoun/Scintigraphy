package org.petctviewer.scintigraphy.liver;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenResultat_Liver extends FenResultatSidePanel {

	public FenResultat_Liver(String nomFen, VueScin vueScin, BufferedImage capture, String additionalInfo) {
		super(nomFen, vueScin, capture, additionalInfo);
	}

}
