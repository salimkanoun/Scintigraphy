package org.petctviewer.scintigraphy.esophageus.resultats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;

public class Modele_Resultats_EsophagealTransit {

	private XYSeries [][] datasetMain;
	private XYSeries [][] datasetTransitTime;

	public Modele_Resultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList) {
			
		// x examen et 4 coubres
		datasetMain = new XYSeries[arrayList.size()][4];
		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetMain[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "entier "+i);
			datasetMain[i][1] = this.listToXYSeries(arrayList.get(i).get("unTier"), arrayList.get(i).get("temps"), "un tier "+i);
			datasetMain[i][2] = this.listToXYSeries(arrayList.get(i).get("deuxTier"), arrayList.get(i).get("temps"), "deux tier "+i);
			datasetMain[i][3] = this.listToXYSeries(arrayList.get(i).get("troisTier"), arrayList.get(i).get("temps"), "trois tier "+i);
		}
		
		// x examen et 4 coubres
		datasetTransitTime = new XYSeries[arrayList.size()][1];
		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetTransitTime[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "entier "+i);
		}
	}
	
	public XYSeries[][] getDataSetMain() {
		return this.datasetMain;
	}
	
	public XYSeries[][] getDataSetTransitTime(){
		return this.datasetTransitTime;
	}
	
	
	private void printList(List<Double> list, String name) {
		System.out.println(name);
		for(int i =0; i< list.size(); i++) {
			System.out.println(list.get(i));
		}
	}
	
	private XYSeries listToXYSeries(List<Double> data, List<Double> time, String title) {
		//System.out.println("data size: "+data.size());
		//System.out.println("temps size: "+time.size());

		if(data.size() != time.size()) {
			System.err.println("erreur : nombre de data !=  du nombre de temps");
		}
		
		XYSeries serie = new XYSeries(title);
		for(int i = 0; i< time.size(); i++) {
			serie.add( time.get(i),data.get(i));
		}
		return serie;
	}
}
