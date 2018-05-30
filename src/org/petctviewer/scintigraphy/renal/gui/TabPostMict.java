package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.basic.CustomControleur;
import org.petctviewer.scintigraphy.scin.basic.VueScin_Basic;
import org.petctviewer.scintigraphy.scin.gui.FenResultatImp;

import ij.ImageJ;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.util.DicomTools;

public class TabPostMict extends FenResultatImp implements ActionListener, CustomControleur {

	private static final long serialVersionUID = 8125367912250906052L;
	private VueScin_Basic vue;
	private int height;
	private JButton btn_addImp;

	private JPanel grid;

	public TabPostMict(VueScin vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "");

		this.pack();
		this.height = h;

		this.grid = new JPanel(new GridLayout());

		btn_addImp = new JButton("Choose post-micturition dicom");
		btn_addImp.addActionListener(this);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());

		this.add(box, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));

		this.finishBuildingWindow(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String[] organes = new String[] { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg" };
		this.vue = new VueScin_Basic(organes, this);
	}

	@Override
	public void fin() {
		HashMap<String, Double> data = this.vue.getData();
		
		this.setImp(vue.getImp());
		
		System.out.println(data);
		
		//TODO moy geom si ant post
		Double rg = data.get("L. Kidney P0") - data.get("L. bkg P0");
		Double rd = data.get("R. Kidney P0") - data.get("R. bkg P0");
		int duration = Integer.parseInt(DicomTools.getTag(this.getImagePlus(), "0018,1242").trim());
		this.grid = (JPanel) this.getPanelNoRa(rg, rd, duration);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				btn_addImp.setVisible(false);
				finishBuildingWindow(true);
			}
		});
	}
	

	@Override
	public Component[] getSidePanelContent() {
		if (this.getImagePlus() != null) {
			Component[] compSuper = super.getSidePanelContent();
			Component[] comp = new Component[compSuper.length + 1];

			comp[0] = this.grid;
			
			if(compSuper != null) {
				comp[1] = compSuper[0];	
			}
			
			return comp;
		}
		return null;
	}

	@Override
	public Roi getOrganRoi(Roi roi) {
		int index = this.vue.getFenApplication().getControleur().getIndexRoi();
		if (index == 1 || index == 3) {
			return VueScin.createBkgRoi(roi, this.vue.getFenApplication().getImagePlus(), VueScin.KIDNEY);
		}
		return null;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Overlay ov = this.vue.getImp().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			VueScin.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			VueScin.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}
	}

	private Component getPanelNoRa(Double rg, Double rd, int duration) {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();
		Double[][] nora = modele.getNoRAPM(rg, rd, duration);
		// panel nora
		JPanel pnl_nora = new JPanel(new GridLayout(3, 3, 0, 3));
		pnl_nora.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_nora.add(new JLabel(" NORA Post-Mict"));
		pnl_nora.add(lbl_L);
		pnl_nora.add(lbl_R);

		pnl_nora.add(new JLabel("Max"));
		pnl_nora.add(new JLabel("" + nora[0][0] + " %"));
		pnl_nora.add(new JLabel("" + nora[0][1] + " %"));

		pnl_nora.add(new JLabel("" + ModeleScin.round(modele.getAdjustedValues()[6], 1) + " min"));
		pnl_nora.add(new JLabel("" + nora[1][0] + " %"));
		pnl_nora.add(new JLabel("" + nora[1][1] + " %"));

		return pnl_nora;
	}

}
