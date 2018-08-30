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

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class FenApplication_Renal extends FenApplication implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RenalScintigraphy vue;
	private boolean dyn;
	private ImagePlus impProj;
	private Button btn_dyn, btn_start;

	public FenApplication_Renal(ImagePlus imp, String nom, RenalScintigraphy vue) {
		
		super(imp, nom);
		//Ajout du boutton dynamic au panel de gauche
		btn_dyn = new Button("Dynamic");
		btn_dyn.addActionListener(this);
		
		this.getPanel_btns_gauche().setLayout(new GridLayout(1, 4));
		this.getPanel_btns_gauche().add(btn_dyn);
		this.getPanel_btns_gauche().revalidate();
		
		// Remplacement boutons de droites sous l'instruction
		Panel btns_instru = new Panel(new GridLayout(1,1));
		btn_start = new Button("Start");
		btn_start.addActionListener(this);
		btns_instru.add(btn_start);
		
		this.getPanel_bttns_droit().removeAll();
		this.getPanel_bttns_droit().setLayout(new GridLayout(1,1));
		this.getPanel_bttns_droit().add(btns_instru);
		
		this.getBtn_drawROI().setEnabled(false);
		
		this.setDefaultSize();
		
		this.vue = vue;
		this.impProj = imp;
		
	
	}

	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.getTextfield_instructions().setText("Click to start the exam");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// clic sur le bouton dynamique
		if (e.getSource() == btn_dyn) {
			Overlay ov = Library_Gui.duplicateOverlay(this.getImagePlus().getOverlay());
			ImagePlus imp;

			if (!this.dyn) {
				if (this.getControleur().isPost()) {
					imp = vue.getImpPost();
				} else {
					imp = vue.getImpAnt();
				}
			} else {
				imp = this.impProj;
			}
			
			/*//si l'imp est null, on utilise l'image ant ou post
			if(imp == null) {
				if(vue.getImpPost() != null) {
					imp = vue.getImpPost();
				}else if(vue.getImpAnt() != null) {
					imp = vue.getImpAnt();
				}
			}*/

			imp.setOverlay(ov);
			Library_Gui.setCustomLut(imp);

			this.revalidate();
			this.setImage(imp);
			this.vue.setImp(imp);
			
			this.updateSliceSelector();

			this.setAnimate(false);

			// on inverse la couleur de fond du bouton
			this.dyn = !this.dyn;
			if (this.dyn) {
				this.btn_dyn.setBackground(Color.LIGHT_GRAY);
			} else {
				this.btn_dyn.setBackground(null);
			}

			resizeCanvas();

		//Mode debut du programme apres visualisation.
		} else if( e.getSource() == btn_start) {
			// TODO move elsewhere
			Fen_NbRein fen = new Fen_NbRein();
			fen.setModal(true);
			fen.setVisible(true);
			fen.setAlwaysOnTop(true);
			fen.setLocationRelativeTo(this);
			((Controleur_Renal) this.getControleur()).setKidneys(fen.getKidneys());

			this.getBtn_contrast().setEnabled(true);

			this.getPanel_bttns_droit().removeAll();
			this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
			this.getControleur().setInstructionsDelimit(0);
			this.getBtn_drawROI().setEnabled(true);
			IJ.setTool(Toolbar.POLYGON);
			this.vue.setImp(impProj);
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