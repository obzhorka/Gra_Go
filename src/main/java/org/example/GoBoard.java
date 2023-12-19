package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GoBoard extends JPanel {
    private final int gridSize = 50;
    private final int numberOfSquares = 9; // Changed to 9x9 grid
    private final int boardSize = gridSize * numberOfSquares;
    private char token;

    private final ArrayList<Intersection> intersections = new ArrayList<>();

    public GoBoard() {
        setPreferredSize(new Dimension(boardSize, boardSize));
        addMouseListener(new IntersectionMouseListener());
    }

    public void addIntersection(int x, int y, Color color) {
        intersections.add(new Intersection(x, y, color));
        repaint();
    }

    public void setToken(int row, int column, char token) {
        intersections.add(new Intersection((column * gridSize) + 50, (row * gridSize) + 50, token == 'B' ? Color.BLACK : Color.WHITE));
        repaint();
   }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        for (Intersection intersection : intersections) {
            g.setColor(intersection.getColor());
            g.fillOval(intersection.getX() - 10, intersection.getY() - 10, 20, 20);
        }
    }


    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i <= numberOfSquares; i++) {
            int xy = i * gridSize;
            g.drawLine(xy, 0, xy, boardSize);
            g.drawLine(0, xy, boardSize, xy);
        }
    }

    private class IntersectionMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int closestX = Math.round((float) x / gridSize) * gridSize;
            int closestY = Math.round((float) y / gridSize) * gridSize;
            addIntersection(closestX, closestY, Color.BLACK);
        }
    }
}

