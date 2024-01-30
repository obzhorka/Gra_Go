package org.example;

import java.awt.*;

public class BlackMoveState implements GoBoardState {
    @Override
    public void handleMouseClick(int x, int y, GoBoard board) {
        // Logika ruchu czarnych
        board.processMove(x, y, Color.BLACK);
    }
}