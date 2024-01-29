package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GoBoard extends JPanel {
    private final int gridSize = 50;
    private final int numberOfSquares = 9;
    private final int boardSize = gridSize * numberOfSquares;
    private char token;
    private boolean hasBlackPassed = false;
    private boolean hasWhitePassed = false;

    private final ArrayList<Intersection> intersections = new ArrayList<>();

    public GoBoard(int boardSize) {
        setPreferredSize(new Dimension(this.boardSize, this.boardSize));
        //reaguje na kliknięcia myszką na planszy
        addMouseListener(new IntersectionMouseListener());
    }

    public void addIntersection(int x, int y, Color color) {
        intersections.add(new Intersection(x , y , color));
        repaint();
    }

    // Dodaj nowe pola dla przesunięcia planszy
    final int offsetX = 50; // Przesunięcie w poziomie
    final int offsetY = 50; // Przesunięcie w pionie
//Dodaje nową intersekcję w określonym rzędzie i kolumnie na podstawie tokena
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
//Rysuje siatkę kwadratów na planszy

    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i <= numberOfSquares; i++) {
            int xy = i * gridSize + 50;
            g.drawLine(xy, 50, xy, boardSize + 50);
            g.drawLine(50, xy, boardSize + 50, xy);
        }
    }

    public int getGridSize() {
        return 0;
    }

    public void setBlackPassed(boolean hasPassed) {
        this.hasBlackPassed = hasPassed;
    }

    public void setWhitePassed(boolean hasPassed) {
        this.hasWhitePassed = hasPassed;
    }

    protected class IntersectionMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            //oblicza najbliższą pozycję punktu siatki na planszy
            int x = e.getX();
            int y = e.getY();
            //w jakiiej kolumnie/rzędzie kwadratu->przecięcie
            int closestX = Math.round((float) x / gridSize) * gridSize;
            int closestY = Math.round((float) y / gridSize) * gridSize;
            addIntersection(closestX, closestY, Color.BLACK);
        }
    }
}

