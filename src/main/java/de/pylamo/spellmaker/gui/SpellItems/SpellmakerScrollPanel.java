package de.pylamo.spellmaker.gui.SpellItems;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class SpellmakerScrollPanel extends JPanel {
    JScrollBar jsb = new JScrollBar();

    public SpellmakerScrollPanel(final Container c) {
        this.setLayout(new BorderLayout());
        this.add(c, BorderLayout.CENTER);
        jsb.setMinimum(0);
        jsb.setMaximum(60);
        this.add(jsb, BorderLayout.EAST);
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                float ratio = SpellmakerScrollPanel.this.getHeight() / c.getHeight();
                jsb.setMaximum(Math.round(ratio * 60));
                jsb.invalidate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
}