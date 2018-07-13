package org.petctviewer.scintigraphy.calibration.chargement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ControleurChargementCalibration implements ActionListener{

	ModeleChargementCalibration modele ;
	FenChargementCalibration fen ;
	
	public ControleurChargementCalibration(ArrayList<String[]> examList, FenChargementCalibration fenChargementCalibration) {
		//this.modele = new ModeleCalibration(examList);
		this.fen = fenChargementCalibration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//fen.lancerRes(this.modele.getDonnees());
		fen.lancerRes(ModeleChargementCalibration.setDonnees());
	}

	
	 public static void updateBouton(){
		    SwingWorker sw = new SwingWorker<Integer, String>(){
		      protected Integer doInBackground() throws Exception {
		        int i;
		        for(i = 0; i < 5; i++){
		          try {
		            //On change la propriété d'état
		            setProgress(i);
		            //On publie un résultat intermédiaire 
		            publish("Tour de boucle N° " + (i+1));
		            Thread.sleep(1000);
		          } catch (InterruptedException e) {
		            e.printStackTrace();
		          }               
		        }
		        return i;
		      }

		      public void done(){
		        if(SwingUtilities.isEventDispatchThread())
		          System.out.println("Dans l'EDT ! ");
		        try {
		          //On utilise la méthode get() pour récupérer le résultat
		          //de la méthode doInBackground()
		          bouton.setText("Traitement terminé au bout de "+get()+" fois !");
		        } catch (InterruptedException e) {
		          e.printStackTrace();
		        } catch (ExecutionException e) {
		          e.printStackTrace();
		        }
		      }   
		      //La méthode gérant les résultats intermédiaires
		      public void process(List<String> list){
		        for(String str : list)
		          System.out.println(str);
		      }
		    };
		    //On écoute le changement de valeur pour la propriété
		    sw.addPropertyChangeListener(new PropertyChangeListener(){
		      //Méthode de l'interface
		      public void propertyChange(PropertyChangeEvent event) {
		        //On vérifie tout de même le nom de la propriété
		        if("progress".equals(event.getPropertyName())){
		          if(SwingUtilities.isEventDispatchThread())
		            System.out.println("Dans le listener donc dans l'EDT ! ");
		          //On récupère sa nouvelle valeur
		          bouton.setText("Pause " + (Integer) event.getNewValue());
		        }            
		      }         
		    });
		    //On lance le SwingWorker
		    sw.execute();
		  }
	
}


