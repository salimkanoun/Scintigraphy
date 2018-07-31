package org.petctviewer.scintigraphy.esophageus.resultats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;

public class Modele_Resultats_EsophagealTransit {

	private XYSeries [][] datasetInitial;
	private XYSeries [][] datasetCurrent;

	public Modele_Resultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList) {

	
	

				
		// x examen et 4 coubres
		datasetInitial = new XYSeries[arrayList.size()][4];
		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetInitial[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "entier "+i);
			datasetInitial[i][1] = this.listToXYSeries(arrayList.get(i).get("unTier"), arrayList.get(i).get("temps"), "un tier "+i);
			datasetInitial[i][2] = this.listToXYSeries(arrayList.get(i).get("deuxTier"), arrayList.get(i).get("temps"), "deux tier "+i);
			datasetInitial[i][3] = this.listToXYSeries(arrayList.get(i).get("troisTier"), arrayList.get(i).get("temps"), "trois tier "+i);
		}
		
		datasetCurrent = new XYSeries[arrayList.size()][4];

		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetCurrent[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "entier "+i);
			datasetCurrent[i][1] = this.listToXYSeries(arrayList.get(i).get("unTier"), arrayList.get(i).get("temps"), "un tier "+i);
			datasetCurrent[i][2] = this.listToXYSeries(arrayList.get(i).get("deuxTier"), arrayList.get(i).get("temps"), "deux tier "+i);
			datasetCurrent[i][3] = this.listToXYSeries(arrayList.get(i).get("troisTier"), arrayList.get(i).get("temps"), "trois tier "+i);
		}
	}
	
	public XYSeries[][] getDataSet() {
		return this.datasetCurrent;
	}
	
	 public void actualiserDatasetFromCheckbox(int x, int y, boolean visibility) {
		 System.out.println("x:"+x+" y:"+y+" v: "+visibility);
		 if(visibility) {
			 datasetCurrent[x][y] = datasetInitial[x][y];
		 }else {
			 datasetCurrent[x][y] = new XYSeries(x+"|"+y);
		 }
	 }
	
	private void printList(List<Double> list, String name) {
		System.out.println(name);
		for(int i =0; i< list.size(); i++) {
			System.out.println(list.get(i));
		}
	}
	
	private XYSeries listToXYSeries(List<Double> data, List<Double> time, String title) {
		System.out.println("data size: "+data.size());
		System.out.println("temps size: "+time.size());

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
