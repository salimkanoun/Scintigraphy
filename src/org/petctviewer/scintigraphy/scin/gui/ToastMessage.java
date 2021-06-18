package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ToastMessage extends JFrame {

    public ToastMessage(final String msg) {
        this.setUndecorated(true);
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(240, 240, 240, 250));
        this.setLocationRelativeTo(null);
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        container.add(new JLabel(msg), BorderLayout.CENTER);
        this.add(container);
        this.pack();

        this.setShape(new RoundRectangle2D.Double(0, 0, this.getWidth(),
                                this.getHeight(), 20, 20));
    }

    public void display(int time) {
        Thread t = new Thread(() -> SwingUtilities.invokeLater(() -> {
            try {
                ToastMessage.this.setOpacity(1);
                ToastMessage.this.setVisible(true);
                Thread.sleep(time);

                for (float f=1; f > 0.2; f-=0.1) {
                    Thread.sleep(100);
                    ToastMessage.this.setOpacity(f);
                }
                ToastMessage.this.setVisible(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        t.start();
    }

    public void display() {
        this.display(2000);
    }
}
