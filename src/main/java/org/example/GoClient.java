package org.example;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
//obiekty tej klasy mogą być uruchamiane w osobnych wątkach
public class GoClient extends JFrame implements Runnable { //, ActionListener

    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    //TODO zmieni kiedy sie otrzyma z Pass Button
    public static final int PLAYER1_WON = 0;
    public static final int PLAYER2_WON = 0;
    public static final int DRAW = 3;

    private char myToken = 'B'; // Black stone
    private char otherToken = 'W'; // White stone

    Socket socket;
    private boolean myTurn = false;
    private Cell[][] cells = new Cell[9][9];
    private JLabel titleLabel = new JLabel();
    private JLabel statusLabel = new JLabel();
    private GoBoard goBoard;
    private int rowSelected;
    private int columnSelected;

    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private boolean continueToPlay = true;
    private boolean waiting = true;
    private JButton passButton;
    public static void main(String[] args) {
        GoClient display = new GoClient();
        display.setBounds(100, 100, 600, 600);
        display.init();
        display.setVisible(true);
    }

    public void init() {
        // Main panel with a layout to hold both the board and the button
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Game board panel (GoBoard)
        goBoard = new GoBoard(); // Create an instance of GoBoard
        mainPanel.add(goBoard, BorderLayout.CENTER); // Add GoBoard to the main panel

        // Pass button
        passButton = new JButton("Pass Turn");
        passButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                passTurn();
            }
        });
        mainPanel.add(passButton, BorderLayout.SOUTH); // Add button below the board

        // Add the main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Other components like title and status labels
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(new LineBorder(Color.black, 1));
        add(titleLabel, BorderLayout.NORTH);

        statusLabel.setBorder(new LineBorder(Color.black, 1));
        add(statusLabel, BorderLayout.SOUTH);

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8001);
            //strumień wejściowy danych
            fromServer = new DataInputStream(socket.getInputStream());
            //strumień wyjściowy danych
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println(ex);
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            int player = fromServer.readInt();

            if (player == PLAYER1) {
                myToken = 'B';
                otherToken = 'W';
                titleLabel.setText("Player 1 with token 'B'");
                statusLabel.setText("Waiting for player 2 to join");
                fromServer.readInt();
                statusLabel.setText("Player 2 has joined. I start first");
                //myTurn = true;
            } else if (player == PLAYER2) {
                myToken = 'W';
                otherToken = 'B';
                titleLabel.setText("Player 2 with token 'W'");
                statusLabel.setText("Waiting for player 1 to move");
                //fromServer.readInt();
                //myTurn = true;
            }

            while (continueToPlay) {
                if (player == PLAYER1) {
                    waitForPlayerAction();
                    sendMove();
                    receiveInfoFromServer();
                } else if (player == PLAYER2) {
                    receiveInfoFromServer();
                    waitForPlayerAction();
                    sendMove();
                }
            }
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }
    }

    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }
        waiting = true;
    }

    private void sendMove() throws IOException {
        toServer.writeInt(rowSelected);
        toServer.writeInt(columnSelected);
    }
