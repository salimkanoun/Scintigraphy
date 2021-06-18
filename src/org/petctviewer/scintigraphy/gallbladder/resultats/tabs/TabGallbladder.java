package org.petctviewer.scintigraphy.gallbladder.resultats.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jfree.chart.*;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.Unit;

import static org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder.RES_GALLBLADDER;

public class TabGallbladder extends TabResult {

    private final BufferedImage capture;

    public TabGallbladder(FenResults parent, BufferedImage capture){
        super(parent, "Gallbladder", true);
        this.capture = capture;
        this.setAdditionalInfo("Gallbladder");
        this.reloadDisplay();
    }

    private ModelGallbladder getModel(){ return (ModelGallbladder) this.parent.getModel();}



    private void setVisibilitySeriesGraph(JFreeChart graph, int numSerie, boolean visibility){
        XYItemRenderer renderer = graph.getXYPlot().getRenderer();
        renderer.setSeriesVisible(numSerie, visibility);
        renderer.setSeriesPaint(numSerie, Color.red);
    }


    /**
     * @return component
     */
    @Override
    public Component getSidePanelContent(){
        this.getResultContent();

        JPanel flow_wrap = new JPanel();
        Box panRes = Box.createVerticalBox();

        //Ejection fraction

        ResultRequest request = new ResultRequest(RES_GALLBLADDER);

        request.changeResultOn(RES_GALLBLADDER);
        request.setUnit(Unit.PERCENTAGE);
      // this.displayResult(this.getModel().getResult(request), res);
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getEjectionFraction());

        flow_wrap.add(panRes);

        return flow_wrap;
    }


    private Component getEjectionFraction(){
        ModelGallbladder model = (ModelGallbladder) this.parent.getModel();
        double results = model.getResults().get(RES_GALLBLADDER.hashCode());

        double ejection = Library_Quantif.round(results,2);
        JPanel pnl_ejfr = new JPanel(new GridLayout(1, 1, 0, 3));

        JLabel val_ejection = new JLabel("Ejection Fraction : " + ejection + " %");
        val_ejection.setFont(new Font("Arial", Font.BOLD, 15));
        val_ejection.setHorizontalAlignment(JLabel.CENTER);

        pnl_ejfr.add(val_ejection);

        return pnl_ejfr;

    }

    @Override
    public JPanel getResultContent() {
        JPanel grid = new JPanel(new GridLayout(2, 1));

        // ajout de la capture et du montage
        JPanel panel_top = new JPanel(new GridLayout(1, 2));
        panel_top.add(new DynamicImage(capture));
        grid.add(panel_top);

        //ajout du graphique image au gallblader
        List<XYSeries> series = ((ModelScinDyn) this.getParent().getModel()).getSeries();

        ChartPanel chart = Library_JFreeChart.associateSeries(new String[] {"Gallbladder"}, series);

        chart.getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
        grid.add(chart);

        return grid;

    }
}