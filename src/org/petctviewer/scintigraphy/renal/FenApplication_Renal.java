package org.petctviewer.scintigraphy.renal;

import java.awt.Button;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class FenApplication_Renal extends FenApplicationWorkflow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RenalScintigraphy vue;
	private boolean dyn;
	private ImagePlus impProj;
	private Button btn_dyn, btn_start;

	public FenApplication_Renal(ImageSelection ims, String nom, RenalScintigraphy vue) {
		super(ims, nom);
		// Keep default visualisation
		this.setVisualizationEnable(false);
		
		this.vue = vue;
		

		Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
		this.getImagePlus().setOverlay(overlay);
		Library_Gui.setOverlayGD(this.getImagePlus(), Color.YELLOW);
		Library_Gui.setOverlayTitle("Post", this.getImagePlus(), Color.yellow, 1);
		Library_Gui.setOverlayTitle("2 first min posterior", this.getImagePlus(), Color.YELLOW, 2);
		Library_Gui.setOverlayTitle("MIP", this.getImagePlus(), Color.YELLOW, 3);
		if (this.vue.getImpAnt() != null) {
			Library_Gui.setOverlayTitle("Ant", this.getImagePlus(), Color.yellow, 4);
		}
		
		// Ajout du boutton dynamic au panel de gauche
		btn_dyn = new Button("Dynamic");
		btn_dyn.addActionListener(this);

		this.getPanel_btns_gauche().setLayout(new GridLayout(1, 4));
		this.getPanel_btns_gauche().add(btn_dyn);
		this.getPanel_btns_gauche().revalidate();

		// Remplacement boutons de droites sous l'instruction
		Panel btns_instru = new Panel(new GridLayout(1, 1));
		btn_start = new Button("Start");
		btn_start.addActionListener(this);
		btns_instru.add(btn_start);

		this.getPanel_bttns_droit().removeAll();
		this.getPanel_bttns_droit().setLayout(new GridLayout(1, 1));
		this.getPanel_bttns_droit().add(btns_instru);

		this.getBtn_drawROI().setEnabled(false);

		this.setDefaultSize();

		
		this.impProj = imp;
		
		this.pack();

	}

	@Override
	public void setControleur(ControllerScin ctrl) {
		super.setControleur(ctrl);
		this.setText_instructions("Click to start the exam");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// clic sur le bouton dynamique
		if (e.getSource() == btn_dyn) {

			// Overlay ov = Library_Gui.duplicateOverlay(this.getImagePlus().getOverlay());
			ImagePlus imp;

			if (!this.dyn) {

				imp = vue.getImpPost().getImagePlus();
				Library_Gui.setCustomLut(imp);
				this.getImagePlus().getOverlay().clear();
				Overlay overlay = Library_Gui.initOverlay(imp);
				imp.setOverlay(overlay);
				Library_Gui.setOverlayGD(imp);
				revalidate();
				setImage(imp);
				resizeCanvas();
				updateSliceSelector();
				Library_Gui.setOverlayGD(imp, Color.YELLOW);
				this.btn_dyn.setBackground(Color.LIGHT_GRAY);

			} else {
				imp = this.impProj;
				Library_Gui.setCustomLut(imp);
				this.btn_dyn.setBackground(null);
				
				Library_Gui.setOverlayGD(imp, Color.YELLOW);
				Library_Gui.setOverlayTitle("Post", imp, Color.yellow, 1);
				Library_Gui.setOverlayTitle("2 first min posterior", imp, Color.YELLOW, 2);
				Library_Gui.setOverlayTitle("MIP", imp, Color.YELLOW, 3);
				if (this.vue.getImpAnt() != null) {
					Library_Gui.setOverlayTitle("Ant", imp, Color.yellow, 4);
				}

				revalidate();
				setImage(imp);
				updateSliceSelector();
				resizeCanvas();

			}

			/*
			 * //si l'imp est null, on utilise l'image ant ou post if(imp == null) {
			 * if(vue.getImpPost() != null) { imp = vue.getImpPost(); }else
			 * if(vue.getImpAnt() != null) { imp = vue.getImpAnt(); } }
			 */

			// on inverse le boolean pour l'utilisation suivante
			this.dyn = !this.dyn;

			// Mode debut du programme apres visualisation.
		} else if (e.getSource() == btn_start) {	
			btn_dyn.setEnabled(false);
			
			// TODO move elsewhere
			Fen_NbRein fen = new Fen_NbRein();
			fen.setLocationRelativeTo(this);
			fen.setModal(true);
			fen.setVisible(true);
			fen.setAlwaysOnTop(true);
			fen.setLocationRelativeTo(this);
			// ((Controller_Renal) this.getControleur()).setKidneys(fen.getKidneys());
			((ControllerWorkflowRenal) this.getControleur()).setKidneys(fen.getKidneys());

			this.getBtn_contrast().setEnabled(true);

			this.getPanel_bttns_droit().removeAll();
			this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
			// ((Controller_OrganeFixe)this.getControleur()).setInstructionsDelimit(0);
			this.getBtn_drawROI().setEnabled(true);
			IJ.setTool(Toolbar.POLYGON);
			this.setImage(impProj);
			this.updateSliceSelector();
			resizeCanvas();
		}

	}

	// TODO move that in it's own window
	private class Fen_NbRein extends JDialog implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean[] kidneys = new boolean[2];
		private JButton btn_l, btn_r, btn_lr;

		public Fen_NbRein() {
			this.setLayout(new GridLayout(2, 1));

			this.setTitle("Number of kidneys");

			JPanel flow = new JPanel();
			flow.add(this.add(new JLabel("How many kidneys has the patient ?")));
			this.add(flow);

			JPanel radio = new JPanel();
			this.btn_l = new JButton("Left kidney");
			this.btn_l.addActionListener(this);
			radio.add(btn_l);
			this.btn_r = new JButton("Right kidney");
			this.btn_r.addActionListener(this);
			radio.add(btn_r);
			this.btn_lr = new JButton("Both kidney");
			this.btn_lr.addActionListener(this);
			radio.add(btn_lr);
			this.add(radio);

			this.setLocationRelativeTo(FenApplication_Renal.this);

			this.pack();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JButton b = (JButton) arg0.getSource();
			if (b == this.btn_l) {
				this.kidneys[0] = true;
			} else if (b == this.btn_r) {
				this.kidneys[1] = true;
			} else if (b == this.btn_lr) {
				this.kidneys[0] = true;
				this.kidneys[1] = true;
			}

			this.dispose();
		}

		public boolean[] getKidneys() {
			if (this.kidneys == null) {
				return new boolean[] { true, true };
			}

			return this.kidneys;
		}
	}
}
