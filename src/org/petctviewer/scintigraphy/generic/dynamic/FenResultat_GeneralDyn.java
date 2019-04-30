package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

public class FenResultat_GeneralDyn extends FenResults {

	private static final long serialVersionUID = -6949646596222162929L;

	private String[][] asso;

	public FenResultat_GeneralDyn(ModeleScinDyn modele, String[][] asso) {
		super(modele);
		this.asso = asso;


		this.setLocationRelativeTo(modele.getImagePlus().getWindow());
	}

	public String[][] getAsso() {
		return this.asso;
	}
}
