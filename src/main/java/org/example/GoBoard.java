package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.*;
import java.awt.Point;

import static java.awt.Color.*;
public class GoBoard extends JPanel {
    private final int gridSize;
    private int gameId;
    private int playerId;
    private int row;
    private int col;

    private final int numberOfSquares;
    private final int boardSize;
    private char token;
    private Color currentColor = white;
    private Color[][] previousBoardColors; // Poprzedni stan planszy

    private int blackStonesCount = 0;
    private int whiteStonesCount = 0;
    private int blackCaptures = 0;
    private int whiteCaptures = 0;
    int blackTerritory = 0;
    int whiteTerritory = 0;

    private Color[][] boardColors;

    private boolean isFirstMove = true;


    private final ArrayList<Intersection> intersections = new ArrayList<>();
    private ArrayList<Color[][]> boardHistory = new ArrayList<>();
    private int licznikUsageKo = 0;
    private boolean hasBlackPassed = false;
    private boolean hasWhitePassed = false;


    public GoBoard() {
        setPreferredSize(new Dimension(boardSize, boardSize));
        addMouseListener(new IntersectionMouseListener());
    }


    public void addIntersection(int x, int y, Color color) {
        intersections.add(new Intersection(x, y, color));
        repaint();
    }


//    public void setToken(int row, int column, char token) {
//        intersections.add(new Intersection((column * gridSize) + 50, (row * gridSize) + 50, token == 'B' ? Color.BLACK : Color.WHITE));
//        repaint();
//   }

    private void displayResult(int blackScore, int whiteScore, int blackCaptures, int whiteCaptures) {
        String resultMessage = String.format("Czarne kamienie: %d (Zbite piony: %d, Terytorium: %d)\n" +
                        "Białe kamienie: %d (Zbite piony: %d, Terytorium: %d)",
                blackScore, blackCaptures, blackTerritory,
                whiteScore, whiteCaptures, whiteTerritory);

        String winner = blackScore > whiteScore ? "Czarny wygrywa!" :
                whiteScore > blackScore ? "Biały wygrywa!" : "Remis!";

        JOptionPane.showMessageDialog(this, resultMessage + "\n\n" + winner,
                "Koniec gry", JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    //siatka planszy oraz skrzyżowania
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        for (Intersection intersection : intersections) {
            g.setColor(intersection.getColor());
            g.fillOval(intersection.getX() - 10, intersection.getY() - 10, 20, 20);

        }
    }

// kratka
    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i <= numberOfSquares; i++) {
            int xy = i * gridSize; //miedzy kwadratami
            g.drawLine(xy, 0, xy, boardSize);
            g.drawLine(0, xy, boardSize, xy);
        }
    }
    public boolean isValidPosition(int row, int column) {
        return row >= 0 && row <= numberOfSquares && column >= 0 && column <= numberOfSquares;
    }


    private Color getColorAt(int row, int column) {
        if (isValidPosition(row, column)) {
            return boardColors[row][column];
        }
        return null; // Jeśli pozycja nie jest na planszy
    }


    public int getGridSize() {
        if(boardSize==19){
            return 30;
        }
        return 50;
    }

    private void calculateFinalScore() {
        boolean[][] visited = new boolean[numberOfSquares + 1][numberOfSquares + 1];
        blackTerritory = 0;
        whiteTerritory = 0;

        for (int i = 1; i <= numberOfSquares; i++) {
            for (int j = 1; j <= numberOfSquares; j++) {
                if (!visited[i][j] && getColorAt(i, j) == null) {
                    Color owner = findTerritoryOwner(i, j, visited);
                    if (owner == Color.BLACK) {
                        blackTerritory++;
                    } else if (owner == Color.WHITE) {
                        whiteTerritory++;
                    }
                }
            }
        }

        int blackScore = blackTerritory + blackCaptures;
        int whiteScore = whiteTerritory + whiteCaptures;

        displayResult(blackScore, whiteScore, blackCaptures, whiteCaptures);
    }
    private Color findTerritoryOwner(int row, int col, boolean[][] visited) {
        Queue<Point> queue = new LinkedList<>();
        Set<Point> territory = new HashSet<>();
        queue.add(new Point(row, col));
        territory.add(new Point(row, col));
        Color owner = null;
        boolean isTerritory = true;

        while (!queue.isEmpty() && isTerritory) {
            Point p = queue.poll();

            int r = p.x;
            int c = p.y;
            visited[r][c] = true;

            // Sprawdź wszystkich sąsiadów
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] d : directions) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (isValidPosition(nr, nc) && !visited[nr][nc]) {
                    Color color = getColorAt(nr, nc);
                    if (color == null) {
                        queue.add(new Point(nr, nc));
                        territory.add(new Point(nr, nc));
                    } else {
                        if (owner == null) {
                            owner = color;
                        } else if (owner != color) {
                            isTerritory = false;
                            break;
                        }
                    }
                }
            }
        }
        // Jeśli nie jest terytorium, czyli znaleziono kamienie różnych kolorów,
        // oznacz wszystkie przecięcia jako nieodwiedzone i zwróć null
        if (!isTerritory) {
            for (Point p : territory) {
                visited[p.x][p.y] = false;
            }
            return null;
        }

        return owner;
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

