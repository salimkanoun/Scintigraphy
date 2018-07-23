package org.petctviewer.scintigraphy.esophageus;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;

public class EsophagealTransit extends DynamicScintigraphy {

	public EsophagealTransit() {
		super("Eso");
	}


	@Override
	public void lancerProgramme() {
		FenApplication_EsophagealTransit fen = new FenApplication_EsophagealTransit(this.getImpAnt());
		fen.setVisible(true);
	}


	

}