//Odbiera informacje od serwera o stanie gry
    private void receiveInfoFromServer() throws IOException {
        int status = fromServer.readInt();
        if (status == PLAYER1_WON) {
            continueToPlay = false;
            if (myToken == 'B') {
                statusLabel.setText("I Won! (B)");
            } else if (myToken == 'W') {
                statusLabel.setText("Player 1 (B) has won!");
                receiveMove();
            }
        } else if (status == PLAYER2_WON) {
            continueToPlay = false;
            if (myToken == 'W') {
                statusLabel.setText("I Won! (W)");
            } else if (myToken == 'B') {
                statusLabel.setText("Player 2 (W) has won!");
                receiveMove();
            }
        } /*else if (status == DRAW) {
            continueToPlay = false;
            statusLabel.setText("Game is over, no winner!");
            if (myToken == 'W') {
                receiveMove();
            }
        }*/ else {
            receiveMove();
            statusLabel.setText("My turn");
            myTurn = true;
        }
    }

    private void receiveMove() throws IOException {
        int row = fromServer.readInt();
        int column = fromServer.readInt();

        if (row == -1 && column == -1) {
            // Handle pass move received
            statusLabel.setText("The other player passed their turn.");
        } else {
            // Handle normal move received
            cells[row][column].setToken(otherToken);
            statusLabel.setText("Move received, your turn.");
        }

        myTurn = true; // Now it's this client's turn
    }

    private void passTurn() {
        if (myTurn) {
            try {
                toServer.writeInt(-1); // Send a special code for pass
                //toServer.writeInt(-1);
                myTurn = false;
                statusLabel.setText("Turn passed, waiting for the other player.");
            } catch (IOException ex) {
                System.err.println("Error passing turn: " + ex);
            }
        }
    }

    public class Cell extends JPanel {
        private int row, column;
        private char token = ' ';


//        public int getColumn() {
//            return column;
//        }
//        public char getToken() {
//            return token;
//        }
//        public char getToken() {
//            return token;
//        }

        public void setToken(char c) {
            token = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (token == 'B') {
                g.setColor(Color.BLACK);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
            } else if (token == 'W') {
                g.setColor(Color.WHITE);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
            }
        }


    }
}

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            GoClient display = new GoClient();
//            display.setBounds(100, 100, 600, 600);
//            display.init();
//            display.setVisible(true);
//        });
//
//    }
//
//    public void init() {
//        // Main panel with a layout to hold both the board and the button
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BorderLayout());
//
//        // Game board panel
//        JPanel boardPanel = new JPanel();
//        boardPanel.setLayout(new GridLayout(9, 9, 0, 0)); // 9x9 for Go board
//        for (int i = 0; i < 9; i++) {
//            for (int j = 0; j < 9; j++) {
//                boardPanel.add(cells[i][j] = new Cell(i, j));
//            }
//        }
//        boardPanel.setBorder(new LineBorder(Color.black, 1));
//        mainPanel.add(boardPanel, BorderLayout.CENTER);
//
//        // Pass button
//        passButton = new JButton("Pass Turn");
//        passButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                passTurn();
//            }
//        });
//        mainPanel.add(passButton, BorderLayout.SOUTH); // Add button below the board
//
//        // Add the main panel to the frame
//        add(mainPanel, BorderLayout.CENTER);
//
//        // Other components like title and status labels
//        titleLabel.setHorizontalAlignment(JLabel.CENTER);
//        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
//        titleLabel.setBorder(new LineBorder(Color.black, 1));
//        add(titleLabel, BorderLayout.NORTH);
//
//        statusLabel.setBorder(new LineBorder(Color.black, 1));
//        add(statusLabel, BorderLayout.SOUTH);
//
//        connectToServer();
//    }
//
//    private void connectToServer() {
//        try {
//            socket = new Socket("localhost", 8000);
//            fromServer = new DataInputStream(socket.getInputStream());
//            toServer = new DataOutputStream(socket.getOutputStream());
//        } catch (IOException ex) {
//            System.err.println(ex);
//        }
//
//        Thread thread = new Thread(this);
//        thread.start();
//    }
//
//    @Override
//    public void run() {
//        try {
//            int player = fromServer.readInt();
//
//            if (player == PLAYER1) {
//                myToken = 'B';
//                otherToken = 'W';
//                titleLabel.setText("Player 1 with token 'B'");
//                statusLabel.setText("Waiting for player 2 to join");
//                fromServer.readInt();
//                statusLabel.setText("Player 2 has joined. I start first");
//                //myTurn = true;
//            } else if (player == PLAYER2) {
//                myToken = 'W';
//                otherToken = 'B';
//                titleLabel.setText("Player 2 with token 'W'");
//                statusLabel.setText("Waiting for player 1 to move");
//                //fromServer.readInt();
//                //myTurn = true;
//            }
//
//            while (continueToPlay) {
//                if (player == PLAYER1) {
//                    waitForPlayerAction();
//                    sendMove(rowSelected, columnSelected);
//                    receiveInfoFromServer();
//                } else if (player == PLAYER2) {
//                    receiveInfoFromServer();
//                    waitForPlayerAction();
//                    sendMove(rowSelected,columnSelected);
//                }
//            }
//        } catch (IOException | InterruptedException ex) {
//            System.err.println(ex);
//        }
//    }
//
//    private void waitForPlayerAction() throws InterruptedException {
//        while (waiting) {
//            Thread.sleep(100);
//        }
//        waiting = true;
//    }
//
//    private void sendMove(int row, int column) throws IOException {
//        try {
//            toServer.writeInt(row);
//            toServer.writeInt(column);
//        } catch (IOException ex) {
//            System.err.println("Error sending move: " + ex);
//        }
////        toServer.writeInt(rowSelected);
////        toServer.writeInt(columnSelected);
//    }
//    public void actionPerformed(ActionEvent e) {
//        if (myTurn) {
//            if (e.getSource() instanceof Cell) {
//                Cell clickedCell = (Cell) e.getSource();
//                int row = clickedCell.getRow();
//                int column = clickedCell.getColumn();
//                try {
//                    sendMove(row, column);
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            } else if (e.getSource() == passButton) {
//                passTurn();
//            }
//        }
//    }
//
//    private void receiveInfoFromServer() throws IOException {
//        int status = fromServer.readInt();
//        if (status == PLAYER1_WON) {
//            continueToPlay = false;
//            if (myToken == 'B') {
//                statusLabel.setText("I Won! (B)");
//            } else if (myToken == 'W') {
//                statusLabel.setText("Player 1 (B) has won!");
//                receiveMove();
//            }
//        } else if (status == PLAYER2_WON) {
//            continueToPlay = false;
//            if (myToken == 'W') {
//                statusLabel.setText("I Won! (W)");
//            } else if (myToken == 'B') {
//                statusLabel.setText("Player 2 (W) has won!");
//                receiveMove();
//            }
//        } /*else if (status == DRAW) {
//            continueToPlay = false;
//            statusLabel.setText("Game is over, no winner!");
//            if (myToken == 'W') {
//                receiveMove();
//
//        } */
//        else {
//            receiveMove();
//            statusLabel.setText("My turn");
//            myTurn = true;
//        }
//    }
//
//    private void receiveMove() throws IOException {
//        int row = fromServer.readInt();
//        int column = fromServer.readInt();
//
//        if (row == -1 && column == -1) {
//            // Handle pass move received
//            statusLabel.setText("The other player passed their turn.");
//        } else {
//            // Handle normal move received
//            cells[row][column].setToken(otherToken);
//            statusLabel.setText("Move received, your turn.");
//        }
//
//        myTurn = true; // Now it's this client's turn
//    }
//    private void passTurn() {
//        if (myTurn) {
//            try {
//                toServer.writeInt(-1); // Send a special code for pass
//                toServer.writeInt(-1);
//                myTurn = false;
//                statusLabel.setText("Turn passed, waiting for the other player.");
//            } catch (IOException ex) {
//                System.err.println("Error passing turn: " + ex);
//            }
//        }
//    }
//
//    public class Cell extends JPanel {
//        private int row, column;
//        private char token = ' ';
//
//        public Cell(int row, int column) {
//            this.row = row;
//            this.column = column;
//            setPreferredSize(new Dimension(30, 30)); // Установите размер каждой ячейки
//            setBorder(new LineBorder(Color.black, 1));
//            addMouseListener(new ClickListener());
//        }
//        public int getRow() {
//            return row;
//        }
//
//        public int getColumn() {
//            return column;
//        }
//        public char getToken() {
//            return token;
//        }
//
//        public void setToken(char c) {
//            token = c;
//            repaint();
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            if (token == 'B') {
//                g.setColor(Color.BLACK);
//                // współrzędne do rysowania kamienia na przecięciu linii
//                int x = getWidth() / 25 - 5;
//                int y = getHeight() / 25 - 5;
//                g.fillOval(x, y, 10, 10); // rysowanie kamienia
//            } else if (token == 'W') {
//                g.setColor(Color.WHITE);
//                // współrzędne do rysowania kamienia na przecięciu linii
//                int x = getWidth() / 25 - 5;
//                int y = getHeight() / 25 - 5;
//                g.fillOval(x, y, 10, 10); // rysowanie kamienia
//            }
//        }
//
//        private class ClickListener extends MouseAdapter {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if ((token == ' ') || myTurn) {
//                    setToken(myToken); // token dla obecnej komórki
//                    myTurn = false;
//                    rowSelected = row;
//                    columnSelected = column;
//                    try {
//                        sendMove(row,column);
//                    } catch (IOException ex) {
//                        System.err.println("Error sending move: " + ex);
//                    }
//                    waiting = false;
//                }
//            }
//        }
//    }
//
//}