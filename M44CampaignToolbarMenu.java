package memoir44;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import VASSAL.build.module.ToolbarMenu;
import VASSAL.tools.LaunchButton;

public class M44CampaignToolbarMenu extends ToolbarMenu {
	protected void buildMenu() {
		if (menuItems.size() > 0) {
			String item = menuItems.get(0);
			Vector<JButton> buttons = new Vector<JButton>();

			for (AbstractButton b : buttonsToMenuMap.keySet()) {
				b.setVisible(true);
				b.removePropertyChangeListener(this);
			}
			buttonsToMenuMap.clear();
			menu.removeAll();
			
			if (toolbar != null) {
				for (int i = 0, n = toolbar.getComponentCount(); i < n; ++i) {
					if (toolbar.getComponentAtIndex(i) instanceof JButton) {
						JButton b = ((JButton) toolbar.getComponentAtIndex(i));
						String text =
							(String) b.getName();
						if (text == null)
							text = (String) b.getClientProperty(LaunchButton.UNTRANSLATED_TEXT);
						if (text == null)
							text = b.getText();

						if (text != null)
							if (text.startsWith(item))
								buttons.add(b);
					}
				}
			}

			if (buttons.size() == 0)
				removeFrom(null);
			else
				for (JButton button : buttons) {
					final JButton b = button;
					if (b != null) {
						Object property = b.getClientProperty(MENU_PROPERTY);
						b.addPropertyChangeListener(this);
						b.setVisible(false);
						if (property instanceof JPopupMenu) {
							// This button corresponds to another ToolbarMenu button.
							// Turn it into a submenu.
							JPopupMenu toolbarMenu = (JPopupMenu) property;
							toolbarMenu.addContainerListener(this);
							JMenu subMenu = new JMenu(b.getText());
							Component[] items = toolbarMenu.getComponents();
							for (int i = 0; i < items.length; i++) {
								final JMenuItem otherItem = (JMenuItem) items[i];
								JMenuItem myItem =
									new JMenuItem(otherItem.getText(), otherItem.getIcon());
								myItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										otherItem.doClick();
									}
								});
								subMenu.add(myItem);
								buttonsToMenuMap.put(otherItem, myItem);
							}
							buttonsToMenuMap.put(b, subMenu);
							menu.add(subMenu);
						}
						else {
							JMenuItem mi = new JMenuItem(b.getText(), b.getIcon());
							mi.setEnabled(b.isEnabled());
							mi.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									b.doClick();
								}
							});
							buttonsToMenuMap.put(b, mi);
							menu.add(mi);
						}
					}
				}
		}
	}
}
