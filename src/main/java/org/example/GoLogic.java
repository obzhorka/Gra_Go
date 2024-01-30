package org.example;


import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.awt.Point;
import static org.example.GoBoard.boardColors;

public class GoLogic extends Component {
    public static int countTerritory(int row, int col, boolean[][] visited, Color owner) {
        if (!isValidPosition(row, col) || visited[row][col] || getColorAt(row, col) != null) {
            return 0;
        }
        visited[row][col] = true;

        // Sprawdź wszystkich sąsiadów
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : directions) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (isValidPosition(nr, nc) && !visited[nr][nc]) {
                Color nextColor = getColorAt(nr, nc);
                if (nextColor != null && nextColor != owner) {
                    return 0;  // Napotkano kamień innego koloru, nie liczymy terytorium
                }
            }
        }
        int count = 1;  // Początkowe przecięcie jest puste i należy do terytorium
        for (int[] d : directions) {
            int nr = row + d[0];
            int nc = col + d[1];
            count += countTerritory(nr, nc, visited, owner);  // Liczymy rekurencyjnie
        }
        return count;
    }
    public static Color findTerritoryOwner(int row, int col, boolean[][] visited) {
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
        // Jeśli nie jest terytorium, czyli znaleziono kamienie różnych kolorów
        if (!isTerritory) {
            for (Point p : territory) {
                visited[p.x][p.y] = false;
            }
            return null;
        }
        return owner;
    }
    public static boolean isValidPosition(int row, int column) {
        return row >= 0 && row <= GoBoard.numberOfSquares && column >= 0 && column <= GoBoard.numberOfSquares;
    }
    public static Color getColorAt(int row, int column) {
        if (isValidPosition(row, column)) {
            return boardColors[row][column];
        }
        return null; // Jeśli pozycja nie jest na planszy
    }
}
