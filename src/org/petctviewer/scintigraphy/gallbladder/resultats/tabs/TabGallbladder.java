package org.petctviewer.scintigraphy.gallbladder.resultats.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import ij.ImagePlus;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder;
import org.petctviewer.scintigraphy.gallbladder.resultats.Model_Resultats_Gallbladder;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import static org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder.RES_GALLBLADDER;

public class TabGallbladder extends TabResult{
    
    private JFreeChart graphGallbladder;

    private Selector selectorGallbladder;
    private double[] selectorGallbladderValue;

    private JRadioButton[] radioButtonGallbladder;

    private JLabel[] gallbladder10sLabel;
    private static int numAcquisitionGallbladder = 0;

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
     * @param result
     * @param container

    private void displayResult(ResultValue result, Container container){
        JLabel label = new JLabel(String.valueOf(result));

        //Color
        if(result.getResultType() == ModelGallbladder.RES_GALLBLADDER){
            if(result.getValue() < 2.) label.setForeground(Color.GREEN);
            else if (result.getValue() < 5.) label.setForeground(Color.magenta);
            else label.setForeground(Color.RED);
        }
        container.add(label);
    }
     */

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

       /* if(nbAcquisition == null)
            return null;

        JPanel radioButtonGallbladderPanel = new JPanel();
        radioButtonGallbladderPanel.setLayout(new GridLayout(nbAcquisition, 1));

        ButtonGroup buttonGroupGallbladder = new ButtonGroup();
        radioButtonGallbladder = new JRadioButton[nbAcquisition];
        for (int i = 0 ; i < nbAcquisition; i++){
            radioButtonGallbladder[i] = new JRadioButton("Acquisition " + (i + 1));
            radioButtonGallbladder[i].addItemListener(e -> {
                TabGallbladder tab = TabGallbladder.this;
                for(int i1 = 0; i1 < tab.radioButtonGallbladder.length; i1++){
                    if(e.getSource().equals(radioButtonGallbladder[i1])){
                        tab.setVisibilitySeriesGraph(tab.graphGallbladder, i1, true);
                        numAcquisitionGallbladder = i1;
                        tab.selectorGallbladder.setXValue(selectorGallbladderValue[i1]);

                    }else{
                        tab.setVisibilitySeriesGraph(tab.graphGallbladder, i1, false);
                    }
                }
            });
            buttonGroupGallbladder.add(radioButtonGallbladder[i]);
            radioButtonGallbladderPanel.add(radioButtonGallbladder[i]);
        }

        JPanel radioButtonGallbladderPanelFlow = new JPanel();
        radioButtonGallbladderPanelFlow.setLayout(new FlowLayout());
        radioButtonGallbladderPanelFlow.add(radioButtonGallbladderPanel);

        Box sidePanel = Box.createVerticalBox();
        SidePanel sidePanelScin = new SidePanel(null, modeleApp.gallPlugIn.getStudyName(), modeleApp.getImagePlus());
        sidePanel.add(sidePanelScin);
        sidePanel.add(radioButtonGallbladderPanelFlow);

        JPanel gallbladderResultPanel = new JPanel();
        gallbladderResultPanel.setLayout(new GridLayout(nbAcquisition +1, 1));
        gallbladderResultPanel.add(new JLabel("Decrease 10s after peak"));
        double[] gallbladder10s = modeleApp.retentionAllPoucentage();
        gallbladder10sLabel = new JLabel[nbAcquisition];
        for(int i = 0; i < gallbladder10s.length; i++){
            gallbladder10sLabel[i] = new JLabel("Acquisition " + (i+1) + " : " + (gallbladder10s[i]) + "%");
            gallbladderResultPanel.add(gallbladder10sLabel[i]);
        }

        JPanel gallbladderResultPanelFlow = new JPanel();
        gallbladderResultPanelFlow.setLayout(new FlowLayout());
        gallbladderResultPanelFlow.add(gallbladderResultPanel);

        sidePanel.add(gallbladderResultPanelFlow);

        radioButtonGallbladder[0].setSelected(true);

        return sidePanel;
        **/

    }

    /** 
	 * @return ModelThyroid
	 */
	//private ModelGallbladder getModel() {
	//	return (ModelGallbladder) this.parent.getModel();
  //  }

    private Component getEjectionFraction(){
        ModelGallbladder model = (ModelGallbladder) this.parent.getModel();
        double results = model.getResults().get(RES_GALLBLADDER.hashCode());

        double ejection = Library_Quantif.round(results,2);
        JPanel pnl_ejfr = new JPanel(new GridLayout(1, 2, 0, 3));
        pnl_ejfr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        pnl_ejfr.add(new JLabel(" Ejection Fraction :  "));

        JLabel val_ejection = new JLabel(String.valueOf(ejection) + " %");
        pnl_ejfr.add(val_ejection);



        return pnl_ejfr;


    }


   // @Override
    public JPanel getResultContent() {
        /*  //graph center
         graphGallbladder = ChartFactory.createXYLineChart("Gallbladder", "s", "Count/s", null);

         graphGallbladder.getXYPlot().setDataset(modeleApp.retentionForGraph());

         XYLineAndShapeRenderer rendererTransit = new XYLineAndShapeRenderer();

         //monter les formes des points
         rendererTransit.setSeriesShapesVisible(0, true);

         //pour avoir les infos des points quand on passe la sourie dessus
         rendererTransit.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
         graphGallbladder.getXYPlot().setRenderer(rendererTransit);

         //grille en noir
         this.graphGallbladder.getXYPlot().setRangeGridlinePaint(Color.black);
         this.graphGallbladder.getXYPlot().setDomainGridlinePaint(Color.black);

         selectorGallbladderValue = modeleApp.retentionAllX();

         JValueSetter valueSetterGallbladder = new JValueSetter(graphGallbladder);
         valueSetterGallbladder.addChartMouseListener(new ChartMouseListener(){

        @Override public void chartMouseMoved(ChartMouseEvent event) {
        }

        @Override public void chartMouseClicked(ChartMouseEvent event) {
        TabGallbladder tab = TabGallbladder.this;
        tab.selectorGallbladderValue[numAcquisitionGallbladder] = tab.selectorGallbladder.getXValue();

        double gallbladder = modeleApp.retentionPoucentage(tab.selectorGallbladder.getXValue(), numAcquisitionGallbladder);
        gallbladder10sLabel[numAcquisitionGallbladder].setText("Acquisition " + (numAcquisitionGallbladder +1 ) + " : " + gallbladder + "%");
        }
        });

         selectorGallbladder = new Selector("max", 1, -1, RectangleAnchor.TOP_RIGHT);
         valueSetterGallbladder.addSelector(selectorGallbladder, "max");

         //Hide every curves exept the first one
         for(int i = 1; i < nbAcquisition; i++){
         this.setVisibilitySeriesGraph(graphGallbladder, i, false);
         }

         return valueSetterGallbladder;
         **/
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