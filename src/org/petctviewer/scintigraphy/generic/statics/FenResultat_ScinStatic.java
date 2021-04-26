package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.generic.statics.tab.TabComparisonStaticScin;
import org.petctviewer.scintigraphy.generic.statics.tab.TabMainStaticScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.awt.image.BufferedImage;

public class FenResultat_ScinStatic extends FenResults {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FenResultat_ScinStatic(BufferedImage capture, ControllerScin controller) {
		super(controller);

		ModelScinStatic model = (ModelScinStatic) this.getModel();
		TabMainStaticScin tabMain = new TabMainStaticScin(this, "Result", capture);
		
		if(!model.isSingleSlice() || model.isAnt())
			tabMain.addAntTab(((ModelScinStatic) controller.getModel()).calculerTableauAnt());
		if( !model.isSingleSlice() || !model.isAnt())
			tabMain.addPostTab(((ModelScinStatic) controller.getModel()).calculerTableauPost());
		if(!model.isSingleSlice())
			tabMain.addMoyGeomTab(((ModelScinStatic) controller.getModel()).calculerTaleauMoyGeom());

		this.addTab(tabMain);
		if (model.getRoiManager().getCount() >= 2) {
			TabComparisonStaticScin tabComp = new TabComparisonStaticScin(this, "Comparison", capture);
			this.addTab(tabComp);
		}

		this.setLocationRelativeTo(controller.getModel().getImagePlus().getWindow());
	}

}
