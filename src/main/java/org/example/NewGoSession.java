package org.example;

import java.io.*;
import java.net.*;


import static org.example.GoClient.DRAW;
import static org.example.GoServer.CONTINUE;


/**
 *
 * @author aid
 */
class NewGoSession implements Runnable {
    private Socket firstPlayer;
    private Socket secondPlayer;
    private char[][] cells = new char[9][9];
    private boolean[] playerPassed = new boolean[2]; // Track if players have passed their turns

    public NewGoSession(Socket firstPlayer, Socket secondPlayer) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = ' ';
            }
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream fromPlayer1 = new DataInputStream(firstPlayer.getInputStream());
            DataOutputStream toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
            DataInputStream fromPlayer2 = new DataInputStream(secondPlayer.getInputStream());
            DataOutputStream toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());

            toPlayer1.writeInt(1);

            while (true) {
                // Handle moves from players
                // Example for Player 1
                handleMove(fromPlayer1, toPlayer1, toPlayer2, 'B', 0);

                // Check for game end
                if (checkGameEnd()) {
                    int scoreB = calculateScore('B');
                    int scoreW = calculateScore('W');
                    // Send end game signal and scores to players
                    break;
                }

                // Similar for Player 2
                handleMove(fromPlayer2, toPlayer2, toPlayer1, 'W', 1);

                // Check for game end
                if (checkGameEnd()) {
                    // Calculate scores and send end game signal to players
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

        private void handleMove(DataInputStream fromPlayer, DataOutputStream toPlayer, DataOutputStream toOtherPlayer, char token, int playerIndex) throws IOException {
            int row = fromPlayer.readInt();
            int column = fromPlayer.readInt();

            if (isValidMove(row, column, token)) {
                cells[row][column] = token;
                sendMove(toOtherPlayer, row, column);
                checkAndApplyCapture(row, column, token);
            } else if (isPassMove(row, column)) {
                playerPassed[playerIndex] = true;
            }
        }


    private void sendMove(DataOutputStream out, int row, int column) throws IOException {
            out.writeInt(row);
            out.writeInt(column);

    }

    private boolean isValidMove(int row, int column, char token) {
            // Check if the move is valid (inside the board and on an empty cell)
            // Implement Ko rule checks here
            return row >= 0 && row < 9 && column >= 0 && column < 9 && cells[row][column] == ' ';
        }

        private boolean isPassMove(int row, int column) {
            // Check if the move is a pass (special signal, e.g., -1, -1)
            return row == -1 && column == -1;
        }

        private void checkAndApplyCapture(int row, int column, char token) {
            // Check for captures and apply them
            checkCapture(row + 1, column, token);
            checkCapture(row - 1, column, token);
            checkCapture(row, column + 1, token);
            checkCapture(row, column - 1, token);
        }
    private void checkCapture(int row, int column, char token) {
        if (row >= 0 && row < 9 && column >= 0 && column < 9 && cells[row][column] != ' ' && cells[row][column] != token) {
            if (isCaptured(row, column)) {
                removeStones(row, column);
            }
        }
    }
    private boolean isCaptured(int row, int column) {
        // Implement logic to check if stones are completely surrounded
        // This is a complex logic that requires checking for empty spaces around a group of connected stones.
        return false; // Placeholder, return true if captured
    }
    private void removeStones(int row, int column) {
        // Implement logic to remove captured stones from the board
    }

        private boolean checkGameEnd() {
            // Check if the game has ended (both players passed consecutively)
            return playerPassed[0] && playerPassed[1];
        }

        private int calculateScore(char token) {
            // Calculate the score for a player based on territory and captured stones
            return 0; // Placeholder for score calculation
        }
    }