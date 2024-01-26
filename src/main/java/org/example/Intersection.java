package org.example;

import java.awt.Color;

// reprezentuje punkt przecięcia
public class Intersection {
    private int x;
    private int y;
    private Color color;

    public Intersection(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public Color setColor(Color color) {
        this.color = color;
        return color;
    }
}

