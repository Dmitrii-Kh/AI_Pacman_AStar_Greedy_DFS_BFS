package com.ai.labs;

import org.jetbrains.annotations.NotNull;

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

public class Board extends JPanel implements ActionListener, PacmanRunner {

    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;

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
    private final State currState = getCurrState();

    // private Stack<Point> neighbours = new Stack<>();
    private final Deque<Point> neighbours = new ArrayDeque<>();
    private ArrayDeque<Point> resultPath = new ArrayDeque<>();
    private ArrayList<Point> resultPathUnfiltered = new ArrayList<>();
    private final ArrayList<Integer> visited = new ArrayList<>();
    private final Map<Integer, Point> visitedHeuristical = new HashMap<>();
    private Point goal = new Point(13, 2);
    private int currentDist = 0;
    private int currentHeuristic = 0;

    enum Algo {
        DFS,
        BFS,
        Greedy,
        AStar
    }
    private Algo algorithm;

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

    @NotNull
    @Override
    public State getCurrState() {
        return new State(false, false, false);
    }

    @Override
    public void setCurrState(@NotNull State currState) {
        currState = this.currState;
    }

    @Override
    public void findSon(){
        switch(algorithm){
            case DFS: findSonDFS();
            break;
            case BFS: findSonBFS();
            break;
            case Greedy: findSonGreedy();
            break;
            case AStar: findSonAStar();
            break;
        }
    }

    public void findSonDFS() {
        Deque<Point> localN = new ArrayDeque<>();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;

        int pos = pointToPos(x, y);
        if (checkObject(pos)) return;

        int posUp = pointToPos(x, y - 1);
        int posDown = pointToPos(x, y + 1);
        int posLeft = pointToPos(x - 1, y);
        int posRight = pointToPos(x + 1, y);

        short up = 8, down = 2, left, right;
        if (y != 0) up = screenData[posUp];
        if (y != 14) down = screenData[posDown];
        left = screenData[posLeft];
        right = screenData[posRight];

            if ((down & 2) == 0 && isNotVisited(posDown)) localN.push(posToCoords(posDown));
            if ((up & 8) == 0 && isNotVisited(posUp)) localN.push(posToCoords(posUp));
            if ((left & 4) == 0 && isNotVisited(posLeft)) localN.push(posToCoords(posLeft));
            if ((right & 1) == 0 && isNotVisited(posRight)) localN.push(posToCoords(posRight));

            //pop & append to visited
            visited.add(pointToPos(x, y));

            if (localN.size() > 1) {
                resultPath.push(new Point(x, y, true, localN.size()));
            } else if (localN.size() == 1) {
                resultPath.push(new Point(x, y));
            }

            Point next;
            if (localN.isEmpty()) {
                while (true) {
                    while (!resultPath.peek().hasFork) {
                        resultPath.pop();
                    }
                    resultPath.peek().numOfNeighbours -= 1;

                    if (resultPath.peek().numOfNeighbours == 0) {
                        resultPath.pop();
                    } else {
                        break;
                    }
                }
                next = neighbours.pop();
                pacman_x = next.x * BLOCK_SIZE;
                pacman_y = next.y * BLOCK_SIZE;
                return;
            } else {
                next = localN.pop();
                while (!localN.isEmpty()) {
                    neighbours.push(localN.pop());
                }
            }
            findDirection(x, y, next);

        changePacmanProperties();
    }

    public void findSonBFS() {
        Deque<Point> localN = new ArrayDeque<>();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;

        int pos = pointToPos(x, y);
        if (checkObject(pos)) return;

        int posUp = pointToPos(x, y - 1);
        int posDown = pointToPos(x, y + 1);
        int posLeft = pointToPos(x - 1, y);
        int posRight = pointToPos(x + 1, y);

        short up = 8, down = 2, left, right;
        if (y != 0) up = screenData[posUp];
        if (y != 14) down = screenData[posDown];
        left = screenData[posLeft];
        right = screenData[posRight];

            if ((down & 2) == 0 && isNotVisited(posDown)) neighbours.addLast(posToCoords(posDown));
            if ((up & 8) == 0 && isNotVisited(posUp)) neighbours.addLast(posToCoords(posUp));
            if ((left & 4) == 0 && isNotVisited(posLeft)) neighbours.addLast(posToCoords(posLeft));
            if ((right & 1) == 0 && isNotVisited(posRight)) neighbours.addLast(posToCoords(posRight));

            //pop & append to visited
            visited.add(pointToPos(x, y));

            Point next;
            next = neighbours.pollFirst();
            pacman_x = next.x * BLOCK_SIZE;
            pacman_y = next.y * BLOCK_SIZE;
            findDirection(x, y, next);

        changePacmanOrientation();
    }

