package org.petctviewer.scintigraphy.shunpo;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import ij.ImagePlus;

public class MainResult extends TabResult {
	
	private ImagePlus montage;

	public MainResult(FenResults parent, ImagePlus montage) {
		super(parent, "Result", true);
		this.montage = montage;
		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		String[] result = ((ModelShunpo) this.parent.getModel()).getResult();
		JPanel res = new JPanel(new GridLayout(result.length, 1));
		for (String s : result)
			res.add(new JLabel(s));
		return res;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}

}
