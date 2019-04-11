package org.petctviewer.scintigraphy.scin;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.gui.Toolbar;

public abstract class ControleurScin implements ActionListener {

	protected FenApplication vue;

	public ControleurScin(FenApplication vue) {
		this.vue = vue;
	}

	/**
	 * This method is called when the 'Previous' button is pressed.
	 */
	public abstract void clicPrecedent();

	/**
	 * This method is called when the 'Next' button is pressed.
	 */
	public abstract void clicSuivant();

	/**
	 * This method is called when the FenApplication is closed.
	 */
	public abstract void close();

	@Override
	public void actionPerformed(ActionEvent e) {
		Button b = (Button) e.getSource();

		if (b == this.vue.getBtn_suivant()) {
			this.clicSuivant();

		} else if (b == this.vue.getBtn_precedent()) {
			this.clicPrecedent();

		} else if (b == this.vue.getBtn_drawROI()) {
			Button btn = this.vue.getBtn_drawROI();

			// on change la couleur du bouton
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}

			// on deselectionne le bouton contraste
			this.vue.getBtn_contrast().setBackground(null);

			IJ.setTool(Toolbar.POLYGON);

		} else if (b == this.vue.getBtn_contrast()) {
			// on change la couleur du bouton
			if (b.getBackground() != Color.LIGHT_GRAY) {
				b.setBackground(Color.LIGHT_GRAY);
			} else {
				b.setBackground(null);
			}

			// on deselectionne le bouton draw roi
			this.vue.getBtn_drawROI().setBackground(null);

			IJ.run("Window Level Tool");

		} else if (b == this.vue.getBtn_quitter()) {
			this.vue.close();
			return;
		}
	}

}
