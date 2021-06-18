package org.petctviewer.scintigraphy.colonic;

import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ImageSelection;
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
				this.popup.addWindowListener(this);
				this.popup.setResizable(false);
				this.popup.pack();
			} else
				this.popup.requestFocus();

		});

		this.pack();
	}
	
	void setPopupToNull() {
		this.popup = null;
	}

	private static class popupColonicROIs extends JFrame {

		private static final long serialVersionUID = 1L;

		public popupColonicROIs() {
			URL res = this.getClass().getClassLoader().getResource("images/colonic/colon_fin.jpg");
			if (res != null) {
				JPanel panel = new JPanel();
				JLabel labelfake = new JLabel();
				labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(res)));
				panel.add(labelfake);
				this.add(panel);
//				this.add(new DynamicImage(FenApplicationColonicTransit.toBufferedImage(Toolkit.getDefaultToolkit().getImage(res))));
				this.setTitle("Expected ROIs for colonic scintigraphy");
			
				this.repaint();
				this.pack();
			}
		}
	}
	
	@Override
	public void windowClosing(WindowEvent arg0) {
		if (arg0.getSource() == this.popup) {
			this.popup = null;
			System.out.println("ok");
		}
		else
			super.windowClosing(arg0);
	}


}