    public void findSonAStar() {
        List<Point> localN = new ArrayList<>();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;
        int newDist = ++currentDist;

        int pos = pointToPos(x, y);
        if (checkObject(pos)) return;

        int posUp = pointToPos(x, y - 1);
        int posDown = pointToPos(x, y + 1);
        int posLeft = pointToPos(x - 1, y);
        int posRight = pointToPos(x + 1, y);

        short up = 8, down = 2, left, right;
        if (y != 0) up = screenData[posUp];
        if (y != 14) down = screenData[posDown];
        left = screenData[posLeft];
        right = screenData[posRight];

        if ((down & 2) == 0) {
            if (isNotVisitedWithDist(posDown)) {
                localN.add(posToCoords(posDown));
            } else {
                if(deprecateOldIfCurrentShorter(posDown, newDist)) {
                    localN.add(posToCoords(posDown));
                }
            }
        }

        if ((up & 8) == 0) {
            if (isNotVisitedWithDist(posUp)) {
                localN.add(posToCoords(posUp));
            } else {
                if(deprecateOldIfCurrentShorter(posUp, newDist)) {
                    localN.add(posToCoords(posUp));
                }
            }
        }

        if ((left & 4) == 0) {
            if (isNotVisitedWithDist(posLeft)) {
                localN.add(posToCoords(posLeft));
            } else {
                if(deprecateOldIfCurrentShorter(posLeft, newDist)) {
                    localN.add(posToCoords(posLeft));
                }
            }
        }

        if ((right & 1) == 0) {
            if (isNotVisitedWithDist(posRight)) {
                localN.add(posToCoords(posRight));
            } else {
                if(deprecateOldIfCurrentShorter(posRight, newDist)) {
                    localN.add(posToCoords(posRight));
                }
            }
        }

        for (int i = 0; i < localN.size(); i++) {
            localN.get(i).distance = newDist;
            localN.get(i).heuristic = heuristic(localN.get(i));
        }

        Stack<Point> prioritizedStack = prioritizeOnHeuristicAndDistance(localN);
        while(!prioritizedStack.isEmpty()){
            neighbours.addLast(prioritizedStack.pop());
        }

        //append current to visited
        Point curr = new Point(x,y);
        curr.distance = currentDist;
        curr.heuristic = currentHeuristic;
        visitedHeuristical.put(pointToPos(x, y), curr);

        //pop next
        Point next;
        next = neighbours.pollFirst();
        pacman_x = next.x * BLOCK_SIZE;
        pacman_y = next.y * BLOCK_SIZE;
        currentDist = next.distance;
        currentHeuristic = next.heuristic;
        findDirection(x, y, next);

        changePacmanOrientation();      //todo Orientation! not Properties
    }

