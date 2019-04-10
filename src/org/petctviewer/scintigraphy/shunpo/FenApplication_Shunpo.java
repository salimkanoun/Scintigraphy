package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

public class FenApplication_Shunpo extends FenApplication {
	private static final long serialVersionUID = 1L;
	
	private Scintigraphy main;

	public FenApplication_Shunpo(Scintigraphy main) {
		super(main.getImp(), main.getExamType());
		this.main = main;
	}

}
