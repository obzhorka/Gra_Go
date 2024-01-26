package org.example;

import java.awt.Color;

public class Game {
    private Intersection[][] board;
    private Color currentPlayer;
    private boolean gameEnded;

    public Game() {
        board = new Intersection[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = new Intersection(i, j, Color.WHITE); // Biały oznacza puste pole
            }
        }
        currentPlayer = Color.BLACK; // Zacznij od czarnego gracza
        gameEnded = false;
    }

    public String makeMove(int x, int y) {
        if (x < 0 || x >= 9 || y < 0 || y >= 9 || board[x][y].getColor() != Color.WHITE || gameEnded) {
            return "Nieprawidłowy ruch";
        }

        board[x][y] = new Intersection(x, y, currentPlayer);


        // Sprawdź, czy własna grupa nie została zdekapitowana
        if (!hasBreath(x, y, currentPlayer, new boolean[9][9])) {
            // Jeśli własna grupa została zdekapitowana, cofnij ruch
            board[x][y] = new Intersection(x, y, Color.WHITE);
            return "Samobójczy ruch";
        }

        currentPlayer = opponentColor(currentPlayer);
        return "Ruch wykonany";
    }
    private boolean hasBreath(int x, int y, Color playerColor, boolean[][] visited) {
        if (x < 0 || x >= 9 || y < 0 || y >= 9 || visited[x][y]) {
            return false;
        }

        if (board[x][y].getColor() == Color.WHITE) {
            return true;
        }

        if (board[x][y].getColor() != playerColor) {
            return false;
        }

        visited[x][y] = true;

        return hasBreath(x - 1, y, playerColor, visited) ||
                hasBreath(x + 1, y, playerColor, visited) ||
                hasBreath(x, y - 1, playerColor, visited) ||
                hasBreath(x, y + 1, playerColor, visited);
    }


    private Color opponentColor(Color color) {
        return color == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    public Intersection[][] getBoard() {
        return board;
    }
}