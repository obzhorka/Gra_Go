package org.example;

import java.io.*;
import java.net.*;


import static org.example.GoClient.*;


/**
 *
 * @author aid
 */
class NewGoSession implements Runnable {
    private Socket firstPlayer;
    private Socket secondPlayer;
    private char[][] cells = new char[9][9];
    private boolean[] playerPassed = new boolean[2]; // Track if players have passed their turns
    private char currentPlayer = 'B';
    private GoBoard goBoard;
    private DataInputStream fromPlayer1;
    private DataOutputStream toPlayer1;
    private DataInputStream fromPlayer2;
    private DataOutputStream toPlayer2;


    public NewGoSession(Socket firstPlayer, Socket secondPlayer, GoBoard goBoard) {
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
             fromPlayer1 = new DataInputStream(firstPlayer.getInputStream());
             toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
            fromPlayer2 = new DataInputStream(secondPlayer.getInputStream());
            toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());

//            toPlayer1.writeInt(1);
            // wyslać powiadomienie o start gry
            toPlayer1.writeChar('B');
            toPlayer2.writeChar('W');

            while (true) {
                // Handle moves from players
                // Example for Player 1
//                handleMove(fromPlayer1, toPlayer1, toPlayer2, 'B', 0);
                if (currentPlayer == 'B') {
                    handleMove(fromPlayer1, toPlayer1, toPlayer2, 'B', 0);
                } else {
                    handleMove(fromPlayer2, toPlayer2, toPlayer1, 'W', 1);
                }
                // Check for game end
                if (checkGameEnd()) {
                    int scoreB = calculateScore('B');
                    int scoreW = calculateScore('W');
                    sendEndGameSignal(scoreB, scoreW);
                    // Send end game signal and scores to players
                    break;
                }
                // Zmień obecnego gracza
                currentPlayer = (currentPlayer == 'B') ? 'W' : 'B';
//                // Similar for Player 2
//                handleMove(fromPlayer2, toPlayer2, toPlayer1, 'W', 1);

//                // Check for game end
//                if (checkGameEnd()) {
//                    int scoreB = calculateScore('B');
//                    int scoreW = calculateScore('W');
//                    // Calculate scores and send end game signal to players
//                    sendEndGameSignal(scoreB, scoreW);
//                    break;
//                }
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
                sendMove(toOtherPlayer, row, column, token);
                checkAndApplyCapture(row, column, token);
            } else if (isPassMove(row, column)) {
                playerPassed[playerIndex] = true;
            }

//            if (isPassMove(row, column)) {
//                playerPassed[playerIndex] = true;
            // If both players have passed, end the game.
            if (playerPassed[0] && playerPassed[1]) {
                endGame();
//                    return;
            }
//            // If only one player has passed, inform the other player to continue
//            sendPassInfoToOtherPlayer(toOtherPlayer);
             else{
                sendMove(toOtherPlayer, row, column, token);
//                // Reset the pass status if a regular move is made
//                playerPassed[playerIndex] = false;
//                // Handle normal move...
            }
        }
