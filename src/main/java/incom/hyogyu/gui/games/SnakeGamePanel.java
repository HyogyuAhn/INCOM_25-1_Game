package incom.hyogyu.gui.games;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SnakeGamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 32;
    private static final int GAME_WIDTH = 1344;
    private static final int GAME_HEIGHT = 768;
    private static final int DELAY = 120;

    private List<Point> snakeBody;
    private Point food;
    private Direction currentDirection;
    private boolean running;

    private Timer timer;
    private final KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            handleDirectionChange(e.getKeyCode());
        }
    };;

    private final HashMap<String, Image> images = new HashMap<>();

    public SnakeGamePanel() {
        setFocusable(true);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        enableKeyListener(false);

        food = null;
        running = false;
    }

    public void startGame() {
        if (!running) {
            running = true;

            snakeBody = new ArrayList<>();
            snakeBody.add(new Point(100, 100));

            currentDirection = Direction.RIGHT;

            loadAssets();
            spawnFood();

            timer = new Timer(DELAY, this);
            timer.start();
            System.out.println("[SNAKE-DEBUG] 게임 시작 및 타이머 ON");
        }

    }

    public void stopGame() {
        if (timer != null) {
            timer.stop(); // 타이머 중단
            System.out.println("[SNAKE-DEBUG] 게임 중단");
        }
    }
    /*
        addHierarchyListener(e -> {
            if (isShowing() && isFocusable()) {
                requestFocusInWindow();
            }
        });
     */

    public void enableKeyListener(boolean enable) {
        if (enable) {
            addKeyListener(keyAdapter);
            requestFocusInWindow();
        } else {
            removeKeyListener(keyAdapter);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }


    private Image loadImage(String fileName) {
        return new ImageIcon(getClass().getResource("/images/snakeAssets/" + fileName)).getImage();
    }

    private void loadAssets() {
        images.put("headUp", loadImage("head_up.png"));
        images.put("headDown", loadImage("head_down.png"));
        images.put("headLeft", loadImage("head_left.png"));
        images.put("headRight", loadImage("head_right.png"));

        images.put("tailUp", loadImage("tail_up.png"));
        images.put("tailDown", loadImage("tail_down.png"));
        images.put("tailLeft", loadImage("tail_left.png"));
        images.put("tailRight", loadImage("tail_right.png"));

        images.put("bodyVertical", loadImage("body_vertical.png"));
        images.put("bodyHorizontal", loadImage("body_horizontal.png"));

        images.put("bodyTopLeft", loadImage("body_topleft.png"));
        images.put("bodyTopRight", loadImage("body_topright.png"));
        images.put("bodyBottomLeft", loadImage("body_bottomleft.png"));
        images.put("bodyBottomRight", loadImage("body_bottomright.png"));

        images.put("apple", loadImage("apple.png"));
    }

    private void spawnFood() {
        Random random = new Random();
        int x = random.nextInt(GAME_WIDTH / TILE_SIZE) * TILE_SIZE;
        int y = random.nextInt(GAME_HEIGHT / TILE_SIZE) * TILE_SIZE;

        food = new Point(x, y);
        System.out.println("[DEBUG] New Food spawned at: " + food.x + ", " + food.y);
    }

    private void handleDirectionChange(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP -> {
                if (currentDirection != Direction.DOWN) currentDirection = Direction.UP;
            }
            case KeyEvent.VK_DOWN -> {
                if (currentDirection != Direction.UP) currentDirection = Direction.DOWN;
            }
            case KeyEvent.VK_LEFT -> {
                if (currentDirection != Direction.RIGHT) currentDirection = Direction.LEFT;
            }
            case KeyEvent.VK_RIGHT -> {
                if (currentDirection != Direction.LEFT) currentDirection = Direction.RIGHT;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            moveSnake();
            checkCollision();
            repaint();
        }
    }


    private void moveSnake() {
        Point head = new Point(snakeBody.get(0));
        switch (currentDirection) {
            case UP -> head.y -= TILE_SIZE;
            case DOWN -> head.y += TILE_SIZE;
            case LEFT -> head.x -= TILE_SIZE;
            case RIGHT -> head.x += TILE_SIZE;
        }
        snakeBody.add(0, head);

        System.out.println("[DEBUG] Snake Head: " + head.x + ", " + head.y);
        System.out.println("[DEBUG] Food: " + food.x + ", " + food.y);

        if (Math.abs(head.x - food.x) < TILE_SIZE / 2 && Math.abs(head.y - food.y) < TILE_SIZE / 2) {
            spawnFood();
            System.out.println("[DEBUG] Food eaten! New Food: " + food.x + ", " + food.y);
            System.out.println("[DEBUG] Snake length: " + snakeBody.size());
        } else {
            snakeBody.remove(snakeBody.size() - 1);
        }
    }

    private void checkCollision() {
        Point head = snakeBody.get(0);

        if (head.x < 0 || head.x >= GAME_WIDTH || head.y < 0 || head.y >= GAME_HEIGHT) {
            running = false;
            timer.stop();
        }

        for (int i = 1; i < snakeBody.size(); i++) {
            if (head.equals(snakeBody.get(i))) {
                running = false;
                timer.stop();
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (running) {
            g.drawImage(images.get("apple"), food.x, food.y, TILE_SIZE, TILE_SIZE, this);

            for (int i = 0; i < snakeBody.size(); i++) {
                Point segment = snakeBody.get(i);
                Image image = getSnakeSegmentImage(i);
                g.drawImage(image, segment.x, segment.y, TILE_SIZE, TILE_SIZE, this);
            }
        } else {
            showGameOver(g);
        }
    }

    private Image getSnakeSegmentImage(int index) {
        if (index == 0) {
            return switch (currentDirection) {
                case UP -> images.get("headUp");
                case DOWN -> images.get("headDown");
                case LEFT -> images.get("headLeft");
                case RIGHT -> images.get("headRight");
            };
        } else if (index == snakeBody.size() - 1) {
            Point tail = snakeBody.get(index);
            Point beforeTail = snakeBody.get(index - 1);
            return getTailImage(tail, beforeTail);
        } else {
            return getBodyImage(index);
        }
    }

    private Image getTailImage(Point tail, Point beforeTail) {
        if (tail.x == beforeTail.x && tail.y < beforeTail.y) return images.get("tailUp");
        if (tail.x == beforeTail.x && tail.y > beforeTail.y) return images.get("tailDown");
        if (tail.y == beforeTail.y && tail.x < beforeTail.x) return images.get("tailLeft");
        if (tail.y == beforeTail.y && tail.x > beforeTail.x) return images.get("tailRight");
        return images.get("tailUp");
    }

    private Image getBodyImage(int index) {
        Point current = snakeBody.get(index);
        Point next = snakeBody.get(index + 1);
        Point prev = snakeBody.get(index - 1);

        if (prev.x == next.x) return images.get("bodyVertical");
        if (prev.y == next.y) return images.get("bodyHorizontal");

        if (prev.x < current.x && next.y < current.y || next.x < current.x && prev.y < current.y) return images.get("bodyTopLeft");
        if (prev.x > current.x && next.y < current.y || next.x > current.x && prev.y < current.y) return images.get("bodyTopRight");
        if (prev.x < current.x && next.y > current.y || next.x < current.x && prev.y > current.y) return images.get("bodyBottomLeft");
        if (prev.x > current.x && next.y > current.y || next.x > current.x && prev.y > current.y) return images.get("bodyBottomRight");

        return images.get("bodyHorizontal");
    }

    private void showGameOver(Graphics g) {
        String message = "Game Over";
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(message, (GAME_WIDTH - metrics.stringWidth(message)) / 2, GAME_HEIGHT / 2);
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}