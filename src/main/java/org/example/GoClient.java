package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;

public class Client {

    private JFrame frame;
    private GoBoard board;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JButton passButtonB;
    private JButton passButtonW;
    private char currentToken = 'B';
    private boolean hasBlackPassed = false;
    private boolean hasWhitePassed = false;

    public Client(String hostName, int port) throws IOException {
        socket = new Socket(hostName, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        frame = new JFrame("Klient - Plansza Go");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        // Dodaj logikę wyboru rozmiaru planszy
        String[] boardSizes = {"9x9", "13x13", "19x19"};
        String selectedSize = (String) JOptionPane.showInputDialog(null,
                "Wybierz rozmiar planszy:", "Wybór planszy",
                JOptionPane.QUESTION_MESSAGE, null, boardSizes, boardSizes[0]);

        int boardSize = 9; // Domyślny rozmiar planszy
        if (selectedSize.equals("13x13")) {
            boardSize = 13;
        } else if (selectedSize.equals("19x19")) {
            boardSize = 19;
        }


        board = new GoBoard(boardSize);

        // Tworzenie przycisków
        passButtonB = new JButton("Pasuj Biały");
        passButtonW = new JButton("Pasuj Czarny");

        // Dodawanie obsługi zdarzeń dla przycisków
        passButtonB.addActionListener(e -> passMove('B'));
        passButtonW.addActionListener(e -> passMove('W'));

        // Umieszczanie przycisków na dole ramki
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(passButtonB);
        buttonPanel.add(passButtonW);

        frame.add(board, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);



        board.addMouseListener(new MouseAdapter() {
            private char currentPlayer = 'B';
            @Override
            public void mouseClicked(MouseEvent e) {
                int gridSize = board.getGridSize();
                int row = (e.getY() - board.offsetY) / gridSize;
                int col = (e.getX() - board.offsetX) / gridSize;

                try {
                    out.println(row + "," + col);
                    String response = in.readLine();
                    if (response != null && response.equals("OK")) {
                        //board.setToken(row, col, currentToken);
                        currentToken = (currentToken == 'B') ? 'И' : 'B'; // Przykładowo zawsze czarny
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.add(board);
        frame.pack();
        frame.setVisible(true);
    }
    private void passMove(char player) {
        System.out.println(player + " gracz pasuje.");
        if (player == 'B') {
            hasBlackPassed = true;
            board.setBlackPassed(true);
        } else if (player == 'W') {
            hasWhitePassed = true;
            board.setWhitePassed(true);
        }

        // Sprawdzenie, czy gra powinna się zakończyć
        if (hasBlackPassed && hasWhitePassed) {
            // Zakończ grę
        } else {
            // Zresetuj flagę pasowania dla gracza, który nie spasował
            if (player == 'B') {
                hasWhitePassed = false;
                board.setWhitePassed(false);
            } else if (player == 'W') {
                hasBlackPassed = false;
                board.setBlackPassed(false);
            }
        }
    }


    public static void main(String[] args) throws IOException {

        new Client("localhost", 1234);
    }
}