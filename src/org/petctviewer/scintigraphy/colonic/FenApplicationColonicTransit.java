package org.petctviewer.scintigraphy.colonic;

import java.awt.MenuItem;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JFrame;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

public class FenApplicationColonicTransit extends FenApplicationWorkflow {

	private static final long serialVersionUID = -3302015218823308415L;

	private popupColonicROIs popup;

	public FenApplicationColonicTransit(ImageSelection ims, String nom) {
		super(ims, nom);

		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());

		MenuItem colonicRoi = new MenuItem("Colonic expected ROIs");
		this.getHelpMenu().add(colonicRoi);
		colonicRoi.addActionListener(e -> {
			if (this.popup == null) {
				this.popup = new popupColonicROIs();
				this.popup.setLocationRelativeTo(this);
				this.popup.setVisible(true);
				this.popup.setResizable(false);
				this.popup.addWindowListener(this);
				this.popup.pack();
			} else
				this.popup.requestFocus();

		});

		this.pack();
	}

	private class popupColonicROIs extends JFrame {

		private static final long serialVersionUID = 1L;

		public popupColonicROIs() {
			URL res = this.getClass().getClassLoader().getResource("images/colonic/colon_fin.jpg");
			if (res != null)
				this.add(new DynamicImage(Toolkit.getDefaultToolkit().getImage(res)));
		}
	}

}
