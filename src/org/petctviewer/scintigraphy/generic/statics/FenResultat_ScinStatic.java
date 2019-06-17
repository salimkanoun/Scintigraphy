package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultat_ScinStatic extends FenResults {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel panAnt;
	private final JPanel panPost;
	private final JPanel panAvgGeo;
	private final BufferedImage capture;

	private final TabResult tab;

	public FenResultat_ScinStatic(BufferedImage capture, ControllerScin controller) {
		super(controller);
		this.capture = capture;

		this.tab = new Tab(this, "Result");
		this.panAnt = new JPanel();
		this.panPost = new JPanel();
		this.panAvgGeo = new JPanel();
		
		if(!((ModelScinStatic) this.getModel()).isSingleSlice() || ((ModelScinStatic) this.getModel()).isAnt())
			this.addAntTab(((ModelScinStatic) controller.getModel()).calculerTableauAnt());
		if( !((ModelScinStatic) this.getModel()).isSingleSlice() || !((ModelScinStatic) this.getModel()).isAnt())
			this.addPostTab(((ModelScinStatic) controller.getModel()).calculerTableauPost());
		if(!((ModelScinStatic) this.getModel()).isSingleSlice())
			this.addMoyGeomTab(((ModelScinStatic) controller.getModel()).calculerTaleauMayGeom());

		this.addTab(tab);

		this.setLocationRelativeTo(controller.getModel().getImagePlus().getWindow());
	}

	private class Tab extends TabResult {

		public Tab(FenResults parent, String title) {
			super(parent, title, true);
//			this.createCaptureButton("cc");
			this.setAdditionalInfo("cc");

			this.reloadDisplay();
		}

		@Override
		public Component getSidePanelContent() {
			Box box = new Box(BoxLayout.PAGE_AXIS);
			if (panAnt != null)
				box.add(panAnt);
			if (panPost != null)
				box.add(panPost);
			if (panAvgGeo != null)
				box.add(panAvgGeo);
			return box;
		}

		@Override
		public JPanel getResultContent() {
			return new DynamicImage(capture);
		}

	}

	public void addAntTab(Object[][] data) {
		JTable table;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		String[] title = { "Name", "Count", "Avg", "Std" };
		table = new JTable(data, title);

		p.add(table.getTableHeader(), BorderLayout.NORTH);
		p.add(table, BorderLayout.CENTER);

		JPanel fixedSize = new JPanel(new FlowLayout());
		fixedSize.add(p);

		this.panAnt.add(new JLabel("Ant"));
		this.panAnt.add(fixedSize);

		this.tab.reloadDisplay();
	}

	public void addPostTab(Object[][] data) {
		JTable table;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		String[] title = { "Name", "Count", "Avg", "Std" };
		table = new JTable(data, title);

		p.add(table.getTableHeader(), BorderLayout.NORTH);
		p.add(table, BorderLayout.CENTER);

		JPanel fixedSize = new JPanel(new FlowLayout());
		fixedSize.add(p);

		this.panPost.add(new JLabel("Post"));
		this.panPost.add(fixedSize);

		this.tab.reloadDisplay();
	}
	// csv
	// 2 ant et post sur la cpture

	public void addMoyGeomTab(Object[][] data) {
		JTable table;
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		String[] title = { "Name", "Geom Mean" };
		table = new JTable(data, title);

		p.add(table.getTableHeader(), BorderLayout.NORTH);
		p.add(table, BorderLayout.CENTER);

		JPanel fixedSize = new JPanel(new FlowLayout());
		fixedSize.add(p);

		this.panAvgGeo.add(new JLabel("Geom Mean"));
		this.panAvgGeo.add(fixedSize);

		this.tab.reloadDisplay();
	}

}
