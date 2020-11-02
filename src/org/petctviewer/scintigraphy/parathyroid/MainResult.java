package org.petctviewer.scintigraphy.parathyroid;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MainResult extends TabResult {

	private final ImagePlus montage;
	private final ImagePlus result;
	List<ImagePlus> captures;

	public MainResult(FenResults parent, ImagePlus montage, String tabName) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = null;
		this.captures = null;
		this.reloadDisplay();
	}

	public MainResult(FenResults parent, ImagePlus montage, String tabName, ImagePlus result) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = result;
		this.reloadDisplay();
	}

	
	/** 
	 * @return Component
	 */
	@Override
	public Component getSidePanelContent() {
		if (this.result!=null){
			JPanel res = new JPanel(new GridLayout(0, 1));
			res.add(new JLabel("1.Early  "+ "\n" +
								"2.Late  \n"+
								"3.Subtract"));
			return res;
		}
		return null;
	}

	
	/** 
	 * @return JPanel
	 */
	@Override
	public JPanel getResultContent() {

		return new DynamicImage(montage.getImage());

	}

}
