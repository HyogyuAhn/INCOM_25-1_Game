package incom.hyogyu.games.snake;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<Point> body;
    private Direction direction;
    private final int gridWidth;
    private final int gridHeight;
    private final int tileSize;

    public Snake(int gridWidth, int gridHeight, int tileSize) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.tileSize = tileSize;
        this.body = new ArrayList<>();
        reset();
    }

    public void reset() {
        body.clear();
        body.add(new Point(5 * tileSize, 5 * tileSize));
        direction = Direction.RIGHT;
    }

    public void setDirection(Direction newDirection) {
        if ((direction == Direction.UP && newDirection != Direction.DOWN) ||
                (direction == Direction.DOWN && newDirection != Direction.UP) ||
                (direction == Direction.LEFT && newDirection != Direction.RIGHT) ||
                (direction == Direction.RIGHT && newDirection != Direction.LEFT)) {
            direction = newDirection;
        }
    }

    public void move() {
        Point head = new Point(body.get(0));
        switch (direction) {
            case UP -> head.y -= tileSize;
            case DOWN -> head.y += tileSize;
            case LEFT -> head.x -= tileSize;
            case RIGHT -> head.x += tileSize;
        }
        body.add(0, head);
        body.remove(body.size() - 1);
    }

    public void grow() {
        Point tail = body.get(body.size() - 1);
        body.add(new Point(tail));
    }

    public boolean hasCollidedWithWalls() {
        Point head = body.get(0);
        return head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight;
    }

    public boolean hasCollidedWithSelf() {
        Point head = body.get(0);
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) return true;
        }
        return false;
    }

    public List<Point> getBody() {
        return body;
    }

    public Point getHead() {
        return body.get(0);
    }
}