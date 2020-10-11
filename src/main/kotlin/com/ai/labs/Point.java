package com.ai.labs;

public class Point {
    int x, y;
    boolean hasFork;
    int numOfNeighbours;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.hasFork = false;
        this.numOfNeighbours = 0;
    }

    public Point(int x, int y, boolean hasFork, int numOfNeighbours) {
        this.x = x;
        this.y = y;
        this.hasFork = hasFork;
        this.numOfNeighbours = numOfNeighbours;
    }
}
