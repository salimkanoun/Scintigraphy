/**
Copyright (C) 2017 PING Xie and KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.gastric;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.petctviewer.scintigraphy.scin.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.*;
import ij.gui.*;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.plugin.StackReverser;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;

public class Vue_VG_Dynamique  implements PlugIn {

	public HashMap<String, Button> lesBoutons;// touts les buttons de l'interface 

	private Label img_inst;

	protected RoiManager leRoi;

	protected ImagePlus imp;

	private Controleur_VG_Dynamique leControleur = new Controleur_VG_Dynamique(this, new Modele_VG_Dynamique());

	protected String nomProgramme = "VG Dynamic";
	
	private boolean imageOuverte = false;// signifie si les images dynamiques sont ouverts
	
	protected Label instructions;//pour afficher les instructions

	protected CustomStackWindow windowstack;//la fenetre principale
	
	protected Overlay overlay;
	
	//private Dimension dimensionPanelPrincipal;

	
	@Override
	public void run(String arg) {
		RoiManager rm = new RoiManager(false);
		leRoi = rm;
		instructions = new Label("");
		instructions.setBackground(Color.LIGHT_GRAY);
		img_inst=new Label("");
		initBoutons();
		IJ.setTool(Toolbar.POLYGON);
		
		this.lesBoutons.get("Valider").addActionListener(new ActionListener() {
						@Override
							public void actionPerformed(ActionEvent e){
									Button b = (Button) e.getSource();
									//si les images dynamiques sont  ouvert au fenetre principal et il y a des images sont ouverts et il est pas des images au fenetre principal
									//c'est a dire ce qu'on a ouvert est l'image des oeufs
									//donc  on ouvert l'image des oeufs au fenetre principal
									if (WindowManager.getCurrentImage() != null && WindowManager.getCurrentImage() != imp && imageOuverte &&  WindowManager.getCurrentImage().getStackSize()==1 ) {
										ImagePlus imp=WindowManager.getCurrentImage();
										((Frame) b.getParent().getParent()).dispose();
										ouvertureImageOeuf(imp);
										lesBoutons.get("Suivant").setEnabled(true);
									}
									
							};
			});
						
		if (!this.imageOuverte) {
			ouvertureImage();
			imageOuverte = true;
		}
		
	}
	
	private void initBoutons() {

		this.lesBoutons = new HashMap<>();
		this.lesBoutons.put("Show", new Button("Show MG%"));
		this.lesBoutons.put("Draw ROI", new Button(" Draw ROI "));
		this.lesBoutons.put("Quitter", new Button(" Quit"));
		this.lesBoutons.put("Contrast", new Button(" Contrast "));
		this.lesBoutons.put("Precedent", new Button(" Previous "));
		this.lesBoutons.put("Suivant", new Button(" Next "));
		this.lesBoutons.put("Valider", new Button(" Confirm "));

	}
	
	public void setInstructions(String inst) {
		this.instructions.setText(inst);
	}
	
	//permet de demander a l'utilisateur de ouvrir les images
	public void ouvrirImage(String image) {
		Frame f = new Frame();
		Panel pan = new Panel();
		pan.setLayout(new GridLayout(2, 1));
		this.img_inst.setText("Please open  " + image + "  then confirm.");
		pan.add(this.img_inst);
		pan.add(this.lesBoutons.get("Valider"));
		f.add(pan);
		f.setLocationRelativeTo(null);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		f.setSize(200, 300);
		f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
		f.setVisible(true);
		f.setResizable(false);
		f.setAlwaysOnTop(true);
		f.pack();
		f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					leRoi.close();
					f.dispose();
					System.gc();
				}
			});
		if (this.imageOuverte==true){
			this.lesBoutons.get("Suivant").setEnabled(false);
		}
	}
	
	//permet de mettre les images dynamiques au fenetre principal
	private void ouvertureImage() {
			FenSelectionDicom selection=new FenSelectionDicom("Gastric Emptying");
			selection.setModal(true);
			selection.setVisible(true);
			String[] imagesOuvertes=selection.getSelectedWindowsTitles();
			
			if( imagesOuvertes !=null){
				
				ArrayList<ImagePlus> projete=new ArrayList<ImagePlus>();
				
				for (int i=0 ; i<imagesOuvertes.length; i++) {
					ImagePlus brute=WindowManager.getImage(imagesOuvertes[i]);
					//On cree l'imageProjetee et on l'ajoute a la liste
					
					//Si unique frame
					if (!VueScin.isMultiFrame(brute)) {
						projete.add(creationImageProjetee(brute)); 
						
					}
					//Si multiFrame mais meme camera
					else if ( VueScin.isMultiFrame(brute)  &&  VueScin.isSameCameraMultiFrame(brute)) {
						projete.add(creationImageProjetee(brute)); 
						
					}
					// Si multiframe avec plusieurs vues
					else if( VueScin.isMultiFrame(brute) && !VueScin.isSameCameraMultiFrame(brute)) {
						//On recupere les deux ImagePlus de chaque Vue
						ImagePlus [] deuxCamera=VueScin.splitCameraMultiFrame(brute);
						//On ajoute a part le ant et le post (qu'on flip horizontal) qui ont ete splite
						deuxCamera[0].setTitle("Anterior");
						projete.add(makeImageProjetee(deuxCamera[0], true));
						deuxCamera[1].setTitle("Posterior");
						projete.add(makeImageProjetee(deuxCamera[1], false));
						brute.close();
					};
					
					
				}
				
				//On trie les images par acquisition time
				ImagePlus[] projeteOrderTemp=VueScin.orderImagesByAcquisitionTime(projete);
				//On met l'image Ant apres l'imagePosterieur car sera inverse par la suite
				ImagePlus[] projeteOrder=new ImagePlus[projeteOrderTemp.length];
				for (int i=0 ; i<projeteOrderTemp.length; i+=2){
					if (projeteOrderTemp[i].getTitle().contains("Anterior")){
						projeteOrder[i]=projeteOrderTemp[i+1];
						projeteOrder[i+1]=projeteOrderTemp[i];
					}
					else {
						projeteOrder[i]=projeteOrderTemp[i];
						projeteOrder[i+1]=projeteOrderTemp[i+1];
					}
				}
				//On cree le stack a partir du tableau d'ImagePlus
				Concatenator enchainer = new Concatenator();
				ImagePlus imp = enchainer.concatenate(projeteOrder, false);
				//On inverse le stack pour avoir l'image la plus tardive en 1er
				StackReverser reverser=new StackReverser();
				reverser.flipStack(imp);
				//On affiche
				imp.show();
				
				HyperStackConverter convert= new HyperStackConverter();
				convert.run("hstostack");
				String serie = DicomTools.getTag(imp, "0008,103E");
				String tag = DicomTools.getTag(imp, "0010,0010");
				String titre = this.nomProgramme + " - " + tag + " - " + serie;
				//On appelle la fonction de Vue_Shunpo pour mettre la lut des preference
				VueScin.setCustomLut(imp);
				// On cree la fenetre avec la pile d'image
				windowstack = new CustomStackWindow(imp);
				windowstack.showSlice(1); //=> equivalent au setslice mais moins de bug
				this.imp=imp;
				//On change les titres
				imp.setTitle(titre);
				windowstack.setTitle(titre);
				this.overlay=VueScin.initOverlay(imp, 12);
				VueScin.setOverlayDG(overlay, imp);
				// On set la dimension de l'image
				windowstack.getCanvas().setSize(new Dimension(512,512));
				windowstack.getCanvas().setScaleToFit(true);
				//On Pack la fenetre pour la mettre a la preferred Size
				windowstack.pack();
				windowstack.setSize(windowstack.getPreferredSize());
				// La fenetre se place au premier plan
				windowstack.toFront();
				this.imageOuverte=true;
				if (this.instructions.getText().equals(""))
					this.instructions.setText("Delimit the Stomache");
				lesBoutons.get("Precedent").setEnabled(false);
				windowstack.getImagePlus().setOverlay(this.overlay);
				
			}
			else {
				leRoi.close();
			}
			
	}
	
	private ImagePlus creationImageProjetee(ImagePlus brute) {
		ImagePlus ImageProjetee=null;
		Boolean anterieur=VueScin.isAnterieur(brute);
		if (anterieur!=null && anterieur){
			brute.setTitle("Anterior");
			ImageProjetee=makeImageProjetee(brute, true);
			}
		else if (anterieur!=null && !anterieur){
			brute.setTitle("Posterior");
			ImageProjetee=makeImageProjetee(brute, false);
			}
		else {
			if (VueScin.isPremiereImageDetecteur1(brute)) ImageProjetee=makeImageProjetee(brute, true) ;
			else if (!VueScin.isPremiereImageDetecteur1(brute)) ImageProjetee=makeImageProjetee(brute, false) ;
		}
		return ImageProjetee;
		
	}
	
	private ImagePlus makeImageProjetee(ImagePlus brute, boolean anterior) {
		String metadata=brute.getInfoProperty();
		//On fait la somme des 10 premieres coupes
		ZProjector projector=new ZProjector();
		projector.setImage(brute);
		projector.setMethod(ij.plugin.ZProjector.SUM_METHOD);
		projector.setStartSlice(1);
		projector.setStopSlice(10);
		projector.doProjection();
		ImagePlus ImageProjetee=projector.getProjection();
		ImageProjetee.setProperty("Info", metadata);
		
		// Si posterieur on flip
		if(!anterior){
			ImageProjetee.getProcessor().flipHorizontal();
		}
		
		brute.close();
		return ImageProjetee;
	}
	
	//permet de mettre l'image des oeufs au fenetre principal
	private void ouvertureImageOeuf(ImagePlus imp) {
		ImagePlus oeufs=(ImagePlus) imp.clone();
		//on remplace l'image des oeufs au fenetre
		windowstack.setImage(oeufs);
		String serie = DicomTools.getTag(imp, "0008,103E");
		String patient = DicomTools.getTag(imp, "0010,0010");
		imp.getWindow().close();
		String titre = this.nomProgramme + " - " + patient + " - " + serie;
		oeufs.setTitle(titre);
		windowstack.setTitle(titre);
		ImageCanvas cc=windowstack.getCanvas();
		this.imp=oeufs;
		cc.fitToWindow();
		windowstack.toFront();
		windowstack.getImagePlus().deleteRoi();
		ActionEvent e = new ActionEvent(lesBoutons.get("Suivant"),ActionEvent.ACTION_PERFORMED,"Suivant");
		leControleur.actionPerformed(e);
		}
	
	// Extraite du code de la classe Panel_Window de Wayne Rasband
		// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
		class CustomCanvas extends ImageCanvas {

			private static final long serialVersionUID = -5710708795558320699L;

			CustomCanvas(ImagePlus imp) {

				super(imp);

			} // Fin constructeur CustomCanvas

		} // Fin CustomCanvas
		
	// Extraite puis modifiee du code de la classe Panel_Window de Wayne Rasband
		// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
		class CustomStackWindow extends StackWindow {

			private static final long serialVersionUID = -6280620624574294247L;

			CustomStackWindow(ImagePlus imp) {
				super(imp, new CustomCanvas(imp));
				addPanel();
			} // Fin constructeur CustomStackWindow

			// Construction de l'interface
			void addPanel() {
				// Agencement des composants
				Panel panel = new Panel();
				panel.setLayout(new FlowLayout());
				addEcouteurs();

				// Partie gauche
				Panel gauche = new Panel();
				gauche.setLayout(new FlowLayout());

				// Premiers boutons
				Panel btns_glob = new Panel();
				btns_glob.setLayout(new GridLayout(1, 3));
				btns_glob.add(lesBoutons.get("Quitter"));
				btns_glob.add(lesBoutons.get("Draw ROI"));
				btns_glob.add(lesBoutons.get("Contrast"));
				gauche.add(btns_glob);

				// Instructions
				Panel instru = new Panel();
				instru.setLayout(new GridLayout(2, 1));
				instru.add(instructions);
				Panel btns_instru = new Panel();
				btns_instru.setLayout(new GridLayout(1, 3));
				btns_instru.add(lesBoutons.get("Show"));
				btns_instru.add(lesBoutons.get("Precedent"));
				lesBoutons.get("Precedent").setEnabled(false);
				btns_instru.add(lesBoutons.get("Suivant"));
				instru.add(btns_instru);
				gauche.add(instru);
				panel.add(gauche);
				add(panel);
				pack();

				// Permet d'avoir la fenetre au meme endroit que le stack original
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				Point loc = getLocation();
				Dimension size = getSize();
				if (loc.y + size.height > screen.height)
					getCanvas().zoomOut(0, 0);
			} // Fin addPanel

			private void addEcouteurs() {
				for (Entry<String, Button> entry : lesBoutons.entrySet())
					entry.getValue().addActionListener(leControleur);
			}
			
			 @Override
	         public void windowClosing(WindowEvent we) {
				 //On ferme le ROI manager en plus de la fenetre
				 leRoi.close();
				 windowstack.close();
				 System.gc();
	         }

		} // Fin CustomStackWindow
		
		
		// fenetre de fermeture du programme
		public void end() {
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "",
					JOptionPane.OK_CANCEL_OPTION);
			if (optionChoisie == JOptionPane.OK_OPTION) {
				windowstack.close();
				leRoi.close();
			}
		}
		
	


		// Dialog pour afficher les % de chaque oeuf et les modifier si necessaire
		public void confirmerOeufPourc() {
			JDialog dialog = new JDialog();
			dialog.setLayout(new GridLayout(2, 1));
			JPanel panelOeufPourc=new JPanel();
			panelOeufPourc.setLayout(new GridLayout(Controleur_VG_Dynamique.oeufsIngere/ 2, 2));
			JTextField[] textOeufPourc=new JTextField[Controleur_VG_Dynamique.oeufsIngere];
			for(int i=0; i<Controleur_VG_Dynamique.oeufsIngere;i++){
				JPanel panelOeuf=new JPanel();
				panelOeuf.setLayout(new FlowLayout());
				panelOeuf.add(new JLabel("egg"+(i+1)+" : "));
				textOeufPourc[i]=new JTextField(Double.toString(Modele_VG_Dynamique.oeufPourc[i]));
				panelOeuf.add(textOeufPourc[i]);
				panelOeufPourc.add(panelOeuf);
				
			}
			dialog.add(panelOeufPourc);

			// ajoute un button au panel du valide
			JButton button = new JButton("OK");
			
			button.setBounds(120, 10, 60, 30);
			if (Modele_VG_Dynamique.logOn) IJ.log("coups des oeufs apres valide");
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					//on recupere les valeurs de chaque texte comme le % de chaque oeuf
					for(int i=0; i<Controleur_VG_Dynamique.oeufsIngere;i++){
						Modele_VG_Dynamique.oeufPourc[i]=Double.parseDouble(textOeufPourc[i].getText());
						ActionEvent e = new ActionEvent(lesBoutons.get("Suivant"),1234,"Suivant");
						leControleur.actionPerformed(e);
						if (Modele_VG_Dynamique.logOn) IJ.log("oeuf "+(i+1)+" : "+Modele_VG_Dynamique.oeufPourc[i]);
					}
					dialog.dispose();
				}
			});
			dialog.add(button);
			dialog.setTitle("Please confirm percentage of eggs");
			dialog.setVisible(true);
			dialog.setAlwaysOnTop(true);
			dialog.setSize(330, 170);
			dialog.setLocationRelativeTo(windowstack);
			dialog.requestFocus();
			}

}
