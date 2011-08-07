package buoy.widget;

import buoy.xml.*;
import buoy.xml.delegate.*;
import javax.swing.*;

/**
 * A BRadioButtonMenuItem is a menu item for selecting between several options.  It is normally used as part
 * of a {@link RadioButtonGroup RadioButtonGroup}.  Each BRadioButtonMenuItem represents a single option.  Choosing it from the menu
 * selects it, and deselects all other members of its RadioButtonGroup.
 * <p>
 * In addition to the event types generated by all Widgets, BRadioButtonMenuItems generate the following event types:
 * <ul>
 * <li>{@link buoy.event.CommandEvent CommandEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */

public class BRadioButtonMenuItem extends BMenuItem
{
  private RadioButtonGroup group;
  
  static
  {
    WidgetEncoder.setPersistenceDelegate(BRadioButtonMenuItem.class, new EventSourceDelegate(new String [] {"group"}));
  }

  /**
   * Create a new BRadioButtonMenuItem with no label, which is initially deselected.
   *
   * @param group     the RadioButtonGroup this menu item is part of
   */
  
  public BRadioButtonMenuItem(RadioButtonGroup group)
  {
    this(null, null, null, false, group);
  }

  /**
   * Create a new BRadioButtonMenuItem.
   *
   * @param text      the text to display on the BRadioButtonMenuItem
   * @param state     the initial selection state of the BRadioButtonMenuItem
   * @param group     the RadioButtonGroup this menu item is part of
   */
  
  public BRadioButtonMenuItem(String text, boolean state, RadioButtonGroup group)
  {
    this(text, null, null, state, group);
  }

  /**
   * Create a new BRadioButtonMenuItem.
   *
   * @param text      the text to display on the BRadioButtonMenuItem
   * @param image     the image to display next to the menu item
   * @param state     the initial selection state of the BRadioButtonMenuItem
   * @param group     the RadioButtonGroup this menu item is part of
   */
  
  public BRadioButtonMenuItem(String text, Icon image, boolean state, RadioButtonGroup group)
  {
    this(text, null, image, state, group);
  }

  /**
   * Create a new BRadioButtonMenuItem.
   *
   * @param text      the text to display on the BRadioButtonMenuItem
   * @param shortcut  a keyboard shortcut which will activate this menu item
   * @param state     the initial selection state of the BRadioButtonMenuItem
   * @param group     the RadioButtonGroup this menu item is part of
   */
  
  public BRadioButtonMenuItem(String text, Shortcut shortcut, boolean state, RadioButtonGroup group)
  {
    this(text, shortcut, null, state, group);
  }

  /**
   * Create a new BRadioButtonMenuItem.
   *
   * @param text      the text to display on the BRadioButtonMenuItem
   * @param shortcut  a keyboard shortcut which will activate this menu item
   * @param image     the image to display next to the menu item
   * @param state     the initial selection state of the BRadioButtonMenuItem
   * @param group     the RadioButtonGroup this menu item is part of
   */
  
  public BRadioButtonMenuItem(String text, Shortcut shortcut, Icon image, boolean state, RadioButtonGroup group)
  {
    super(text, shortcut, image);
    this.group = group;
    group.add(this);
    setState(state);
  }
  
  /**
   * Create the JRadioButtonMenuItem which serves as this Widget's Component.  This method is protected so that
   * subclasses can override it.
   */
  
  protected JRadioButtonMenuItem createComponent()
  {
    return new JRadioButtonMenuItem();
  }

  public JRadioButtonMenuItem getComponent()
  {
    return (JRadioButtonMenuItem) component;
  }

  /**
   * Get the selection state of this menu item.
   */
  
  public boolean getState()
  {
    return getComponent().isSelected();
  }
  
  /**
   * Set the selection state of this menu item.  If you set the selection state to true,
   * the state of every other member of its RadioButtonGroup will be set to false.
   */
  
  public void setState(boolean selected)
  {
    if (selected)
      group.setSelection(this);
    else
      getComponent().setSelected(selected);
  }
  
  /**
   * Get the RadioButtonGroup this menu item is part of.
   */
  
  public RadioButtonGroup getGroup()
  {
    return group;
  }
  
  /**
   * Set the RadioButtonGroup this menu item is part of.
   */
  
  public void setGroup(RadioButtonGroup newGroup)
  {
    for (int i = 0; i < group.getRadioButtonCount(); i++)
      if (group.getRadioButton(i) == this)
      {
        group.remove(i);
        break;
      }
    group = newGroup;
    group.add(this);
  }
}
