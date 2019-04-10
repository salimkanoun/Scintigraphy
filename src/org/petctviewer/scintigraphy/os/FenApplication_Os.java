package org.petctviewer.scintigraphy.os;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

public class FenApplication_Os extends JPanel implements ChangeListener{
	
	private JSlider slider;
	private Scintigraphy scin;
	private JLabel sliderLabel;
	protected Box boxSlider;
	
	private JPanel grid;
	
	private ImagePlus[][] imps;
	DynamicImage[][] dynamicImps;
	
	boolean[][] selected;
	
	
	private ImagePlus imp;
	private DynamicImage dynamicImp;
	
	protected SidePanel sidePanel;
	String additionalInfo, nomFen;
	
	private int nbScinty;
	
	
	
	public FenApplication_Os(OsScintigraphy scin, ImagePlus[][] img) {
		super(new BorderLayout());
		
		this.setScin(scin);
		
		this.nbScinty = img.length;
		
		this.additionalInfo = "Info";
		this.nomFen = "Fen";
		
		sidePanel = new SidePanel(null, "Bone scintigraphy", scin.getImp());
		JButton b = new JButton("Inverser");
		ActionListener ad = new inverser();
		b.addActionListener(ad);
		sidePanel.add(b);
		// sidePanel.addCaptureBtn(scin, "_other");
		this.add(sidePanel, BorderLayout.WEST);
		
		this.grid = new JPanel(new GridLayout(1, nbScinty*2));
		
		this.dynamicImps = new DynamicImage[nbScinty][2];
		this.imps = new ImagePlus[nbScinty][2];
		this.imps = img;
		this.imp = this.imps[0][0];
		
		this.add(grid, BorderLayout.CENTER);
		
		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		this.finishBuildingWindow();
	}

	
	
	
	
	
	public void finishBuildingWindow() {
		System.out.println("ok");
		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) imp.getStatistics().max, 4);
		slider.addChangeListener(this);
		
		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);

		// BufferedImage img = this.imp.getBufferedImage();
		
		for(int i = 0; i<nbScinty ; i++){
			for (int j=0 ; j<2 ; j++) {
				// System.out.println("Fen_Os 5.");
				if (this.dynamicImps[i][j] == null) {
					BufferedImage imgbuffered = this.imps[i][j].getBufferedImage();
					this.dynamicImps[i][j] = new DynamicImage(imgbuffered);
					this.dynamicImps[i][j].addMouseListener(new MouseAdapter() {
						@Override
				         public void mousePressed(MouseEvent e) {
				        	JPanel di = (JPanel)e.getSource();
				     		for (int i =0 ;i<nbScinty;i++) {
				     			for (int j = 0;j<2;j++) {
				     				if(di == dynamicImps[i][j]){
				     					imp = imps[i][j];
				     					dynamicImp = dynamicImps[i][j];
				     					// perform(dynamicImps[i][j],i,j);
				     				}
				     			}
				     		}
				     		slider.setValue((int) ((slider.getModel().getMaximum() - imp.getLuts()[0].max)+1));
				     	}
				      });
					this.setContrast(this.slider.getValue());
					grid.add(dynamicImps[i][j]);
				}
				
			}
		}
		
		this.dynamicImp = this.dynamicImps[0][0];
		this.setContrast(slider.getValue());
		sidePanel.add(boxSlider);
		// sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] { this.slider });
		this.add(sidePanel, BorderLayout.WEST);
	}
	
	public ImagePlus getImagePlus() {
		return this.imp;
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

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow();
	}


	@Override
	public void stateChanged(ChangeEvent e) {	
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
	}
	
	public void setScin(Scintigraphy scin) {
		this.scin = scin;
	}
	
	
	
	private void setContrast(int sliderValue) {
		
		for(int i = 0; i<nbScinty ; i++){
			for (int j=0 ; j<2 ; j++) {
				if(isSelected(imps[i][j])) {
					imps[i][j].getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - sliderValue)+1);
				}
			}
		}
		
		//imp.getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - sliderValue)+1);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				for(int i = 0; i<nbScinty ; i++){
					for (int j=0 ; j<2 ; j++) {
						if(isSelected(dynamicImps[i][j])) {
							dynamicImps[i][j].setImage(imp.getBufferedImage());
							dynamicImps[i][j].repaint();
						}
					}
				}
				// dynamicImp.setImage(imp.getBufferedImage());
				// dynamicImp.repaint();
			}
		});
	}

	
	
	
	
	
	public class inverser implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			for(int i = 0; i<nbScinty ; i++){
				for (int j=0 ; j<2 ; j++) {
					imps[i][j].setLut(imps[i][j].getLuts()[0].createInvertedLut());
					dynamicImps[i][j].setImage(imps[i][j].getBufferedImage());
					dynamicImps[i][j].repaint();
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public void  perform(DynamicImage dyn) {
		if(selected[position(dyn)[0]][position(dyn)[1]]) {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 0, 0, Color.red));
			selected[position(dyn)[0]][position(dyn)[1]] = false;
			
		}else {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    3, 3, 3, 3, Color.red));
			selected[position(dyn)[0]][position(dyn)[1]] = true;
		}
	}
	
	public void  perform(DynamicImage dyn,int i, int j) {
		if(selected[i][j]) {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 0, 0, Color.red));
			selected[i][j] = false;
			
		}else {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    3, 3, 3, 3, Color.red));
			selected[i][j] = true;
		}
	}
	
	
	
	public int[] position(DynamicImage dyn) {
		int[] location = new int[2];
		for (int i =0 ;i<nbScinty;i++) {
 			for (int j = 0;j<2;j++) {
 				if(dyn == dynamicImps[i][j]){
 					location[0] = i;
 					location[1] = j;
 				}
 			}	
 		}
		return location;
	}
	
	public int[] position(ImagePlus image) {
		int[] location = new int[2];
		for (int i =0 ;i<nbScinty;i++) {
 			for (int j = 0;j<2;j++) {
 				if(image == imps[i][j]){
 					location[0] = i;
 					location[1] = j;
 				}
 			}	
 		}
		return location;
	}
	
	
	
	public boolean isSelected(DynamicImage dyn) {
		return this.selected[position(dyn)[0]][position(dyn)[1]];
	}
	
	public boolean isSelected(DynamicImage dyn,int i, int j) {
		return this.selected[i][j];
	}
	
	public boolean isSelected(ImagePlus imp) {
		return this.selected[position(imp)[0]][position(imp)[1]];
	}
	
	public boolean isSelected(ImagePlus imp,int i, int j) {
		return this.selected[i][j];
	}
}
