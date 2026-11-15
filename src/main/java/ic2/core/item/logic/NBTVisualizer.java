package ic2.core.item.logic;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NBTVisualizer {
   public static JFrame load(Supplier<CompoundTag> provider, final Runnable run) {
      JFrame frame = new JFrame("NBTVisualizer");
      frame.addWindowListener(new WindowListener() {
         @Override
         public void windowOpened(WindowEvent e) {
         }

         @Override
         public void windowIconified(WindowEvent e) {
         }

         @Override
         public void windowDeiconified(WindowEvent e) {
         }

         @Override
         public void windowDeactivated(WindowEvent e) {
         }

         @Override
         public void windowClosing(WindowEvent e) {
         }

         @Override
         public void windowClosed(WindowEvent e) {
            run.run();
         }

         @Override
         public void windowActivated(WindowEvent e) {
         }
      });
      JTree tree = new JTree(new DefaultMutableTreeNode("Root: 0 Entries"));
      frame.setDefaultCloseOperation(2);
      frame.setSize(700, 480);
      frame.setLocationRelativeTo(null);
      JPanel contentPane = new JPanel(new BorderLayout());
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      frame.setContentPane(contentPane);
      JScrollPane scrollPane = new JScrollPane();
      contentPane.add(scrollPane, "Center");
      scrollPane.setViewportView(tree);
      JMenuBar menuBar = new JMenuBar();
      frame.setJMenuBar(menuBar);
      JMenuItem menu = menuBar.add(new JMenu("Base")).add("Reload");
      menu.addActionListener(T -> EventQueue.invokeLater(() -> {
            tree.setModel(new DefaultTreeModel(readMap(provider.get(), "")));
            tree.updateUI();
         }));
      menu.setAccelerator(KeyStroke.getKeyStroke('r'));
      EventQueue.invokeLater(() -> {
         tree.setModel(new DefaultTreeModel(readMap(provider.get(), "")));
         tree.expandRow(0);
         tree.updateUI();
      });
      frame.setVisible(true);
      return frame;
   }

   public static DefaultMutableTreeNode readMap(CompoundTag map, String prefix) {
      if (map == null) {
         return new DefaultMutableTreeNode("0 Entries");
      } else {
         ByteArrayOutputStream out = new ByteArrayOutputStream();

         try {
            NbtIo.write(map, new DataOutputStream(out));
         } catch (Exception var8) {
         }

         DefaultMutableTreeNode node = new DefaultMutableTreeNode(prefix + map.size() + " Entries: " + out.size() + " Bytes");

         for (String key : map.getAllKeys()) {
            Tag tag = Objects.requireNonNull(map.get(key));
            if (tag instanceof ListTag) {
               node.add(readList((ListTag)tag, key + ": "));
            } else if (tag instanceof CompoundTag) {
               node.add(readMap((CompoundTag)tag, key + ": "));
            } else if ((tag.getId() < 1 || tag.getId() > 6) && tag.getId() != 8) {
               if (tag.getId() == 7 || tag.getId() == 11 || tag.getId() == 12) {
                  DefaultMutableTreeNode subNode = readArray(tag, key + ": ");
                  if (subNode != null) {
                     node.add(subNode);
                  }
               }
            } else {
               DefaultMutableTreeNode subNode = readValue(tag, key + ": ");
               if (subNode != null) {
                  node.add(subNode);
               }
            }
         }

         return node;
      }
   }

   public static DefaultMutableTreeNode readList(ListTag list, String prefix) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(prefix + list.size() + " Entries");

      for (int i = 0; i < list.size(); i++) {
         Tag tag = list.get(i);
         if (tag instanceof ListTag) {
            node.add(readList((ListTag)tag, ""));
         } else if (tag instanceof CompoundTag) {
            node.add(readMap((CompoundTag)tag, ""));
         } else if ((tag.getId() < 1 || tag.getId() > 6) && tag.getId() != 8) {
            if (tag.getId() == 7 || tag.getId() == 11 || tag.getId() == 12) {
               DefaultMutableTreeNode subNode = readArray(tag, "");
               if (subNode != null) {
                  node.add(subNode);
               }
            }
         } else {
            DefaultMutableTreeNode subNode = readValue(tag, "");
            if (subNode != null) {
               node.add(subNode);
            }
         }
      }

      return node;
   }

   public static DefaultMutableTreeNode readArray(Tag tag, String prefix) {
      if (tag instanceof ByteArrayTag) {
         return new DefaultMutableTreeNode(prefix + ByteArrayList.wrap(((ByteArrayTag)tag).getAsByteArray()) + ", (Byte Array)");
      } else if (tag instanceof IntArrayTag) {
         return new DefaultMutableTreeNode(prefix + IntArrayList.wrap(((IntArrayTag)tag).getAsIntArray()) + ", (Int Array)");
      } else {
         return tag instanceof LongArrayTag
            ? new DefaultMutableTreeNode(prefix + LongArrayList.wrap(((LongArrayTag)tag).getAsLongArray()) + ", (Long Array)")
            : null;
      }
   }

   public static DefaultMutableTreeNode readValue(Tag tag, String prefix) {
      if (tag instanceof ByteTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsByte() + ", (Byte)");
      } else if (tag instanceof ShortTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsShort() + ", (Short)");
      } else if (tag instanceof IntTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsInt() + ", (Int)");
      } else if (tag instanceof LongTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsLong() + ", (Long)");
      } else if (tag instanceof FloatTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsFloat() + ", (Float)");
      } else if (tag instanceof DoubleTag) {
         return new DefaultMutableTreeNode(prefix + ((NumericTag)tag).getAsDouble() + ", (Double)");
      } else {
         return tag instanceof StringTag ? new DefaultMutableTreeNode(prefix + tag.getAsString() + ", (String)") : null;
      }
   }
}
