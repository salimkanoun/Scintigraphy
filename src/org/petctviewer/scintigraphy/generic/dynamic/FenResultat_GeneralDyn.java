package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

public class FenResultat_GeneralDyn extends FenResults {

	private static final long serialVersionUID = -6949646596222162929L;

	private String[][] asso;

	public FenResultat_GeneralDyn(ControllerScin controller, String[][] asso) {
		super(controller);
		this.asso = asso;


		this.setLocationRelativeTo(controller.getModel().getImagePlus().getWindow());
	}

	public String[][] getAsso() {
		return this.asso;
	}
}