//public NewGoSession(Socket firstPlayer, Socket secondPlayer, GoBoard goBoard) {
//    this.firstPlayer = firstPlayer;
//    this.secondPlayer = secondPlayer;
//    this.goBoard = goBoard; // Store a reference to the GoBoard
//    for (int i = 0; i < 9; i++) {
//        for (int j = 0; j < 9; j++) {
//            cells[i][j] = ' ';
//        }
//    }
//}
//
//    @Override
//    public void run() {
//        try {
//            DataInputStream fromPlayer1 = new DataInputStream(firstPlayer.getInputStream());
//            DataOutputStream toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
//            DataInputStream fromPlayer2 = new DataInputStream(secondPlayer.getInputStream());
//            DataOutputStream toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());
//
//            toPlayer1.writeInt(1);
//
//            while (true) {
//                // Handle moves from players
//                // Example for Player 1
//                handleMove(fromPlayer1, toPlayer1, toPlayer2, 'B', 0);
//
//                // Check for game end
//                if (checkGameEnd()) {
//                    int scoreB = calculateScore('B');
//                    int scoreW = calculateScore('W');
//                    // Send end game signal and scores to players
//                    break;
//                }
//
//                // Similar for Player 2
//                handleMove(fromPlayer2, toPlayer2, toPlayer1, 'W', 1);
//
//                // Check for game end
//                if (checkGameEnd()) {
//                    // Calculate scores and send end game signal to players
//                    break;
//                }
//            }
//        } catch (IOException ex) {
//            System.err.println(ex);
//        }
//    }
//
//    private void handleMove(DataInputStream fromPlayer, DataOutputStream toPlayer, DataOutputStream toOtherPlayer, char token, int playerIndex) throws IOException {
//        int row = fromPlayer.readInt();
//        int column = fromPlayer.readInt();
//
//        if (isValidMove(row, column, token)) {
//            cells[row][column] = token;
//            sendMove(toOtherPlayer, row, column,token);
//            checkAndApplyCapture(row, column, token);
//            goBoard.setToken(row, column, token); // Update GoBoard with the move
//        } else if (isPassMove(row, column)) {
//            playerPassed[playerIndex] = true;
//        }
//
//        if (isPassMove(row, column)) {
//            playerPassed[playerIndex] = true;
//            // If both players have passed, end the game.
//            if (playerPassed[0] && playerPassed[1]) {
//                endGame();
//                return;
//            }
//            // If only one player has passed, inform the other player to continue
//            sendPassInfoToOtherPlayer(toOtherPlayer);
//        } else {
//            // Reset the pass status if a regular move is made
//            playerPassed[playerIndex] = false;
//            // Handle normal move...
//        }
//    }
//    private void sendPassInfoToOtherPlayer(DataOutputStream out) throws IOException {
//        // Send a special code to indicate that the other player has passed
//        out.writeInt(-2); // Example: -2 could be a code for 'other player passed'
//        out.writeInt(-2);
//    }


    private void sendMove(DataOutputStream out, int row, int column, char token) throws IOException {
            out.writeInt(row);
            out.writeInt(column);
            out.writeChar(token);
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
    //TODO dodać obsługe prisonerów
    private boolean isCaptured(int row, int column) {
        // Implement logic to check if stones are completely surrounded
        // This is a complex logic that requires checking for empty spaces around a group of connected stones.
        return false; // Placeholder, return true if captured
    }
    //TODO odadać obsługę
    private void removeStones(int row, int column) {
        // Implement logic to remove captured stones from the board
    }

        private boolean checkGameEnd() {
            // Check if the game has ended (both players passed consecutively)
            return playerPassed[0] && playerPassed[1];
        }


    private void endGame() throws IOException {
        // Implement the logic to calculate scores
        int scoreB = calculateScore('B');
        int scoreW = calculateScore('W');

        // Determine the winner or if it's a draw
        int gameResult;
        if (scoreB > scoreW) {
            gameResult = PLAYER1_WON;
        } else if (scoreW > scoreB) {
            gameResult = PLAYER2_WON;
        } else {
            gameResult = DRAW;
        }

        // Send the game result and scores to both players
        sendDataToPlayers(gameResult, scoreB, scoreW);
    }
//TODO zaimplememntować metodę
    private int calculateScore(char token) {
        // Implement the scoring logic
        // This is a placeholder; actual scoring can be complex in Go
        int score = 0;
        // Calculate score based on the number of stones and territories
        return score;
    }

    private void sendDataToPlayers(int gameResult, int scoreB, int scoreW) throws IOException {
        // Send data to Player 1
        DataOutputStream toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
        toPlayer1.writeInt(gameResult);
        toPlayer1.writeInt(scoreB);
        toPlayer1.writeInt(scoreW);

        // Send data to Player 2
        DataOutputStream toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());
        toPlayer2.writeInt(gameResult);
        toPlayer2.writeInt(scoreB);
        toPlayer2.writeInt(scoreW);
    }
    private void sendEndGameSignal(int scoreB, int scoreW) throws IOException {
        //sending the end of the game signal and transmitting the results
        int gameResult;
        if (scoreB > scoreW) {
            gameResult = GoServer.PLAYER1_WON;
        } else if (scoreW > scoreB) {
            gameResult = GoServer.PLAYER2_WON;
        } else {
            gameResult = GoServer.DRAW;
        }

        sendDataToPlayers(gameResult, scoreB, scoreW);
    }

}