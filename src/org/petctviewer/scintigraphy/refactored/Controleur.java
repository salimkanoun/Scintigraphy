package org.petctviewer.scintigraphy.refactored;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controleur implements ActionListener{

	private Modele modele;
	
	public Controleur(VueRefactored vueRefactored) {
		String[] organes = new String[] {"pied", "main", "tete"};
		this.modele = new Modele(vueRefactored.getImagePlus(), new CalculateurImp());
		this.modele.setOrganes(organes);
		
		vueRefactored.getInstructions().setText(modele.getInstructions());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Button b = (Button) e.getSource();
		
		switch(b.getLabel()) {
			case "Previous":
				
				break;
			case "Next":
				
				break;
			default:
				System.out.println("Bouton non pris en compte");
		}
	}

}
