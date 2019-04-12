package org.petctviewer.scintigraphy.os;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import ij.ImagePlus;



public class FenApplication_Os extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7121587802669948009L;
	private JSlider slider;
	private Scintigraphy scin;
	private JLabel sliderLabel;
	protected Box boxSlider;
	protected JButton reverseButton;
	
	private JPanel grid;												// Panneau central contenant les DynamicImage de la Scintigrapjie Osseuse

	protected SidePanel sidePanel;
	String additionalInfo, nomFen;
	
	private int nbScinty;
	
	
	
	 /**
		 * Constructeur de la fenêtre permettant de visualiser la scintigraphie osseuse.<br/>
		 * Prend en paramètre la fenêtre qui la lance, ainsi que les images du patient.<br/>
		 * Le buffer transmis est un tableau à double dimension possédant 2 colonnes et n ligne (n = nombre de patient).
		 * Chaque ligne est un patient. <br/>
		 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus POST du patient.<br/>
		 * 
		 * Crée un sidePanel, qui affiche quelques informations du patient.<br/>
		 * Appelle finishBuildingWindow() à la fin de l'execution.<br/>
		 * @param selectedImages
		 *            liste des images transmises depuis FenSelectionDicom
		 *@param img
		 *            Tableau à double dimension des images transmises
		 * @return
		 */
	public FenApplication_Os(OsScintigraphy scin, ImagePlus[][] img,Controleur_Os controleur_os) {
		super(new BorderLayout());
		
		this.setScin(scin);
		
		this.nbScinty = img.length;
		System.out.println("Vue : "+this.nbScinty);
		
		this.additionalInfo = "Info";
		this.nomFen = "Fen";
		
		sidePanel = new SidePanel(null, "Bone scintigraphy", scin.getImp());
		
		// sidePanel.addCaptureBtn(scin, "_other");
		this.add(sidePanel, BorderLayout.WEST);
		
		this.grid = new JPanel(new GridLayout(1, nbScinty*2));
		
		this.add(grid, BorderLayout.CENTER);
		
		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		
		this.finishBuildingWindow(controleur_os);
	}

	
	
	
	
	 /**
	 * Fini la contruction de la fenêtre.<br/>
	 * Crée un slider et le place. Ce slider servira à faire varier le contraste des images.<br/>
	 * Crée le tableau des DynamicImage à partir des ImagePlus reçues.<br/>
	 * Affete à chaque DynamicImage un Listener, permettant de le selectionner.<br/>
	 * Enregistre la valeure macimale du slider, qui permet de gérer le contraste.<br/>
	 * Crée un gridLayout et ajoute dedans les DynamicImage créés.<br/>
	 *@param img
	 *            Tableau à double dimension des images transmises
	 * @return
	 */
	public void finishBuildingWindow(Controleur_Os controleur_Os) {

		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) controleur_Os.getImp().getStatistics().max, 4);
		slider.addChangeListener(controleur_Os);

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);
		
		
		this.reverseButton = new JButton("Inverser");														// Boutton inversant le contraste.
		this.reverseButton.addActionListener(controleur_Os);
		this.reverseButton.setPreferredSize(new Dimension(200, 40));

		JPanel gbl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2;       //third row
		c.insets = new Insets(10,0,0,0);  //top padding
		gbl.add(boxSlider);
		gbl.add(this.reverseButton,c);
		
		sidePanel.add(gbl);


		// sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] { this.slider });
		this.add(sidePanel, BorderLayout.WEST);
	}
	


	public Box getBoxSlider() {
		return this.boxSlider;
	}
	public JSlider getSlider() {
		return this.slider;
	}
	public Scintigraphy getScin() {
		return scin;
	}
	public void setScin(Scintigraphy scin) {
		this.scin = scin;
	}
	
	
	public void cadrer(int index,boolean selected) {
		if(selected)
			((DynamicImage) grid.getComponent(index)).setBorder(BorderFactory.createMatteBorder(
                    3, 3, 3, 3, new Color(148,252,9)));
		else
			((DynamicImage) grid.getComponent(index)).setBorder(BorderFactory.createMatteBorder(
					0, 0, 0, 0, Color.black));
	}
	

	public JPanel getZoneAffichage() {
		return this.grid;
	}
	
	public JButton getReverseButton() {
		return this.reverseButton;
	}
	
}
