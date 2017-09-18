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

import java.awt.*;
import java.util.ArrayList;
import ij.*;
import ij.gui.*;
import ij.plugin.ContrastEnhancer;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;

public class Condense_Dynamique  implements PlugIn {

	private ArrayList<ImagePlus> condenses = new ArrayList<ImagePlus>();
	private String tag=new String();
	private Dimension dimCondense;
	private Point p=new Point();
	
	@Override
	public void run(String arg) {
		ouvertureImage();	
	}
	
	private void ouvertureImage() {
			
			IJ.setTool("Rectangle");
		
			WaitForUserDialog wait=new WaitForUserDialog("Please Open Images");
			wait.show();
			//On liste les images ouvertes
			String[] imagesOuvertes=WindowManager.getImageTitles();
			
			if (imagesOuvertes!=null) {
				
			}
			
			//On ferme les images posterieures et on assigne a chaque image un nom unique car sinon confusion du programme (les images originales on le meme nom)
			for (int i=0 ; i<imagesOuvertes.length; i++) {
				ImagePlus brutepost=WindowManager.getImage(imagesOuvertes[i]);
				if (!Vue_Shunpo.isAnterieur(brutepost)) {
					 brutepost.close();
					 continue;
				}
				if (Vue_Shunpo.isAnterieur(brutepost)) {
					 brutepost.setTitle("Image"+i);;
				}
			}
			imagesOuvertes=null;
			//On liste les images restantes
			imagesOuvertes=WindowManager.getImageTitles();
			
			//On sauve le header
			tag=Modele_Shunpo.genererDicomTagsPartie1(WindowManager.getImage(imagesOuvertes[0]), "Condense");
			
			//On met les images ouvertes dans un tableau pour les trier
			ImagePlus[] imagesOuvertesPlus=new ImagePlus[imagesOuvertes.length];
			for (int i=0 ; i<imagesOuvertes.length; i++) {
				imagesOuvertesPlus[i]=WindowManager.getImage(imagesOuvertes[i]);
			}
			
		
			
			//On trie le tableau par heure d'acquisition
			ImagePlus[] imagesOuvertesOrdonees=Vue_Shunpo.ordonnerSerie(imagesOuvertesPlus);
			p.setLocation(imagesOuvertesOrdonees[0].getWidth()/2, 0);
			
			for (int i=0 ; i<imagesOuvertesOrdonees.length; i++) {
				
				ImagePlus brute=imagesOuvertesOrdonees[i];
				
					if (Vue_Shunpo.isAnterieur(brute)){
						brute.setTitle("Anterior"+i);
						//On cree la somme des 10 premieres images pour la visualisation
						ZProjector projector=new ZProjector();
						projector.setImage(brute);
						projector.setMethod(ij.plugin.ZProjector.SUM_METHOD);
						projector.setStartSlice(1);
						projector.setStopSlice(10);
						projector.doProjection();
						ImagePlus projete=projector.getProjection();
						Rectangle r= new Rectangle();
						r.setSize(9,brute.getHeight());
						r.setLocation(p);
						projete.setRoi(r);
						//On deplace la fenetre au centre de l'écran et on l'agrandi
						projete.show();
						projete.getWindow().setSize(512,512);
						//On calcule la dimension du condense
						dimCondense=new Dimension (9*brute.getStackSize(), brute.getHeight());
						//On regle l'affichage
						projete.getCanvas().setScaleToFit(true);
						ContrastEnhancer contrast= new ContrastEnhancer();
						contrast.setProcessStack(true);
						contrast.stretchHistogram(projete, 0.35);
						projete.getWindow().toFront();
						//On demande � l'utilisateur de regler la ROI
						wait=new WaitForUserDialog("Ajust the ROI");
						wait.show();
						Roi roiAjustee=projete.getRoi();
						Rectangle rectangleRoi=roiAjustee.getBounds();
						//On met en memoire la valeur X de la ROI pour le mettre au meme endroit le tour suivant
						p.setLocation(rectangleRoi.getX(),0);
						double largeur=projete.getRoi().getFloatWidth();
						//On force l'utilisateur � garder la largeur a 9pixel
						while (largeur!=9){
							IJ.showMessage("Don't change ROI width");
							projete.setRoi(r);
							wait=new WaitForUserDialog("Ajust the ROI");
							wait.show();
							roiAjustee=projete.getRoi();
							rectangleRoi=roiAjustee.getBounds();
							//On met en memoire la valeur X de la ROI pour le mettre au meme endroit le tour suivant
							p.setLocation(rectangleRoi.getX(),0);
							largeur=projete.getRoi().getFloatWidth();
						}
						//On recupere la Roi et on l'applique dans l'image originale
						brute.setRoi(roiAjustee); 
						projete.close();
						//On genere le condense qu'on met dans l'array
						ImagePlus condensetemp=condense2(brute,rectangleRoi);
						condensetemp.setTitle("Ingestion "+(i+1));
						condenses.add(condensetemp);
						brute.killRoi();
						brute.close();
						
						}
			}
			
			ImagePlus imp2=condenseOutput();
			//On affiche le r�sultat
			imp2.show();
			//On applique la LUT par defaut
			Vue_Shunpo.setCustomLut(imp2);
			//On resize l'image
			Dimension d=new Dimension();
			d.setSize(imp2.getWidth()*1.75, imp2.getHeight()*1.75);
			imp2.getWindow().setSize(d);
			imp2.getCanvas().setScaleToFit(true);
			IJ.run("Window Level Tool");
			//On demande d'ajuster le contraste
			wait=new WaitForUserDialog("Adjust contrast and click OK to Capture");
			wait.show();
			//On capture
			ImagePlus imp3=Modele_Shunpo.captureImage(imp2, 700, 0);
			imp2.close();
			//On resize
			imp3.show();
			imp3.getCanvas().setScaleToFit(true);
			//On fait le header DICOM
			tag+=Modele_Shunpo.genererDicomTagsPartie2(imp2);
			imp3.setProperty("Info", tag);
			//On propose de sauver en DICOM
			IJ.run("myDicom...");
			
			
	}
	
