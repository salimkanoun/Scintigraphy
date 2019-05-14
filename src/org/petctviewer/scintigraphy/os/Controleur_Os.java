package org.petctviewer.scintigraphy.os;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.ImageSelection;



/**
 * DISCLAIMER :
 * Dans cette application, il a été fait comme choix d'initialiser le module par le biais du Contrôleur, qui va ensuite créer la vue et le modèle.
 * */
public class Controleur_Os implements ActionListener, ChangeListener, MouseListener{
	
	Modele_Os modele;
	FenApplication_Os vue;

	ImageSelection imp;
	
	public Controleur_Os(ImageSelection[] imps, OsScintigraphy scin) {
		
		this.modele = new Modele_Os(imps);
		this.imp=imps[0];
		
		this.vue = new FenApplication_Os(this);
		initialiser_Vue();
	}

	/**
	 * Listener permmettant d'inverser la LUT de chaque image, et donc son contraste.
	 *@param arg0
	 *            
	 * @return
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (((JButton)arg0.getSource()) == this.vue.getReverseButton()) {
			this.modele.inverser();
			rafraichir();
		}else {
			JPanel di = (JPanel)arg0.getSource();
			for (int i =0 ;i<this.modele.getNbScinti();i++) {														// This loop look for the DynamicImage clicked on the DynamicImage list (dynamicImps)
				for (int j = 0;j<2;j++) {
					if(di == this.modele.getDynamicImage(i, j)){									// When we found it
						this.modele.perform(i,j);													// We perform this DynamicImage
						this.vue.cadrer(i*2+j, modele.isSelected(i,j));
						this.vue.getSlider().setValue((int) ((this.vue.getSlider().getModel().getMaximum() - this.modele.getImagePlus(i, j).getImagePlus().getLuts()[0].max)+1));		// We put the slider value to the current ImagePlus contrast value.
					}
				}
			}
		}
	}


	 /**
		 * A chaque modification du slider, on change la valeur du contrast par setContrast(int).<br/>
		 *@param e
		 *            Origine de l'évènement
		 * @return
		 */
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.modele.setContrast(slider);
		rafraichir();
	}
	

	/**
	 * Listener permmettant de selectionner les images.
	 *@param arg0
	 *            
	 * @return
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
		JPanel di = (JPanel)arg0.getSource();
		for (int i =0 ;i<this.modele.getNbScinti();i++) {														// This loop look for the DynamicImage clicked on the DynamicImage list (dynamicImps)
			for (int j = 0;j<2;j++) {
				if(di == this.modele.getDynamicImage(i, j)){									// When we found it
					this.modele.perform(i,j);													// We perform this DynamicImage
					this.vue.cadrer(i*2+j, modele.isSelected(i,j));
					this.vue.getSlider().setValue((int) ((this.vue.getSlider().getModel().getMaximum() - this.modele.getImagePlus(i, j).getImagePlus().getLuts()[0].max)+1));		// We put the slider value to the current ImagePlus contrast value.
				}
			}
		}
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	
	
	
	
	private void rafraichir() {
		JPanel panel = this.vue.getZoneAffichage();
		panel.revalidate();
		panel.repaint();
	}
	
	private void initialiser_Vue() {
		JPanel panel = this.vue.getZoneAffichage();
		for(int i = 0; i<this.modele.getNbScinti() ; i++){																			// Pour toutes les images
			for (int j=0 ; j<2 ; j++) {
				panel.add(modele.getDynamicImage(i,j));
				modele.getDynamicImage(i,j).addMouseListener(this);
				modele.getDynamicImage(i,j).setBackground(Color.black);
			}
		}
	}
	
	public FenApplication_Os getFenApplicatio_Os() {
		return this.vue;
	}
	
	public ImageSelection getImageSelection() {
		return this.modele.getImageSelection();
	}
	
	public int getNbScinti() {
		return this.modele.getNbScinti();
	}

}
