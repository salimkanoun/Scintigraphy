package org.petctviewer.scintigraphy.view;

import ij.WindowManager;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class FenetreDialogue
  extends Frame
{
  private static final long serialVersionUID = 7249861393425869097L;
  private Label lbl_message;
  private Button btn_valider;
  private VueScin vue;
  
  public FenetreDialogue(String examType, VueScin vue)
  {
    this.vue = vue;
    
    Panel pan = new Panel();
    pan.setLayout(new GridLayout(2, 1));
    initBtnValider();
    this.lbl_message = new Label();
    this.lbl_message.setText("Please open the " + examType + " image(s) then confirm.");
    pan.add(this.lbl_message);
    pan.add(this.btn_valider);
    add(pan);
    
    setLocationRelativeTo(null);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(200, 300);
    pack();
    setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
    setVisible(true);
    setResizable(false);
    
    setAlwaysOnTop(true);
    toFront();
    
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent we)
      {
        System.gc();
        FenetreDialogue.this.dispose();
      }
    });
  }
  
  private void initBtnValider()
  {
    this.btn_valider = new Button("Confirm");
    this.btn_valider.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent arg0)
      {
        if (WindowManager.getCurrentImage() != null)
        {
          String[] titresFenetres = WindowManager.getImageTitles();
          FenetreDialogue.this.vue.ouvertureImage(titresFenetres);
          FenetreDialogue.this.dispose();
        }
        else
        {
          System.out.println("Pas de dicom ouverte");
        }
      }
    });
  }
}