	protected ImagePlus condenseOutput(){
		//On ouvre un stack de bonne dimension
		int dimensionStack[]=condenses.get(0).getDimensions();
		ImageStack stack=new ImageStack(dimensionStack[0],dimensionStack[1]);
		// On regarde combien on a de condense
		int nombrecondense=condenses.size();
		// Pour chaque condense on ajoute une slice dans le stack
		for (int i=0 ; i<nombrecondense ; i++) {
			stack.addSlice(condenses.get(i).getProcessor());
			//On set le label de chaque coupe pour qu'elle apparaisse dans le montage
			stack.setSliceLabel(condenses.get(i).getTitle(), i+1);
		}
		//On cree une imageplus qui recoit le stack
		ImagePlus imp = new ImagePlus();
		imp.setStack(stack);
		//On fait le montage et on affiche
		MontageMaker mm = new MontageMaker();
		ImagePlus imp2=mm.makeMontage2(imp, 1, stack.getSize(), 0.75, 1, stack.getSize(), 1, 10, true);
		return imp2;
		
	}
	
	protected ImagePlus condense2(ImagePlus imp, Rectangle roi) {
		int coupes=imp.getStack().getSize();
		ImagePlus imageCondensee=IJ.createImage("Image", "16-bit black", dimCondense.width, dimCondense.height, coupes);
		//imp.hide();
		for (int i=0; i<coupes; i++) {
			imp.setSlice(i+1);
			Rectangle imageShift=new Rectangle();
			imageShift.setBounds((int) Math.round(roi.getX()), (int) Math.round(roi.getY()), (int) Math.round(imp.getWidth()-roi.getX()), imp.getHeight());
			imp.setRoi(imageShift);
			//On copie cette zone
			imp.copy();
			//on cree une nouvelle imagePlus de la taille finale
			ImagePlus image=IJ.createImage("Image", "16-bit black", dimCondense.width, dimCondense.height, 1);
			//On met un nouveau rectangle qu'on shift de 9 pixel et  on colle dans cette image
			Rectangle recDestination=new Rectangle();
			recDestination.setBounds(i*9, imageShift.y, imageShift.width, imageShift.height);
			//recDestination.setLocation(i*9, 0);
			image.setRoi(recDestination);
			//image.show();
			image.paste();
			image.killRoi();
			//On l'ajoute � l'image condensee
			imageCondensee.getStack().setProcessor(image.getProcessor(),i+1);
			
		}
		//On fait la somme du stack pour avoir l'image finale
		ZProjector projector=new ZProjector();
		projector.setImage(imageCondensee);
		projector.setMethod(ij.plugin.ZProjector.SUM_METHOD);
		projector.setStartSlice(1);
		projector.setStopSlice(coupes);
		projector.doProjection();
		ImagePlus projete=projector.getProjection();

		return projete;
	}
	
}
	