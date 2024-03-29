package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;

/**
 *  Support custom painting on a panel in the form of
 *
 *  a) images - that can be scaled, tiled or painted at original size
 *  b) non solid painting - that can be done by using a Paint object
 *
 *  Also, any component added directly to this panel will be made
 *  non-opaque so that the custom painting can show through.
 */
public class DynamicImage extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final int SCALED = 0, SCALED_KEEPRATIO = 3, TILED = 1, ACTUAL = 2; //affichage
	
	private final Object interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;

	private Paint painter;
	private Image image;
	private int style = SCALED;
	private float alignmentX = 0.5f;
	private float alignmentY = 0.5f;
	private boolean isTransparentAdd = true;
	

	/**
	 * Set image as the background with the SCALED style
	 */
	public DynamicImage(Image image) {
		this(image, SCALED_KEEPRATIO);
	}

	/**
	 * Set image as the background with the specified style
	 */
	public DynamicImage(Image image, int style) {
		setImage(image);
		setStyle(style);
		setLayout(new BorderLayout());
	}

	/**
	 * Set image as the background with the specified style and alignment
	 */
	public DynamicImage(Image image, int style, float alignmentX, float alignmentY) {
		setImage(image);
		setStyle(style);
		setImageAlignmentX(alignmentX);
		setImageAlignmentY(alignmentY);
		setLayout(new BorderLayout());
	}

	/**
	 * Use the Paint interface to paint a background
	 */
	public DynamicImage(Paint painter) {
		setPaint(painter);
		setLayout(new BorderLayout());
	}

	/**
	 * Set the image used as the background
	 */
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	/**
	 * Get Image of this panel to refresh
	 */
	public Image getImage() {
		return this.image;
	}
	
	/**
	 * Set the style used to paint the background image
	 */
	public void setStyle(int style) {
		this.style = style;
		repaint();
	}

	/**
	 * Set the Paint object used to paint the background
	 */
	public void setPaint(Paint painter) {
		this.painter = painter;
		repaint();
	}

	/**
	 * Specify the horizontal alignment of the image when using ACTUAL style
	 */
	public void setImageAlignmentX(float alignmentX) {
		this.alignmentX = alignmentX > 1.0f ? 1.0f : Math.max(alignmentX, 0.0f);
		repaint();
	}

	/**
	 * Specify the horizontal alignment of the image when using ACTUAL style
	 */
	public void setImageAlignmentY(float alignmentY) {
		this.alignmentY = alignmentY > 1.0f ? 1.0f : Math.max(alignmentY, 0.0f);
		repaint();
	}

	/**
	 * Override method so we can make the component transparent
	 */
	public void add(JComponent component) {
		add(component, null);
	}

	/**
	 * Override to provide a preferred size equal to the image size
	 */
	@Override
	public Dimension getPreferredSize() {
		if (image == null)
			return super.getPreferredSize();
		else
			return new Dimension(image.getWidth(null), image.getHeight(null));
	}

	/**
	 * Override method so we can make the component transparent
	 */
	public void add(JComponent component, Object constraints) {
		if (isTransparentAdd) {
			makeComponentTransparent(component);
		}

		super.add(component, constraints);
	}

	/**
	 * Controls whether components added to this panel should automatically be made
	 * transparent. That is, setOpaque(false) will be invoked. The default is set to
	 * true.
	 */
	public void setTransparentAdd(boolean isTransparentAdd) {
		this.isTransparentAdd = isTransparentAdd;
	}

	/**
	 * Try to make the component transparent. For components that use renderers,
	 * like JTable, you will also need to change the renderer to be transparent. An
	 * easy way to do this it to set the background of the table to a Color using an
	 * alpha value of 0.
	 */
	private void makeComponentTransparent(JComponent component) {
		component.setOpaque(false);

		if (component instanceof JScrollPane) {
			JScrollPane scrollPane = (JScrollPane) component;
			JViewport viewport = scrollPane.getViewport();
			viewport.setOpaque(false);
			Component c = viewport.getView();

			if (c instanceof JComponent) {
				((JComponent) c).setOpaque(false);
			}
		}
	}

	/**
	 * Add custom painting
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Invoke the painter for the background
		if (painter != null) {
			Dimension d = getSize();
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(painter);
			g2.fill(new Rectangle(0, 0, d.width, d.height));
		}

		if(interpolation != null) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
		}
		
		// Draw the image
		if (image == null)
			return;

		switch (style) {

			case TILED:
			drawTiled(g);
			break;

		case ACTUAL:
			drawActual(g);
			break;

		case SCALED_KEEPRATIO:
			drawScaledRatio(g);
			break;

		default:
			drawScaled(g);
		}
	}

	private void drawScaledRatio(Graphics g) {
		Dimension d = getSize();
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		
		int newHeight = (int) (h * (d.width / (w * 1.0)));
		int newWidth = (int) (w * (d.height / (h * 1.0)));
		
		if(newHeight < d.height) {
			g.drawImage(image, 0, (d.height - newHeight) / 2, d.width, newHeight, null);
		}else {
			g.drawImage(image, (d.width - newWidth) / 2, 0, newWidth, d.height, null);
		}		
	}

	/**
	 * Custom painting code for drawing a SCALED image as the background
	 */
	private void drawScaled(Graphics g) {
		Dimension d = getSize();
		g.drawImage(image, 0, 0, d.width, d.height, null);
	}

	/**
	 * Custom painting code for drawing TILED images as the background
	 */
	private void drawTiled(Graphics g) {
		Dimension d = getSize();
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		for (int x = 0; x < d.width; x += width) {
			for (int y = 0; y < d.height; y += height) {
				g.drawImage(image, x, y, null, null);
			}
		}
	}

	/**
	 * Custom painting code for drawing the ACTUAL image as the background. The
	 * image is positioned in the panel based on the horizontal and vertical
	 * alignments specified.
	 */
	private void drawActual(Graphics g) {
		Dimension d = getSize();
		Insets insets = getInsets();
		int width = d.width - insets.left - insets.right;
		int height = d.height - insets.top - insets.left;
		float x = (width - image.getWidth(null)) * alignmentX;
		float y = (height - image.getHeight(null)) * alignmentY;
		g.drawImage(image, (int) x + insets.left, (int) y + insets.top, this);
	}
}
