package com.ai.labs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;

    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int PAC_ANIM_DELAY = 2;

    private int pacAnimCount = PAC_ANIM_DELAY;
    private int pacAnimDir = 1;
    private int pacmanAnimPos = 0;
    private int pacsLeft, score;

    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    private Image pacman3up, pacman3down, pacman3left, pacman3right;
    private Image pacman4up, pacman4down, pacman4left, pacman4right;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy, view_dx, view_dy;

    private long startTime = 0;
    private boolean dfs = true;

   // private Stack<Point> neighbours = new Stack<>();
    private final Deque<Point> neighbours = new ArrayDeque<>();
    private final ArrayList<Integer> visited = new ArrayList<>();

    private final short[] levelData = {
            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, //0 .. 14
            15, 15, 15, 0, 0, 0, 15, 0, 0, 0, 15, 0, 0, 0, 15,      //29   //TODO works only if TABLETKA tyt
            15, 15, 0, 0, 15, 0, 0, 0, 15, 0, 15, 0, 15, 16, 15,     //44    //TODO esli tyt to ne rabotaet
            15, 0, 15, 15, 15, 15, 15, 0, 0, 0, 15, 0, 15, 15, 15,      //59
            15, 0, 0, 0, 15, 0, 0, 0, 15, 0, 0, 0, 0, 0, 15,      //74
            15, 15, 15, 0, 15, 0, 15, 15, 15, 15, 15, 15, 15, 15, 15,      //89
            15, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 15, 0, 15,     //104
            15, 0, 15, 15, 15, 15, 15, 15, 15, 0, 15, 15, 15, 0, 15,     //119
            15, 0, 15, 0, 0, 0, 0, 0, 15, 0, 15, 0, 0, 0, 15,     //134
            15, 0, 15, 0, 15, 15, 15, 0, 0, 0, 0, 0, 15, 15, 15,     //149
            15, 0, 15, 0, 0, 0, 15, 0, 15, 15, 15, 0, 15, 0, 15,     //164
            15, 0, 15, 15, 15, 0, 15, 0, 15, 0, 0, 0, 15, 0, 15,     //179

            15, 0, 0, 0, 0, 0, 15, 15, 15, 0, 15, 15, 15, 0, 15,
            15, 15, 15, 15, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15,
            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15
    };

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board() {
        loadImages();
        initVariables();
        initBoard();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.black);
    }

    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(252, 0, 248);
        d = new Dimension(400, 400);

        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    private void doAnim() {
        pacAnimCount--;

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            pacmanAnimPos = pacmanAnimPos + pacAnimDir;

            int PACMAN_ANIM_COUNT = 4;
            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir;
            }
        }
    }

    private void playGame(Graphics2D g2d) {
        if (dying) {
            death();
        } else {
            movePacman();
            drawPacman(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String s1 = "Press D to start DFS.";
        String s2 = "Press B to start BFS.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s1, (SCREEN_SIZE - metr.stringWidth(s1)) / 2, SCREEN_SIZE / 2 + 10);
        g2d.drawString(s2, (SCREEN_SIZE - metr.stringWidth(s2)) / 2, SCREEN_SIZE / 2 - 10);
    }

    private void drawScore(Graphics2D g) {
        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(pacman3left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {
        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0)
                finished = false;
            i++;
        }

        if (finished) {
            int maxSpeed = 6;
            if (currentSpeed < maxSpeed)
                currentSpeed++;
            initLevel();
        }
    }

    private void death() {
        pacsLeft--;
        if (pacsLeft == 0) {
            inGame = false;
        }
        continueLevel();
    }

    private Point posToCoords(int pos) {         //coords start from 0,0
        int y = 0;
        int x = pos;
        while (x - N_BLOCKS >= 0) {
            y++;
            x -= N_BLOCKS;
        }
        return new Point(x, y);
    }

    private boolean isVisited(int pos) {        //pos -> index of screenData[]
        return !visited.contains(pos);
    }

//    private int pointToPos(Point p) {
//        return p.y * N_BLOCKS + p.x;
//    }

    private int pointToPos(int x, int y) {
        return y * N_BLOCKS + x;
    }

    private char checkDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            return y1 < y2 ? 'd' : 'u';
        } else {
            return x1 < x2 ? 'r' : 'l';
        }
    }

    private void isPill(int pos){
        short ch = screenData[pos];

        if ((ch & 16) != 0) {
            screenData[pos] = (short) (ch & 15);        //eats a pill
            score++;
            inGame = false;
            List<Integer> listWithoutDuplicates = visited.stream()
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("Amount of steps = " + listWithoutDuplicates.size());
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long memory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Used memory is bytes: " + memory);
        }

    }

    private void movePacman() {
        //                              **ALGO**
        //check for neighbours --> not walls && not visited
        //if no neighbours --> tp to popped Point on prev iteration (local stack of neighbours?)
        //pop Point from neighbours

        Deque<Point> localN = new ArrayDeque<>();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;

        int pos = pointToPos(x, y);
        isPill(pos);

        int posUp = pointToPos(x, y-1);
        int posDown = pointToPos(x, y+1);
        int posLeft = pointToPos(x-1, y);
        int posRight = pointToPos(x+1, y);

        short up = 8, down = 2, left, right;
        if (y != 0) up = screenData[posUp];
        if (y != 14) down = screenData[posDown];
        left = screenData[posLeft];
        right = screenData[posRight];

        if(dfs) {
            if ((down & 2) == 0 && isVisited(posDown)) localN.push(posToCoords(posDown));
            if ((up & 8) == 0 && isVisited(posUp)) localN.push(posToCoords(posUp));
            if ((left & 4) == 0 && isVisited(posLeft)) localN.push(posToCoords(posLeft));
            if ((right & 1) == 0 && isVisited(posRight)) localN.push(posToCoords(posRight));

            //pop & append to visited
            visited.add(pointToPos(x, y));

            Point next;
            if (localN.isEmpty()) {
                next = neighbours.pop();
                pacman_x = next.x * BLOCK_SIZE;
                pacman_y = next.y * BLOCK_SIZE;
            } else {
                next = localN.pop();
                while (!localN.isEmpty()) {
                    neighbours.push(localN.pop());
                }
            }
            findDirection(x, y, next);
        } else {
            if ((down & 2) == 0 && isVisited(posDown)) neighbours.addLast(posToCoords(posDown));
            if ((up & 8) == 0 && isVisited(posUp)) neighbours.addLast(posToCoords(posUp));
            if ((left & 4) == 0 && isVisited(posLeft)) neighbours.addLast(posToCoords(posLeft));
            if ((right & 1) == 0 && isVisited(posRight)) neighbours.addLast(posToCoords(posRight));

            //pop & append to visited
            visited.add(pointToPos(x, y));

            Point next;
            next = neighbours.pollFirst();
            pacman_x = next.x * BLOCK_SIZE;
            pacman_y = next.y * BLOCK_SIZE;
            findDirection(x, y, next);
        }
        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
            pacmand_x = req_dx;
            pacmand_y = req_dy;
            //change avatar
            view_dx = pacmand_x;
            view_dy = pacmand_y;
        }
        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pacmand_x = req_dx;
            pacmand_y = req_dy;
            view_dx = pacmand_x;
            view_dy = pacmand_y;

        }
        int PACMAN_SPEED = 12;
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void findDirection(int x, int y, Point next) {
        System.out.println(x + "-x, " + y + "-y");
        System.out.println(next.x + "-x.next, " + next.y + "-y.next");

        switch (checkDirection(x, y, next.x, next.y)) {
            case 'r':
                System.out.println("r");
                req_dx = 1;
                req_dy = 0;
                break;
            case 'l':
                System.out.println("l");
                req_dx = -1;
                req_dy = 0;
                break;
            case 'u':
                System.out.println("u");
                req_dx = 0;
                req_dy = -1;
                break;
            case 'd':
                System.out.println("d");
                req_dx = 0;
                req_dy = 1;
                break;
        }
    }

    private void drawPacman(Graphics2D g2d) {
        if (view_dx == -1) {
            drawPacman(g2d, pacman2left, pacman3left, pacman4left);
        } else if (view_dx == 1) {
            drawPacman(g2d, pacman2right, pacman3right, pacman4right);
        } else if (view_dy == -1) {
            drawPacman(g2d, pacman2up, pacman3up, pacman4up);
        } else {
            drawPacman(g2d, pacman2down, pacman3down, pacman4down);
        }
    }

    private void drawPacman(Graphics2D g2d, Image pacman2left, Image pacman3left, Image pacman4left) {
        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {
        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {
                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));
                if ((screenData[i] & 1) != 0)
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                if ((screenData[i] & 2) != 0)
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }
                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }
                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }
                i++;
            }
        }
    }

    private void initGame() {
        score = 0;
        initLevel();
        currentSpeed = 3;
    }

    private void initLevel() {
        if(score!=0) {
            long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) + "ms");
        }
        startTime = System.currentTimeMillis();
        neighbours.clear();
        visited.clear();
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }
        continueLevel();
    }

    private void continueLevel() {
        pacman_x = 7 * BLOCK_SIZE;
        pacman_y = 11 * BLOCK_SIZE;
        //visited.add(pointToPos(7,11));
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
    }

    private void loadImages() {
        pacman1 = new ImageIcon("src/main/resources/images/pacman.png").getImage();
        pacman2up = new ImageIcon("src/main/resources/images/up1.png").getImage();
        pacman3up = new ImageIcon("src/main/resources/images/up2.png").getImage();
        pacman4up = new ImageIcon("src/main/resources/images/up3.png").getImage();
        pacman2down = new ImageIcon("src/main/resources/images/down1.png").getImage();
        pacman3down = new ImageIcon("src/main/resources/images/down2.png").getImage();
        pacman4down = new ImageIcon("src/main/resources/images/down3.png").getImage();
        pacman2left = new ImageIcon("src/main/resources/images/left1.png").getImage();
        pacman3left = new ImageIcon("src/main/resources/images/left2.png").getImage();
        pacman4left = new ImageIcon("src/main/resources/images/left3.png").getImage();
        pacman2right = new ImageIcon("src/main/resources/images/right1.png").getImage();
        pacman3right = new ImageIcon("src/main/resources/images/right2.png").getImage();
        pacman4right = new ImageIcon("src/main/resources/images/right3.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 'b' || key == 'B') {
                    dfs = false;
                    inGame = true;
                    initGame();
                }
                if (key == 'd' || key == 'D') {
                    dfs = true;
                    inGame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}