package org.petctviewer.scintigraphy.gastric.tabs;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.gastric.Unit;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.awt.*;

public class TabLiquidPhase extends TabResultDefault {

	public TabLiquidPhase(FenResults parent, ImagePlus capture, int seriesToGenerate) {
		super(parent, capture, "Liquid Phase", Unit.PERCENTAGE, Unit.TIME, seriesToGenerate);
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}
}
