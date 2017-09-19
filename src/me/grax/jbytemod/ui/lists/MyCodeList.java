package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.dialogue.EditDialogue;
import me.grax.jbytemod.utils.list.InstrEntry;

public class MyCodeList extends JList<InstrEntry> {
  private JLabel editor;
  private AdressList adressList;

  public MyCodeList(JByteMod jam, JLabel editor) {
    super(new DefaultListModel<InstrEntry>());
    this.editor = editor;
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        InstrEntry entry = (InstrEntry) MyCodeList.this.getSelectedValue();
        if(entry == null) return;
        MethodNode mn = entry.getMethod();
        AbstractInsnNode ain = entry.getInstr();
        if (SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu menu = new JPopupMenu();
          JMenuItem insert = new JMenuItem("Insert after");
          insert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              try {
                EditDialogue.createInsertInsnDialog(mn, ain);
              } catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
          menu.add(insert);
          JMenuItem edit = new JMenuItem("Edit");
          edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              try {
                EditDialogue.createEditInsnDialog(mn, ain);
              } catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
          menu.add(edit);
          JMenuItem duplicate = new JMenuItem("Duplicate");
          duplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              try {
                if (ain instanceof LabelNode) {
                  mn.instructions.insert(ain, new LabelNode());
                } else {
                  mn.instructions.insert(ain, ain.clone(new HashMap<>()));
                }
                MyCodeList.this.loadInstructions(mn);

              } catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
          menu.add(duplicate);
          JMenuItem up = new JMenuItem("Move up");
          up.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              AbstractInsnNode node = ain.getPrevious();
              mn.instructions.remove(node);
              mn.instructions.insert(ain, node);
              MyCodeList.this.loadInstructions(mn);
            }
          });
          menu.add(up);
          JMenuItem down = new JMenuItem("Move down");
          down.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              AbstractInsnNode node = ain.getNext();
              mn.instructions.remove(node);
              mn.instructions.insertBefore(ain, node);
              MyCodeList.this.loadInstructions(mn);
            }
          });
          menu.add(down);
          JMenuItem remove = new JMenuItem("Remove");
          remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              mn.instructions.remove(ain);
              MyCodeList.this.loadInstructions(mn);
            }
          });
          menu.add(remove);
          menu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
              MyCodeList.this.setFocusable(true);
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
              MyCodeList.this.setFocusable(true);
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
              MyCodeList.this.setFocusable(false);
            }
          });
          menu.show(JByteMod.instance, (int) JByteMod.instance.getMousePosition().getX(),
              (int) JByteMod.instance.getMousePosition().getY());
        }
      }
    });
  }

  public boolean loadInstructions(MethodNode m) {
    DefaultListModel<InstrEntry> lm = new DefaultListModel<InstrEntry>();
    editor.setText(m.name + m.desc);
    ArrayList<InstrEntry> entries = new ArrayList<>();
    for (AbstractInsnNode i : m.instructions) {
      InstrEntry entry = new InstrEntry(m, i);
      lm.addElement(entry);
      entries.add(entry);
    }
    this.setModel(lm);
    //update sidebar
    if (adressList != null) {
      adressList.updateAdr();
    }
    return true;
  }

  public void setAdressList(AdressList adressList) {
    this.adressList = adressList;
  }

}
