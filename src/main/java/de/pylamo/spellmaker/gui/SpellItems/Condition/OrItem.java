package de.pylamo.spellmaker.gui.SpellItems.Condition;

import de.pylamo.spellmaker.gui.SimpleDragObject;
import de.pylamo.spellmaker.gui.SpellItems.ImageMover;
import de.pylamo.spellmaker.gui.SpellItems.Parameter.IParameter;
import de.pylamo.spellmaker.gui.SpellItems.Parameter.Parameter;
import de.pylamo.spellmaker.gui.SpellItems.Parameter.ParameterSlot;
import de.pylamo.spellmaker.gui.SpellItems.ToolTipItem;
import de.pylamo.spellmaker.gui.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;

public class OrItem extends IParameter {
    private static final long serialVersionUID = 1L;

    public final ParameterSlot lefthand;
    public final ParameterSlot righthand;
    Window w;


    public IParameter clone() {
        OrItem oi = new OrItem(preview, w);
        if (lefthand.content != null) {
            oi.lefthand.add(lefthand.content.clone());
            oi.lefthand.content = lefthand.content.clone();
        }
        if (righthand.content != null) {
            oi.righthand.add(righthand.content.clone());
            oi.righthand.content = righthand.content.clone();
        }
        return oi;
    }

    public OrItem(boolean preview, Window w) {
        super(preview);
        this.w = w;
        FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 1, 1);
        this.setLayout(fl);
        lefthand = new ParameterSlot(Parameter.Condition, "lefthand", preview, w);
        this.add(lefthand);
        JLabel la = new JLabel(" OR ");
        this.add(la);
        righthand = new ParameterSlot(Parameter.Condition, "righthand", preview, w);
        this.add(righthand);
        ToolTipItem tti = new ToolTipItem();
        tti.setToolTipText("<html>This is an if item, if the specified condition <br> is fulfilled, the following block will be executed</html>");
        this.add(tti);
        this.setBackground(new Color(108, 45, 199));
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        if (preview) {
            DragSource ds = new DragSource();
            ParameterDragGestureListener pdgl = new ParameterDragGestureListener();
            ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, pdgl);
            ds.addDragSourceMotionListener(pdgl);
            ds.addDragSourceListener(new DragSourceListener() {
                @Override
                public void dropActionChanged(DragSourceDragEvent dsde) {
                }

                @Override
                public void dragOver(DragSourceDragEvent dsde) {
                }

                @Override
                public void dragExit(DragSourceEvent dse) {
                }

                @Override
                public void dragEnter(DragSourceDragEvent dsde) {
                }

                @Override
                public void dragDropEnd(DragSourceDropEvent dsde) {
                    ImageMover.stop();
                }
            });
        }
    }

    @Override
    public void revalidate() {
        super.revalidate();
        if (this.getParent() != null && this.getParent() instanceof JComponent) {
            ((JComponent) this.getParent()).revalidate();
        }
    }

    @Override
    public String getString() {
        String left = "";
        if (lefthand.content != null) {
            left = lefthand.content.getString();
        }
        String right = "";
        if (righthand.content != null) {
            right = righthand.content.getString();
        }
        return left + " || " + right;
    }

    private class ParameterDragGestureListener implements DragGestureListener, DragSourceMotionListener {
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            Cursor cursor = null;
            BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            paint(bi.getGraphics());
            ImageMover.start(bi, MouseInfo.getPointerInfo().getLocation());
            String s = "[ORITEM]";
            if (dge.getDragAction() == DnDConstants.ACTION_COPY) {
                cursor = DragSource.DefaultCopyDrop;
            }
            dge.startDrag(cursor, new SimpleDragObject.TransferableSimpleDragObject(new SimpleDragObject(s)));
        }

        @Override
        public void dragMouseMoved(DragSourceDragEvent dsde) {
            ImageMover.w.setLocation(new Point(dsde.getLocation().x + 2, dsde.getLocation().y + 4));
        }
    }
}