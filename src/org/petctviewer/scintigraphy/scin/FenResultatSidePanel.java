package org.petctviewer.scintigraphy.scin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public abstract class FenResultatSidePanel extends JFrame {

	private static final long serialVersionUID = -5212479342782678916L;

	protected Box side;
	private JButton btn_capture;
	private VueScin vue;
	
	private String additionalInfo;
	
	private ModeleScin modele;

	/**
	 * Fenetre d'affichage de resultat presentant les informations du patient et le bouton capture dans un side panel sur la droite
	 * @param nomFen : Nom de la fenetre
	 * @param vueScin : Vue correspondante
	 * @param capture : capture de l'imp a afficher
	 * @param additionalInfo : informations supplementaires a ajouter au nom de fichier lors de la sauvegarde
	 */
	public FenResultatSidePanel(String nomFen, VueScin vueScin, BufferedImage capture, String additionalInfo) {

		this.vue = vueScin;
		this.modele = vueScin.getFen_application().getControleur().getModele();
		this.additionalInfo = additionalInfo;

		this.setLayout(new BorderLayout());

		this.side = Box.createVerticalBox();
		this.side.setBorder(new EmptyBorder(0, 10, 0, 10));
		
		//ajout de la capture
		JLabel img = new JLabel();
		
		if(capture != null) {
			img.setIcon(new ImageIcon(capture));
		}

		this.add(img, BorderLayout.WEST);

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>" + nomFen + "</h1><html>");
		titreFen.setHorizontalAlignment(JLabel.CENTER);
		flow.add(titreFen);
		this.side.add(flow);

		// ajout des informations du patient
		HashMap<String, String> infoPatient = ModeleScin.getPatientInfo(vueScin.getImp());
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("name")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));
		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		side.add(flow1);
	}
	
	/**
	 * Fini de construire la fenetre en incluant tous les components de la methode {@link #getSidePanelContent()}
	 */
	public void finishBuildingWindow() {
		side.add(Box.createVerticalGlue());
		
		//on ajoute tous les components de la methode getSidePanelContent
		if(this.getSidePanelContent() != null) {
			for(Component c : this.getSidePanelContent()) {
				side.add(c);
			}
		}

		side.add(Box.createVerticalGlue());

		//bouton capture
		this.btn_capture = new JButton("Capture");
		this.btn_capture.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.side.add(btn_capture);

		//label de credits
		JLabel credits = new JLabel("Provided by petctviewer.org");
		credits.setVisible(false);
		side.add(credits);

		//on ajoute le listener pour la capture
		this.vue.fen_application.getControleur().setCaptureButton(btn_capture, credits, this, this.modele, this.additionalInfo);
		
		//on ajoute le sie panel a droite de la fenetre
		this.add(side, BorderLayout.EAST);

		this.pack();
		
		this.setResizable(false);
		this.setVisible(true);
		this.setSize(this.getPreferredSize());
		this.setLocationRelativeTo(null);
	}

	/**
	 * Renvoie les components a afficher dans le side panel
	 * @return les components
	 */
	public abstract Component[] getSidePanelContent();

	public void setModele(ModeleScin modele) {
		this.modele = modele;
	}

}
