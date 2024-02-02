package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import static java.awt.Color.*;


public class GoBoard extends JPanel {
    public static int gridSize;
    private int gameId;
    private int playerId;
    private int row;
    private int col;
    public static int numberOfSquares;
    private final int boardSize;
    private Color currentColor = white;
    private Color[][] previousBoardColors;

    static int blackTerritory = 0;
    static int whiteTerritory = 0;
    private static int blackCaptures = 0;
    private static int whiteCaptures = 0;

    private int blackStonesCount = 0;
    private int whiteStonesCount = 0;

    public static Color[][] boardColors;
    public static ArrayList<Intersection> intersections = new ArrayList<>();
    public boolean hasBlackPassed = false;
    public boolean hasWhitePassed = false;

    public void setBlackPassed(boolean hasPassed) {
        this.hasBlackPassed = hasPassed;
    }
    public void setWhitePassed(boolean hasPassed) {
        this.hasWhitePassed = hasPassed;
    }
    private GoBoardState currentState;
    private Color[][] secondPreviousBoardColors;
    public GoBoard(int boardSize) {
        this.numberOfSquares = boardSize;
        this.currentState = new BlackMoveState(); // Początkowy stan
        if(this.numberOfSquares==19){
            this.gridSize=35;
        }
        else{
            this.gridSize = 50;
        }
        this.boardSize = this.gridSize * this.numberOfSquares;

        setPreferredSize(new Dimension(this.boardSize + 100, this.boardSize + 100));
        addMouseListener(new IntersectionMouseListener());
        boardColors = new Color[numberOfSquares+2][numberOfSquares+2];
        previousBoardColors = new Color[numberOfSquares + 2][numberOfSquares + 2];
        secondPreviousBoardColors = new Color[numberOfSquares + 2][numberOfSquares + 2];
        for (int i = 0; i <= numberOfSquares + 1; i++) {
            for (int j = 0; j <= numberOfSquares + 1; j++) {
                previousBoardColors[i][j] = null;
                secondPreviousBoardColors[i][j] = null;
            }
        }
    }

    public void processMove(int x, int y, Color stoneColor) {
        // Sprawdź, czy pozycja jest ważna i czy pole jest puste
        if (!GoLogic.isValidPosition(x, y) || GoLogic.getColorAt(x, y) != null) {
            return; // Nieprawidłowy ruch
        }

        // Ustaw kamień na planszy
        boardColors[x][y] = stoneColor;
        addIntersection(x * gridSize + offsetX, y * gridSize + offsetY, stoneColor);
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        Color enemyColor = (stoneColor == BLACK) ? WHITE : BLACK;
        for (int[] dir : directions) {
            int newRow = x + dir[0];
            int newCol = y + dir[1];
            if (GoLogic.getColorAt(newRow, newCol) == enemyColor && isStoneSurrounded(newRow, newCol, enemyColor)) {
                removeGroup(newRow, newCol, enemyColor);
            }
        }
        updatePreviousBoardStates();
        // Zmiana gracza
        changeState();
        repaint();
    }
    public void removeGroup(int row, int column, Color stoneColor) {
        if (!GoLogic.isValidPosition(row, column) || GoLogic.getColorAt(row, column) != stoneColor) return;

        removeStone(row, column);
        removeGroup(row - 1, column, stoneColor);
        removeGroup(row + 1, column, stoneColor);
        removeGroup(row, column - 1, stoneColor);
        removeGroup(row, column + 1, stoneColor);
    }

    public void removeStone(int row, int column) {
        Color removedStoneColor = boardColors[row][column];
        boardColors[row][column] = null;
        if (removedStoneColor == BLACK) {
            blackCaptures--;
            whiteCaptures++;
        } else if (removedStoneColor == WHITE) {
            whiteCaptures--;
            blackCaptures++;
        }
        // Teraz znajdź i usuń odpowiednią skrzyżowanie (Intersection)
        Intersection toRemove = null;
        for (Intersection intersection : intersections) {
            // Zamieniamy współrzędne siatki na współrzędne pikseli i sprawdzamy
            if (intersection.getX() == row * gridSize + offsetX && intersection.getY() == column * gridSize + offsetY) {
                toRemove = intersection;
                break;
            }
        }
        if (toRemove != null) {
            intersections.remove(toRemove);
        }
        repaint(); // Odświeżamy planszę, aby usunięcie było widoczne
    }
    private void updatePreviousBoardStates() {
        for (int i = 0; i <= numberOfSquares + 1; i++) {
            System.arraycopy(previousBoardColors[i], 0, secondPreviousBoardColors[i], 0, numberOfSquares + 2);
            System.arraycopy(boardColors[i], 0, previousBoardColors[i], 0, numberOfSquares + 2);
        }
    }

    public void calculateFinalScore() {
        boolean[][] visited = new boolean[GoBoard.numberOfSquares + 1][GoBoard.numberOfSquares + 1];

        for (int i = 1; i <= GoBoard.numberOfSquares; i++) {
            for (int j = 1; j <= GoBoard.numberOfSquares; j++) {
                if (!visited[i][j] && GoLogic.getColorAt(i, j) == null) {
                    Color owner = GoLogic.findTerritoryOwner(i, j, visited);
                    if (owner != null) {
                        int territorySize = GoLogic.countTerritory(i, j, new boolean[GoBoard.numberOfSquares + 1][GoBoard.numberOfSquares + 1], owner);
                        if (owner == BLACK) {
                            blackTerritory += territorySize;
                        } else if (owner == WHITE) {
                            whiteTerritory += territorySize;
                        }
                    }
                }
            }
        }

        int blackScore = blackTerritory + blackCaptures;
        int whiteScore = whiteTerritory + whiteCaptures;

        displayResult(blackScore, whiteScore, blackCaptures, whiteCaptures);
    }
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
    public void changeState() {
        if (currentState instanceof BlackMoveState) {
            setCurrentState(new WhiteMoveState());
        } else if (currentState instanceof WhiteMoveState) {
            setCurrentState(new BlackMoveState());
        }
    }

