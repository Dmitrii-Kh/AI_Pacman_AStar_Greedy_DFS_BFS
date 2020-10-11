package com.ai.labs;

public class Point {
    int x, y;
    boolean hasFork;
    int numOfNeighbours;
    int prevx, prevy;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.hasFork = false;
        this.numOfNeighbours = 0;
        this.prevx = 0;
        this.prevy = 0;
    }

    public Point(int x, int y, int prevx, int prevy) {
        this.x = x;
        this.y = y;
        this.hasFork = false;
        this.numOfNeighbours = 0;
        this.prevx = prevx;
        this.prevy = prevy;
    }

    public Point(int x, int y, boolean hasFork, int numOfNeighbours) {
        this.x = x;
        this.y = y;
        this.hasFork = hasFork;
        this.numOfNeighbours = numOfNeighbours;
    }
}
