package net.wattpadpremium.amazinggame.client;

import javax.swing.*;
import java.awt.*;

public class ResizableDragListener {

    private static final int MARGIN = 8;
    private final JFrame frame;

    private int dragX, dragY;
    private int initialWidth, initialHeight;
    private int initialX, initialY;   // mouse screen position at press
    private int frameX, frameY;       // frame top-left position at press
    private int dragCursorType = Cursor.DEFAULT_CURSOR;

    public ResizableDragListener(JFrame frame) {
        this.frame = frame;
        this.frame.setMinimumSize(new Dimension(1280, 720));
        attach();
    }

    private void attach() {
        frame.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                frame.setCursor(Cursor.getPredefinedCursor(getCursorType(e)));
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Rectangle bounds = frame.getBounds();

                if (dragCursorType == Cursor.DEFAULT_CURSOR) {
                    // move — only allow drag if the initial click was in the title bar area
                    if (dragY <= 40) {
                        frame.setLocation(
                                e.getXOnScreen() - dragX,
                                e.getYOnScreen() - dragY
                        );
                    }
                } else {
                    // resize
                    int sx = e.getXOnScreen(), sy = e.getYOnScreen();
                    int dx = sx - initialX; // delta from press position (screen coords)
                    int dy = sy - initialY;

                    Dimension min = frame.getMinimumSize();
                    int minW = min.width, minH = min.height;

                    switch (dragCursorType) {
                        case Cursor.E_RESIZE_CURSOR ->
                                frame.setSize(Math.max(minW, initialWidth + dx), initialHeight);
                        case Cursor.S_RESIZE_CURSOR ->
                                frame.setSize(initialWidth, Math.max(minH, initialHeight + dy));
                        case Cursor.W_RESIZE_CURSOR -> {
                            int newW = Math.max(minW, initialWidth - dx);
                            frame.setBounds(frameX + initialWidth - newW, bounds.y,
                                    newW, bounds.height);
                        }
                        case Cursor.N_RESIZE_CURSOR -> {
                            int newH = Math.max(minH, initialHeight - dy);
                            frame.setBounds(bounds.x, frameY + initialHeight - newH,
                                    bounds.width, newH);
                        }
                        case Cursor.SE_RESIZE_CURSOR ->
                                frame.setSize(Math.max(minW, initialWidth + dx),
                                        Math.max(minH, initialHeight + dy));
                        case Cursor.SW_RESIZE_CURSOR -> {
                            int newW = Math.max(minW, initialWidth - dx);
                            frame.setBounds(frameX + initialWidth - newW, bounds.y,
                                    newW, Math.max(minH, initialHeight + dy));
                        }
                        case Cursor.NE_RESIZE_CURSOR -> {
                            int newH = Math.max(minH, initialHeight - dy);
                            frame.setBounds(bounds.x, frameY + initialHeight - newH,
                                    Math.max(minW, initialWidth + dx), newH);
                        }
                        case Cursor.NW_RESIZE_CURSOR -> {
                            int newW = Math.max(minW, initialWidth - dx);
                            int newH = Math.max(minH, initialHeight - dy);
                            frame.setBounds(frameX + initialWidth - newW,
                                    frameY + initialHeight - newH,
                                    newW, newH);
                        }
                    }
                }
            }
        });

        frame.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
                // Store screen position as anchor for resize deltas
                initialX = e.getXOnScreen();
                initialY = e.getYOnScreen();
                frameX = frame.getX();
                frameY = frame.getY();
                initialWidth = frame.getWidth();
                initialHeight = frame.getHeight();
                dragCursorType = getCursorType(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                dragCursorType = Cursor.DEFAULT_CURSOR;
            }
        });
    }

    private int getCursorType(java.awt.event.MouseEvent e) {
        int x = e.getX(), y = e.getY();
        int w = frame.getWidth(), h = frame.getHeight();
        boolean l = x < MARGIN, r = x > w - MARGIN;
        boolean t = y < MARGIN, b = y > h - MARGIN;

        if (l && t) return Cursor.NW_RESIZE_CURSOR;
        if (r && t) return Cursor.NE_RESIZE_CURSOR;
        if (l && b) return Cursor.SW_RESIZE_CURSOR;
        if (r && b) return Cursor.SE_RESIZE_CURSOR;
        if (l)      return Cursor.W_RESIZE_CURSOR;
        if (r)      return Cursor.E_RESIZE_CURSOR;
        if (t)      return Cursor.N_RESIZE_CURSOR;
        if (b)      return Cursor.S_RESIZE_CURSOR;
        return Cursor.DEFAULT_CURSOR;
    }
}