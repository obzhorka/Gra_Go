package org.example;

import java.awt.*;

public class WhiteMoveState implements GoBoardState {
    @Override
    public void handleMouseClick(int x, int y, GoBoard board) {
        // Logika ruchu białych
        board.processMove(x, y, Color.WHITE);
    }
}
