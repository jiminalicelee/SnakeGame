import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Map;
import java.io.File;
import java.io.IOException;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        this.add(new GamePanel());
        this.setTitle("Java 2D Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int TILE_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (TILE_SIZE * TILE_SIZE);
    private static final int DELAY = 100; // Game speed in ms
    private static final int START_DELAY_SECONDS = 3;
    private static final Map<Character, Character> OPPOSITE = Map.of(
        'L', 'R',
        'R', 'L',
        'U', 'D',
        'D', 'U'
    );

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private final Queue<Character> directionQueue = new ArrayDeque<>();
    private int bodyParts = 3;
    private int foodX;
    private int foodY;
    private char direction = 'R'; // 'U', 'D', 'L', 'R'
    private boolean running = false;
    private boolean won = false;
    private boolean starting = true;
    private int countdown = START_DELAY_SECONDS;
    private Timer timer;
    private Timer countdownTimer;
    private final Random random = new Random();
    private static final Font BASE_FONT = loadFont();

    private static Font loadFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,
                    new File("static/Press_Start_2P/PressStart2P-Regular.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            return new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    private void startGame() {
        newFood();
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown <= 0) {
                countdownTimer.stop();
                starting = false;
                running = true;
                timer = new Timer(DELAY, this);
                timer.start();
            }
            repaint();
        });
        countdownTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        if (starting) {
            drawCountdown(g);
        } else if (running) {
            // Draw Food
            g.setColor(Color.RED);
            g.fillOval(foodX, foodY, TILE_SIZE, TILE_SIZE);

            // Draw Snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
            }

            // Draw Score
            g.setColor(Color.WHITE);
            g.setFont(BASE_FONT.deriveFont(14f));
            g.drawString("Score: " + (bodyParts - 3), 10, 20);
        } else {
            endGame(g);
        }
    }

    private boolean overlapsSnake(int px, int py) {
        for (int i = 0; i < bodyParts; i++) {
            if (px == x[i] && py == y[i]) {
                return true;
            }
        }
        return false;
    }

    private void newFood() {
        // Generate food at coordinates that do not overlap with the snake body
        do {
            foodX = random.nextInt(SCREEN_WIDTH / TILE_SIZE) * TILE_SIZE;
            foodY = random.nextInt(SCREEN_HEIGHT / TILE_SIZE) * TILE_SIZE;
        } while (overlapsSnake(foodX, foodY));
    }

    private void checkValidMove() {
        Character newDirection;
        while ((newDirection = directionQueue.poll()) != null) {
            if (OPPOSITE.get(newDirection) != direction) {
                direction = newDirection;
                break;
            }
        }
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] = y[0] - TILE_SIZE;
            case 'D' -> y[0] = y[0] + TILE_SIZE;
            case 'L' -> x[0] = x[0] - TILE_SIZE;
            case 'R' -> x[0] = x[0] + TILE_SIZE;
        }
    }

    private void checkFood() {
        if ((x[0] == foodX) && (y[0] == foodY)) {
            bodyParts++;
            if (bodyParts == GAME_UNITS) {
                won = true;
                running = false;
                timer.stop();
                return;
            }
            newFood();
        }
    }

    private void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts - 1; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }
        // Check head collision with borders
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    private void drawCountdown(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(BASE_FONT.deriveFont(40f));
        FontMetrics titleMetrics = getFontMetrics(g.getFont());
        String title = "Get Ready!";
        g.drawString(title, (SCREEN_WIDTH - titleMetrics.stringWidth(title)) / 2, SCREEN_HEIGHT / 2 - 30);

        g.setFont(BASE_FONT.deriveFont(60f));
        FontMetrics countMetrics = getFontMetrics(g.getFont());
        String countText = String.valueOf(countdown);
        g.drawString(countText, (SCREEN_WIDTH - countMetrics.stringWidth(countText)) / 2, SCREEN_HEIGHT / 2 + 40);
    }

    private void endGame(Graphics g) {
        g.setFont(BASE_FONT.deriveFont(40f));
        FontMetrics metrics = getFontMetrics(g.getFont());
        if (won) {
            g.setColor(Color.GREEN);
            g.drawString("YOU WIN!", (SCREEN_WIDTH - metrics.stringWidth("YOU WIN!")) / 2, SCREEN_HEIGHT / 2);
        } else {
            g.setColor(Color.RED);
            g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
        }
        g.setColor(Color.WHITE);
        g.setFont(BASE_FONT.deriveFont(20f));
        FontMetrics scoreMetrics = getFontMetrics(g.getFont());
        String finalScore = "Final Score: " + (bodyParts - 3);
        g.drawString(finalScore, (SCREEN_WIDTH - scoreMetrics.stringWidth(finalScore)) / 2, (SCREEN_HEIGHT / 2) + 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            checkValidMove();
            move();
            checkFood();
            checkCollisions();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (directionQueue.size() < 2)
                        directionQueue.add('L');
                }
                case KeyEvent.VK_RIGHT -> {
                    if (directionQueue.size() < 2)
                        directionQueue.add('R');
                }
                case KeyEvent.VK_UP -> {
                    if (directionQueue.size() < 2)
                        directionQueue.add('U');
                }
                case KeyEvent.VK_DOWN -> {
                    if (directionQueue.size() < 2)
                        directionQueue.add('D');
                }
            }
        }
    }
}