    private Stack<Point> prioritizeOnHeuristicAndDistance(List<Point> list){
        Stack<Point> result = new Stack<>();
        int largestHeuristic;
        int indexOfLargest;

        while(!list.isEmpty()){
            largestHeuristic = -1;
            indexOfLargest = -1;
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).distance + list.get(i).heuristic > largestHeuristic) {
                    largestHeuristic = list.get(i).distance + list.get(i).heuristic;
                    indexOfLargest = i;
                }
            }
            result.push(list.get(indexOfLargest));
            list.remove(indexOfLargest);
        }
        return result;
    }

    private boolean deprecateOldIfCurrentShorter(int pos, int newDist) {
        boolean res = false;
        for (Map.Entry<Integer,Point> entry : visitedHeuristical.entrySet()){
            if(entry.getKey() == pos && newDist < entry.getValue().distance){
                visitedHeuristical.remove(entry.getKey());
                res = true;
            }
        }

        return res;
    }

    private Stack<Point> prioritizeNeighbours(Stack<Point> s) {
        Map<Point, Integer> map = new HashMap<>();
        Stack<Point> result = new Stack<>();
        Point popped;
        Point smallest;     //shortest distance to the pill i.e. 1st priority

        while (!s.isEmpty()) {
            popped = s.pop();
            map.put(popped, heuristic(popped));
        }

        while (!map.isEmpty()) {
            smallest = findSmallest(map);
            map.remove(smallest);           //todo verify removal one more time
            result.push(smallest);
        }
        return result;
    }

    private Point findSmallest(Map<Point, Integer> map) {
        int smallest = Integer.MAX_VALUE;
        Point res = new Point(0, 0);
        for (Point p : map.keySet()) {
            if (map.get(p) < smallest) {
                smallest = map.get(p);
                res = p;
            }
        }
        return res;
    }

    private Integer heuristic(Point popped) {
        return Math.abs(popped.x - goal.x) + Math.abs(popped.y - goal.y);
    }

    public void findSonGreedy() {
        Stack<Point> localN = new Stack<>();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;

        int pos = pointToPos(x, y);
        if (checkObject(pos)) return;

        int posUp = pointToPos(x, y - 1);
        int posDown = pointToPos(x, y + 1);
        int posLeft = pointToPos(x - 1, y);
        int posRight = pointToPos(x + 1, y);

        short up = 8, down = 2, left, right;
        if (y != 0) up = screenData[posUp];
        if (y != 14) down = screenData[posDown];
        left = screenData[posLeft];
        right = screenData[posRight];

        if ((down & 2) == 0 && isNotVisited(posDown)) localN.push(posToCoords(posDown));
        if ((up & 8) == 0 && isNotVisited(posUp)) localN.push(posToCoords(posUp));
        if ((left & 4) == 0 && isNotVisited(posLeft)) localN.push(posToCoords(posLeft));
        if ((right & 1) == 0 && isNotVisited(posRight)) localN.push(posToCoords(posRight));

        //append to visited
        visited.add(pointToPos(x, y));

        Point next;
        if (localN.isEmpty()) {
            next = neighbours.pop();
            // **error** resultRoute.add(new Point(next.x, next.y, x, y));
            pacman_x = next.x * BLOCK_SIZE;
            pacman_y = next.y * BLOCK_SIZE;
            return;
        } else if (localN.size() > 1) {
            localN = prioritizeNeighbours(localN);
            while (localN.size() != 1) {
                neighbours.push(localN.pop());
            }
            next = localN.pop();
            //todo resultRoute.add(new Point(next.x, next.y, x, y));
        } else {        //if only 1 neighbour
            next = localN.pop();
            //todo resultRoute.add(new Point(next.x, next.y, x, y));
        }

        findDirection(x, y, next);
        changePacmanProperties();
    }

    private void changePacmanProperties() {
        changePacmanOrientation();
        int PACMAN_SPEED = 12;
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void changePacmanOrientation() {
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
    }


    @Override
    public void pathWeight() {
        if(algorithm == Algo.AStar) {
            System.out.println("Amount of steps = " + visitedHeuristical.size());
            return;
        }

        List<Integer> listWithoutDuplicates = visited.stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println("Amount of steps = " + listWithoutDuplicates.size());
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
        initState();
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
        if (currState.getDying()) {
            death();
//        } else if (currState.getShowResultPath()) {
//            movePacmanFinal();
//            drawPacman(g2d);
//            checkMaze();
        } else {
            findSon();
            drawPacman(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 50, SCREEN_SIZE - 100, 90);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 50, SCREEN_SIZE - 100, 90);

        String s1 = "Press D to start DFS";
        String s2 = "Press B to start BFS";
        String s3 = "Press A to start A*";
        String s4 = "Press G to start Greedy";

        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s1, (SCREEN_SIZE - metr.stringWidth(s1)) / 2, SCREEN_SIZE / 2 - 30);
        g2d.drawString(s2, (SCREEN_SIZE - metr.stringWidth(s2)) / 2, SCREEN_SIZE / 2 - 10);
        g2d.drawString(s3, (SCREEN_SIZE - metr.stringWidth(s2)) / 2, SCREEN_SIZE / 2 + 10);
        g2d.drawString(s4, (SCREEN_SIZE - metr.stringWidth(s2)) / 2, SCREEN_SIZE / 2 + 30);
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
            currState.setInGame(false);
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

    private boolean isNotVisited(int pos) {        //pos -> index of screenData[]
        return !visited.contains(pos);
    }

    private boolean isNotVisitedWithDist(int pos) {        //pos -> index of screenData[]
        return !visitedHeuristical.containsKey(pos);
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

    private boolean contains(ArrayDeque<Point> list, Point p) {
        for (Point point : list) {
            if (point.x == p.x && point.y == p.y) return true;
        }
        return false;
    }

    private ArrayDeque<Point> removeDuplicates(ArrayDeque<Point> list) {
        ArrayDeque<Point> newList = new ArrayDeque<>();
        for (Point element : list) {
            if (!contains(newList, element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    @Override
    public boolean checkObject(int pos) {
        short ch = screenData[pos];

        if ((ch & 16) != 0) {
            screenData[pos] = (short) (ch & 15);        //eats a pill
            score++;

            // resultPath.push(posToCoords(pos));
//            System.out.println("====================");
//            for(Point p : resultRoute){
//                System.out.println("x : " + p.x + " y : " + p.y + " prevx : " + p.prevx + " prevy : " + p.prevy);
//            }
//            System.out.println("====================");

            //todo createResultPath(pacman_x/BLOCK_SIZE, pacman_y/BLOCK_SIZE);

            pacman_x = 7 * BLOCK_SIZE;
            pacman_y = 11 * BLOCK_SIZE;
            currState.setInGame(false);

            pathWeight();
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long memory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Used memory is bytes: " + memory);
            return true;
        }
        return false;
    }

    private void createResultPath(int finalX, int finalY) {
        //resultPath.push(new Point(finalX, finalY));
        int x = finalX;
        int y = finalY;
        do {
            for (Point p : resultPathUnfiltered) {
                if (p.x == x && p.y == y) {
                    resultPath.push(p);
                    x = p.prevx;
                    y = p.prevy;
                    break;
                }
            }
        } while (x != 7 && y != 11);
    }



    private void findDirection(int x, int y, Point next) {
        findDirections(next, x, y);
    }

    private void movePacmanFinal() {
        if (resultPath.size() == 0) {
            currState.setInGame(false);
            currState.setShowResultPath(false);
            resultPath.clear();
            resultPathUnfiltered.clear();
            return;
        }

        Point next = resultPath.pop();

        int x = pacman_x / BLOCK_SIZE;
        int y = pacman_y / BLOCK_SIZE;

        findDirections(next, x, y);
        changePacmanProperties();
    }

    private void findDirections(Point next, int x, int y) {
        switch (checkDirection(x, y, next.x, next.y)) {
            case 'r':
                req_dx = 1;
                req_dy = 0;
                break;
            case 'l':
                req_dx = -1;
                req_dy = 0;
                break;
            case 'u':
                req_dx = 0;
                req_dy = -1;
                break;
            case 'd':
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

    @Override
    public void initState() {
        score = 0;
        initLevel();
        currentSpeed = 3;
    }

    private void initLevel() {
        if (score != 0) {
            long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) + "ms");
        }
        startTime = System.currentTimeMillis();
        neighbours.clear();
        visited.clear();
        visitedHeuristical.clear();
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
        //todo resultRoute.add(new Point(7,11));
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        currState.setDying(false);
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

        if (currState.getInGame()) {
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

            if (currState.getInGame()) {
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
                    currState.setInGame(false);
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                switch(key){
                    case 'b':
                    case 'B':
                        algorithm = Algo.BFS;
                        break;
                    case 'd':
                    case 'D':
                        algorithm = Algo.DFS;
                        break;
                    case 'a':
                    case 'A':
                        algorithm = Algo.AStar;
                        break;
                    case 'g':
                    case 'G':
                        algorithm = Algo.Greedy;
                        break;
                }
                    currState.setInGame(true);
                    initState();
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