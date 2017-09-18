import ij.IJ;
import ij.Menus;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

	
	
public class Scintigraphy_Tool extends PlugInTool implements ActionListener {
	
	
		private final int OFFSET = 0;
		PopupMenu general;
		
		
		
		@Override
		public void showPopupMenu(MouseEvent e, Toolbar par) {
			addPopupMenu(par);
			general.show(e.getComponent(), e.getX()+OFFSET, e.getY()+OFFSET);
		}
		

		void addPopupMenu(Toolbar par) {
			//On cree le popup menu general
			general = new PopupMenu();
			//On cree les menus par organes
			PopupMenu gastric = new PopupMenu("Gastric");
			PopupMenu pulmonary = new PopupMenu("Pulmonary");
			
			if (Menus.getFontSize()!=0) general.setFont(Menus.getFont());
			
			MenuItem gastricEmptying = new MenuItem("Gastric Emptying");
			MenuItem gastricDynamic = new MenuItem("Gastric Dyamique");
			MenuItem gastricCondense = new MenuItem("Gastric Condense");
			
			//plumonary shunt
			MenuItem plumonaryShunt = new MenuItem("Pulmonary Shunt");
			
			//Ajout des listeners
			gastricEmptying.addActionListener(this);
			gastricDynamic.addActionListener(this);
			gastricCondense.addActionListener(this);
			plumonaryShunt.addActionListener(this);
			
			gastric.add(gastricEmptying);
			gastric.add(gastricDynamic);
			gastric.add(gastricCondense);
			
			pulmonary.add(plumonaryShunt);
			
			general.add(gastric);
			general.add(pulmonary);
			
			par.add(general);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd=="Gastric Emptying") {
				IJ.run("Gastric Emptying software");
			}
			if (cmd=="Gastric Dyamique") {
				IJ.run("Gastric Emptying Dynamic");
			}
			if (cmd=="Pulmonary Shunt") {
				IJ.run("Pulmonary Shunt");
			}
			if (cmd=="Gastric Condense") {
				Thread condense=new Thread (new Runnable() 
			    {
			      public void run()
			      {  
			    	  IJ.run("Gastric Condense");
			      }
			    });
			   condense.start();
			   
			}
			
		}
		

		@Override
		public String getToolIcon() {
			return "T05079 T55079 T7b11T Tbb11c";
		}

		@Override
		public String getToolName() {
			return "Scintigraphy Access, Right click to show menu";
		}
		
	}

