package incom.hyogyu.games.snake;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Food {
    private Point position;
    private final int gridWidth;
    private final int gridHeight;
    private final int tileSize;

    public Food(int gridWidth, int gridHeight, int tileSize) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.tileSize = tileSize;
        generateNewPosition(null);
    }

    public void generateNewPosition(List<Point> snakeBody) {
        Random random = new Random();
        Point newPosition;
        do {
            int x = random.nextInt(gridWidth / tileSize) * tileSize;
            int y = random.nextInt(gridHeight / tileSize) * tileSize;
            newPosition = new Point(x, y);
        } while (snakeBody != null && snakeBody.contains(newPosition)); // 겹치지 않도록 확인
        position = newPosition;
    }

    public Point getPosition() {
        return position;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }
}