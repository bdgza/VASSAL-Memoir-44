/*
*
* Copyright (c) 2004 by Rodney Kinney
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public
* License (LGPL) as published by the Free Software Foundation.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public
* License along with this library; if not, copies are available
* at http://www.opensource.org.
*/
package memoir44;

import java.util.List;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.menu.ChildProxy;
import VASSAL.tools.menu.MenuManager;
import VASSAL.tools.menu.MenuProxy;

/**
* Defines a saved game that is accessible from the File menu.
* The game will be loaded in place of a normal New Game
*/
public class M44ParentMenu extends AbstractConfigurable implements GameComponent {
 public static final String NAME = "name"; //$NON-NLS-1$
 
 protected MenuProxy menu;

 public M44ParentMenu() {
   menu = new MenuProxy();
 }

 @Override
 public String[] getAttributeDescriptions() {
   return new String[]{
     Resources.getString(Resources.NAME_LABEL)
   };
 }

 @Override
 public Class<?>[] getAttributeTypes() {
   return new Class<?>[]{
     String.class
   };
 }

 @Override
 public String[] getAttributeNames() {
   return new String[]{
     NAME
   };
 }

 @Override
 public String getAttributeValueString(String key) {
   if (NAME.equals(key)) {
     return getConfigureName();
   }
   else {
     return null;
   }
 }

 @Override
 public void setAttribute(String key, Object value) {
   if (NAME.equals(key)) {
     setConfigureName((String) value);
     menu.setText((String) value);
   }
 }

 @Override
 public VisibilityCondition getAttributeVisibility(String name) {
   return super.getAttributeVisibility(name);
 }

 public ChildProxy<?> getMenuInUse() {
   return menu;
 }

 @Override
 public void addTo(Buildable parent) {
   if (parent instanceof GameModule) {
     MenuManager.getInstance().addToSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
   }
   else if (parent instanceof M44ParentMenu) {
     final M44ParentMenu setup = (M44ParentMenu) parent;
     setup.menu.add(getMenuInUse());
   }
   
   GameModule.getGameModule().getGameState().addGameComponent(this);
 }

 @Override
 public void removeFrom(Buildable parent) {
   if (parent instanceof GameModule) {
     MenuManager.getInstance()
       .removeFromSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
   }
   else if (parent instanceof M44ParentMenu) {
     final M44ParentMenu setup = (M44ParentMenu) parent;
     setup.menu.remove(getMenuInUse());
   }
   GameModule.getGameModule().getGameState().removeGameComponent(this);
 }

 @Override
 public Class<?>[] getAllowableConfigureComponents() {
   return new Class<?>[]{M44ParentMenu.class};
 }

 public static String getConfigureTypeName() {
   return "M44 Parent Menu";
 }

 @Override
 public HelpFile getHelpFile() {
   return HelpFile.getReferenceManualPage("GameModule.html", "PredefinedSetup"); //$NON-NLS-1$ //$NON-NLS-2$
 }

 public boolean isMenu() {
   return true;
 }

 @Override
 public Command getRestoreCommand() {
   return null;
 }

 @Override
 public void setup(boolean gameStarting) {
   
 }

 @Override
 public String toString() {
   return "M44ParentMenu{" + //NON-NLS
     "name='" + name + '\'' + //NON-NLS
     ", menu='" + true + '\'' + //NON-NLS
     '}';
 }

 /**
  * {@link VASSAL.search.SearchTarget}
  * @return a list of the Configurable's string/expression fields if any (for search)
  */
 @Override
 public List<String> getExpressionList() {
   return List.of(name);
 }
}
 