    public void setCurrentState(GoBoardState state) {
        this.currentState = state;
    }
    public void addIntersection(int x, int y, Color color) {
        intersections.add(new Intersection(x, y, color));
        repaint();
        // Aktualizacja liczników kamieni
        if (color == BLACK) {
            blackStonesCount++;
        } else if (color == WHITE) {
            whiteStonesCount++;
        }
    }

    static int offsetX = 50;
    static int offsetY = 50;
    public void setToken(int x, int y) {
        int closestX = Math.round((float) (x - offsetX) / gridSize);
        int closestY = Math.round((float) (y - offsetY) / gridSize);
        this.gameId = gameId;
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        if (GoLogic.isValidNotDeath(closestX, closestY, currentColor)) {
            return; // Jeśli próba naruszenia zasady ko, to nie pozwalamy na ten ruch
        }
        if (GoLogic.isValidPosition(closestX, closestY) && GoLogic.getColorAt(closestX, closestY) == null) {
            Color stoneColor = currentColor == white ? black : white;
            if (!hasLiberties(closestX, closestY, stoneColor)) {
                System.out.println("Nie możesz!!");
                return;
            }
            System.out.println("closestX: " + closestX + ", closestY: " + closestY);
            boardColors[closestX][closestY] = stoneColor;
            addIntersection(closestX * gridSize + offsetX, closestY * gridSize + offsetY, stoneColor);

            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            // Sprawdź, czy umieszczony kamień otacza kamienie przeciwnika
            Color enemyColor = (stoneColor == BLACK) ? WHITE : BLACK;
            for (int[] dir : directions) {
                int newRow = closestX + dir[0];
                int newCol = closestY + dir[1];
                if (GoLogic.getColorAt(newRow, newCol) == enemyColor && isStoneSurrounded(newRow, newCol, enemyColor)) {
                    removeGroup(newRow, newCol, enemyColor);
                }
            }
            currentColor = stoneColor;
            repaint();
        }
        // Po zakończeniu ruchu, kopiujemy obecny stan planszy do poprzedniego stanu
        for (int i = 0; i <= numberOfSquares + 1; i++) {
            System.arraycopy(previousBoardColors[i], 0, secondPreviousBoardColors[i], 0, numberOfSquares + 2);
            System.arraycopy(boardColors[i], 0, previousBoardColors[i], 0, numberOfSquares + 2);
        }
    }
    public static boolean hasLiberties(int row, int column, Color stoneColor) {
        if (!GoLogic.isValidPosition(row, column)) {
            return false;
        }

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        boolean hasLiberties = false;
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = column + dir[1];
            Color adjacentColor = GoLogic.getColorAt(newRow, newCol);
            if (adjacentColor == null) {
                return true; // Jeśli jest przynajmniej jeden wolny oddech
            }
            if (adjacentColor != stoneColor) {
                hasLiberties = true; // Jeśli jest kamień innego koloru, to jest oddech
            }
        }

        return hasLiberties;
    }

    static boolean isStoneSurrounded(int row, int column, Color stoneColor) {
        boolean[][] visited = new boolean[numberOfSquares+2][numberOfSquares+2];
        return !hasLiberty(row, column, stoneColor, visited);
    }
    private static boolean hasLiberty(int row, int col, Color stoneColor, boolean[][] visited) {
        if (!GoLogic.isValidPosition(row, col)) return false;
        if (visited[row][col]) return false;
        visited[row][col] = true;

        Color currentColor = GoLogic.getColorAt(row, col);
        if (currentColor == null) return true; // znaleziono wolność
        if (currentColor != stoneColor) return false; // napotkano kamień przeciwnika

        // Sprawdza wszystkie kierunki
        return hasLiberty(row - 1, col, stoneColor, visited) ||
                hasLiberty(row + 1, col, stoneColor, visited) ||
                hasLiberty(row, col - 1, stoneColor, visited) ||
                hasLiberty(row, col + 1, stoneColor, visited);
    }

    public int getGridSize() {
        if(boardSize==19){
            return 30;
        }
        return 50;
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
        g.setColor(BLACK);
        for (int i = 0; i <= numberOfSquares; i++) {
            int xy = i * gridSize + offsetX;
            g.drawLine(xy, offsetY, xy, boardSize + offsetY);
            g.drawLine(offsetX, xy, boardSize + offsetX, xy);
        }
    }


    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
    protected class IntersectionMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (currentColor == black && hasBlackPassed) {
                currentColor = white;
            } else if (currentColor == white && hasWhitePassed) {
                currentColor = black;
            }
            if(hasBlackPassed&&hasWhitePassed){
                calculateFinalScore();
                return;
            }
            if (currentColor == BLACK) {
                hasWhitePassed = false; // Resetuj flagę pasowania dla białego
                setWhitePassed(false);
            } else if (currentColor == WHITE) {
                hasBlackPassed = false; // Resetuj flagę pasowania dla czarnego
                setBlackPassed(false);
            }
            int x = e.getX();
            int y = e.getY();
            setToken(x, y);
            currentState.handleMouseClick(x, y, GoBoard.this);
        }
    }
}