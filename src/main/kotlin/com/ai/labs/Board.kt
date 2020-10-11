//package com.ai.labs
//
//import java.awt.*
//import java.awt.event.ActionEvent
//import java.awt.event.ActionListener
//import java.awt.event.KeyAdapter
//import java.awt.event.KeyEvent
//import java.util.*
//import java.util.stream.Collectors
//import javax.swing.ImageIcon
//import javax.swing.JPanel
//import javax.swing.Timer
//
//class Board : JPanel(), ActionListener, PacmanRunner {
//    private var d: Dimension? = null
//    private val smallFont = Font("Helvetica", Font.BOLD, 14)
//    private val ii: Image? = null
//    private val dotColor = Color(192, 192, 0)
//    private var mazeColor: Color? = null
//    private var dying = false
//    private var showResultPath = false
//    private val BLOCK_SIZE = 24
//    private val N_BLOCKS = 15
//    private val SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE
//    private val PAC_ANIM_DELAY = 2
//    private var pacAnimCount = PAC_ANIM_DELAY
//    private var pacAnimDir = 1
//    private var pacmanAnimPos = 0
//    private var pacsLeft = 0
//    private var score = 0
//    private var pacman1: Image? = null
//    private var pacman2up: Image? = null
//    private var pacman2left: Image? = null
//    private var pacman2right: Image? = null
//    private var pacman2down: Image? = null
//    private var pacman3up: Image? = null
//    private var pacman3down: Image? = null
//    private var pacman3left: Image? = null
//    private var pacman3right: Image? = null
//    private var pacman4up: Image? = null
//    private var pacman4down: Image? = null
//    private var pacman4left: Image? = null
//    private var pacman4right: Image? = null
//    private var pacman_x = 0
//    private var pacman_y = 0
//    private var pacmand_x = 0
//    private var pacmand_y = 0
//    private var req_dx = 0
//    private var req_dy = 0
//    private var view_dx = 0
//    private var view_dy = 0
//    private var startTime: Long = 0
//    private var dfs = true
//
//    override var currState: State = State(dying = false, inGame = false, showResultPath = false)
//
//    // private Stack<Point> neighbours = new Stack<>();
//    private val neighbours: Deque<Point> = ArrayDeque()
//    private var resultPath = ArrayDeque<Point>()
//    private val visited = ArrayList<Int>()
//    private val levelData = shortArrayOf(
//            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,  //0 .. 14
//            15, 15, 15, 0, 0, 0, 15, 0, 0, 0, 15, 0, 0, 0, 15,  //29   //TODO works only if TABLETKA tyt
//            15, 15, 0, 0, 15, 0, 0, 0, 15, 0, 15, 0, 15, 16, 15,  //44    //TODO esli tyt to ne rabotaet
//            15, 0, 15, 15, 15, 15, 15, 0, 0, 0, 15, 0, 15, 15, 15,  //59
//            15, 0, 0, 0, 15, 0, 0, 0, 15, 0, 0, 0, 0, 0, 15,  //74
//            15, 15, 15, 0, 15, 0, 15, 15, 15, 15, 15, 15, 15, 15, 15,  //89
//            15, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 15, 0, 15,  //104
//            15, 0, 15, 15, 15, 15, 15, 15, 15, 0, 15, 15, 15, 0, 15,  //119
//            15, 0, 15, 0, 0, 0, 0, 0, 15, 0, 15, 0, 0, 0, 15,  //134
//            15, 0, 15, 0, 15, 15, 15, 0, 0, 0, 0, 0, 15, 15, 15,  //149
//            15, 0, 15, 0, 0, 0, 15, 0, 15, 15, 15, 0, 15, 0, 15,  //164
//            15, 0, 15, 15, 15, 0, 15, 0, 15, 0, 0, 0, 15, 0, 15,  //179
//            15, 0, 0, 0, 0, 0, 15, 15, 15, 0, 15, 15, 15, 0, 15,
//            15, 15, 15, 15, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15,
//            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15
//    )
//    private var currentSpeed = 3
//    private lateinit var screenData: IntArray
//    private var timer: Timer? = null
//    private fun initBoard() {
//        addKeyListener(TAdapter())
//        isFocusable = true
//        background = Color.black
//    }
//
//    //    @NotNull
//    //    @Override
//    //    public State getCurrState() {
//    //       //TODO()
//    //    }
//    override fun findSon() {
//        val localN: Deque<Point> = ArrayDeque()
//        val x = pacman_x / BLOCK_SIZE
//        val y = pacman_y / BLOCK_SIZE
//        val pos = pointToPos(x, y)
//        if (checkObject(pos)) return
//        val posUp = pointToPos(x, y - 1)
//        val posDown = pointToPos(x, y + 1)
//        val posLeft = pointToPos(x - 1, y)
//        val posRight = pointToPos(x + 1, y)
//        var up: Int = 8
//        var down: Int = 2
//        val left: Int
//        val right: Int
//        if (y != 0) up = screenData[posUp]
//        if (y != 14) down = screenData[posDown]
//        left = screenData[posLeft]
//        right = screenData[posRight]
//        if (dfs) {
//            if (down and 2 == 0 && isVisited(posDown)) localN.push(posToCoords(posDown))
//            if (up and 8 == 0 && isVisited(posUp)) localN.push(posToCoords(posUp))
//            if (left and 4 == 0 && isVisited(posLeft)) localN.push(posToCoords(posLeft))
//            if (right and 1 == 0 && isVisited(posRight)) localN.push(posToCoords(posRight))
//
//            //pop & append to visited
//            visited.add(pointToPos(x, y))
//            if (localN.size > 1) {
//                resultPath.push(Point(x, y, true, localN.size))
//            } else if (localN.size == 1) {
//                resultPath.push(Point(x, y))
//            }
//            val next: Point
//            if (localN.isEmpty()) {
//                while (true) {
//                    while (!resultPath.peek().hasFork) {
//                        resultPath.pop()
//                    }
//                    resultPath.peek().numOfNeighbours -= 1
//                    if (resultPath.peek().numOfNeighbours == 0) {
//                        resultPath.pop()
//                    } else {
//                        break
//                    }
//                }
//                next = neighbours.pop()
//                pacman_x = next.x * BLOCK_SIZE
//                pacman_y = next.y * BLOCK_SIZE
//                return
//            } else {
//                next = localN.pop()
//                while (!localN.isEmpty()) {
//                    neighbours.push(localN.pop())
//                }
//            }
//            findDirection(x, y, next)
//        } else {
//            if (down and 2 == 0 && isVisited(posDown)) neighbours.addLast(posToCoords(posDown))
//            if (up and 8 == 0 && isVisited(posUp)) neighbours.addLast(posToCoords(posUp))
//            if (left and 4 == 0 && isVisited(posLeft)) neighbours.addLast(posToCoords(posLeft))
//            if (right and 1 == 0 && isVisited(posRight)) neighbours.addLast(posToCoords(posRight))
//
//            //pop & append to visited
//            visited.add(pointToPos(x, y))
//            val next: Point
//            next = neighbours.pollFirst()
//            pacman_x = next.x * BLOCK_SIZE
//            pacman_y = next.y * BLOCK_SIZE
//            findDirection(x, y, next)
//        }
//    }
//
//    private fun initVariables() {
//        screenData = IntArray(N_BLOCKS * N_BLOCKS)
//        mazeColor = Color(252, 0, 248)
//        d = Dimension(400, 400)
//        timer = Timer(40, this)
//        timer!!.start()
//    }
//
//    override fun addNotify() {
//        super.addNotify()
//        initState()
//    }
//
//    private fun doAnim() {
//        pacAnimCount--
//        if (pacAnimCount <= 0) {
//            pacAnimCount = PAC_ANIM_DELAY
//            pacmanAnimPos = pacmanAnimPos + pacAnimDir
//            val PACMAN_ANIM_COUNT = 4
//            if (pacmanAnimPos == PACMAN_ANIM_COUNT - 1 || pacmanAnimPos == 0) {
//                pacAnimDir = -pacAnimDir
//            }
//        }
//    }
//
//    private fun playGame(g2d: Graphics2D) {
//        if (dying) {
//            death()
//        } else if (showResultPath) {
//            movePacmanFinal()
//            drawPacman(g2d)
//            checkMaze()
//        } else {
//            movePacman()
//            drawPacman(g2d)
//            checkMaze()
//        }
//    }
//
//    private fun showIntroScreen(g2d: Graphics2D) {
//        g2d.color = Color(0, 32, 48)
//        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50)
//        g2d.color = Color.white
//        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50)
//        val s1 = "Press D to start DFS."
//        val s2 = "Press B to start BFS."
//        val small = Font("Helvetica", Font.BOLD, 14)
//        val metr = getFontMetrics(small)
//        g2d.color = Color.white
//        g2d.font = small
//        g2d.drawString(s1, (SCREEN_SIZE - metr.stringWidth(s1)) / 2, SCREEN_SIZE / 2 + 10)
//        g2d.drawString(s2, (SCREEN_SIZE - metr.stringWidth(s2)) / 2, SCREEN_SIZE / 2 - 10)
//    }
//
//    private fun drawScore(g: Graphics2D) {
//        var i: Int
//        val s: String
//        g.font = smallFont
//        g.color = Color(96, 128, 255)
//        s = "Score: $score"
//        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16)
//        i = 0
//        while (i < pacsLeft) {
//            g.drawImage(pacman3left, i * 28 + 8, SCREEN_SIZE + 1, this)
//            i++
//        }
//    }
//
//    private fun checkMaze() {
//        var i: Short = 0
//        var finished = true
//        while (i < N_BLOCKS * N_BLOCKS && finished) {
//            if (screenData[i.toInt()] and 48 != 0) finished = false
//            i++
//        }
//        if (finished) {
//            val maxSpeed = 6
//            if (currentSpeed < maxSpeed) currentSpeed++
//            initLevel()
//        }
//    }
//
//    private fun death() {
//        pacsLeft--
//        if (pacsLeft == 0) {
//            currState.inGame = false
//        }
//        continueLevel()
//    }
//
//    private fun posToCoords(pos: Int): Point {         //coords start from 0,0
//        var y = 0
//        var x = pos
//        while (x - N_BLOCKS >= 0) {
//            y++
//            x -= N_BLOCKS
//        }
//        return Point(x, y)
//    }
//
//    private fun isVisited(pos: Int): Boolean {        //pos -> index of screenData[]
//        return !visited.contains(pos)
//    }
//
//    //    private int pointToPos(Point p) {
//    //        return p.y * N_BLOCKS + p.x;
//    //    }
//    private fun pointToPos(x: Int, y: Int): Int {
//        return y * N_BLOCKS + x
//    }
//
//    private fun checkDirection(x1: Int, y1: Int, x2: Int, y2: Int): Char {
//        return if (x1 == x2) {
//            if (y1 < y2) 'd' else 'u'
//        } else {
//            if (x1 < x2) 'r' else 'l'
//        }
//    }
//
//    private fun contains(list: ArrayDeque<Point>, p: Point): Boolean {
//        for (point in list) {
//            if (point.x == p.x && point.y == p.y) return true
//        }
//        return false
//    }
//
//    private fun removeDuplicates(list: ArrayDeque<Point>): ArrayDeque<Point> {
//        val newList = ArrayDeque<Point>()
//        for (element in list) {
//            if (!contains(newList, element)) {
//                newList.add(element)
//            }
//        }
//        return newList
//    }
//
//    override fun checkObject(pos: Int): Boolean {
//        val ch = screenData[pos]
//        if (ch and 16 != 0) {
//            screenData[pos] = (ch and 15) as Int //eats a pill
//            score++
//            resultPath.push(posToCoords(pos))
//            pacman_x = 7 * BLOCK_SIZE
//            pacman_y = 11 * BLOCK_SIZE
//            showResultPath = true
//            pathWeight()
//            val runtime = Runtime.getRuntime()
//            runtime.gc()
//            val memory = runtime.totalMemory() - runtime.freeMemory()
//            println("Used memory is bytes: $memory")
//            return true
//        }
//        return false
//    }
//
//    private fun movePacman() {
//        //                              **ALGO**
//        //check for neighbours --> not walls && not visited
//        //if no neighbours --> tp to popped Point on prev iteration (local stack of neighbours?)
//        //pop Point from neighbours
//        findSon()
//        changePacmanProperties()
//    }
//
//    private fun changePacmanProperties() {
//        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
//            pacmand_x = req_dx
//            pacmand_y = req_dy
//            //change avatar
//            view_dx = pacmand_x
//            view_dy = pacmand_y
//        }
//        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
//            pacmand_x = req_dx
//            pacmand_y = req_dy
//            view_dx = pacmand_x
//            view_dy = pacmand_y
//        }
//        val PACMAN_SPEED = 12
//        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x
//        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y
//    }
//
//    private fun findDirection(x: Int, y: Int, next: Point) {
//        println("$x-x, $y-y")
//        println(next.x.toString() + "-x.next, " + next.y + "-y.next")
//        when (checkDirection(x, y, next.x, next.y)) {
//            'r' -> {
//                println("r")
//                req_dx = 1
//                req_dy = 0
//            }
//            'l' -> {
//                println("l")
//                req_dx = -1
//                req_dy = 0
//            }
//            'u' -> {
//                println("u")
//                req_dx = 0
//                req_dy = -1
//            }
//            'd' -> {
//                println("d")
//                req_dx = 0
//                req_dy = 1
//            }
//        }
//    }
//
//    private fun movePacmanFinal() {
//        if (resultPath.size == 0) {
//            currState.inGame = false
//            showResultPath = false
//            resultPath = ArrayDeque()
//            return
//        }
//        val next = resultPath.last
//        resultPath.removeLast()
//        val x = pacman_x / BLOCK_SIZE
//        val y = pacman_y / BLOCK_SIZE
//        println("$x-x, $y-y")
//        println(next.x.toString() + "-x.next, " + next.y + "-y.next")
//        when (checkDirection(x, y, next.x, next.y)) {
//            'r' -> {
//                println("r")
//                req_dx = 1
//                req_dy = 0
//            }
//            'l' -> {
//                println("l")
//                req_dx = -1
//                req_dy = 0
//            }
//            'u' -> {
//                println("u")
//                req_dx = 0
//                req_dy = -1
//            }
//            'd' -> {
//                println("d")
//                req_dx = 0
//                req_dy = 1
//            }
//        }
//        changePacmanProperties()
//    }
//
//    private fun drawPacman(g2d: Graphics2D) {
//        if (view_dx == -1) {
//            drawPacman(g2d, pacman2left, pacman3left, pacman4left)
//        } else if (view_dx == 1) {
//            drawPacman(g2d, pacman2right, pacman3right, pacman4right)
//        } else if (view_dy == -1) {
//            drawPacman(g2d, pacman2up, pacman3up, pacman4up)
//        } else {
//            drawPacman(g2d, pacman2down, pacman3down, pacman4down)
//        }
//    }
//
//    private fun drawPacman(g2d: Graphics2D, pacman2left: Image?, pacman3left: Image?, pacman4left: Image?) {
//        when (pacmanAnimPos) {
//            1 -> g2d.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this)
//            2 -> g2d.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this)
//            3 -> g2d.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this)
//            else -> g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this)
//        }
//    }
//
//    private fun drawMaze(g2d: Graphics2D) {
//        var i: Short = 0
//        var x: Int
//        var y: Int
//        y = 0
//        while (y < SCREEN_SIZE) {
//            x = 0
//            while (x < SCREEN_SIZE) {
//                g2d.color = mazeColor
//                g2d.stroke = BasicStroke(2F)
//                if (screenData[i.toInt()] and 1 != 0) g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1)
//                if (screenData[i.toInt()] and 2 != 0) g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y)
//                if (screenData[i.toInt()] and 4 != 0) {
//                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
//                            y + BLOCK_SIZE - 1)
//                }
//                if (screenData[i.toInt()] and 8 != 0) {
//                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
//                            y + BLOCK_SIZE - 1)
//                }
//                if (screenData[i.toInt()] and 16 != 0) {
//                    g2d.color = dotColor
//                    g2d.fillRect(x + 11, y + 11, 2, 2)
//                }
//                i++
//                x += BLOCK_SIZE
//            }
//            y += BLOCK_SIZE
//        }
//    }
//
//    override fun initState() {
//        score = 0
//        initLevel()
//        currentSpeed = 3
//    }
//
//    private fun initLevel() {
//        if (score != 0) {
//            val endTime = System.currentTimeMillis()
//            println("Total execution time: " + (endTime - startTime) + "ms")
//        }
//        startTime = System.currentTimeMillis()
//        neighbours.clear()
//        visited.clear()
//        var i: Int
//        i = 0
//        while (i < N_BLOCKS * N_BLOCKS) {
//            screenData[i] = levelData[i].toInt()
//            i++
//        }
//        continueLevel()
//    }
//
//    private fun continueLevel() {
//        pacman_x = 7 * BLOCK_SIZE
//        pacman_y = 11 * BLOCK_SIZE
//        //visited.add(pointToPos(7,11));
//        pacmand_x = 0
//        pacmand_y = 0
//        req_dx = 0
//        req_dy = 0
//        view_dx = -1
//        view_dy = 0
//        dying = false
//    }
//
//    private fun loadImages() {
//        pacman1 = ImageIcon("src/main/resources/images/pacman.png").image
//        pacman2up = ImageIcon("src/main/resources/images/up1.png").image
//        pacman3up = ImageIcon("src/main/resources/images/up2.png").image
//        pacman4up = ImageIcon("src/main/resources/images/up3.png").image
//        pacman2down = ImageIcon("src/main/resources/images/down1.png").image
//        pacman3down = ImageIcon("src/main/resources/images/down2.png").image
//        pacman4down = ImageIcon("src/main/resources/images/down3.png").image
//        pacman2left = ImageIcon("src/main/resources/images/left1.png").image
//        pacman3left = ImageIcon("src/main/resources/images/left2.png").image
//        pacman4left = ImageIcon("src/main/resources/images/left3.png").image
//        pacman2right = ImageIcon("src/main/resources/images/right1.png").image
//        pacman3right = ImageIcon("src/main/resources/images/right2.png").image
//        pacman4right = ImageIcon("src/main/resources/images/right3.png").image
//    }
//
//    public override fun paintComponent(g: Graphics) {
//        super.paintComponent(g)
//        doDrawing(g)
//    }
//
//    private fun doDrawing(g: Graphics) {
//        val g2d = g as Graphics2D
//        g2d.color = Color.black
//        g2d.fillRect(0, 0, d!!.width, d!!.height)
//        drawMaze(g2d)
//        drawScore(g2d)
//        doAnim()
//        if (currState.inGame) {
//            playGame(g2d)
//        } else {
//            showIntroScreen(g2d)
//        }
//        g2d.drawImage(ii, 5, 5, this)
//        Toolkit.getDefaultToolkit().sync()
//        g2d.dispose()
//    }
//
//    override fun pathWeight() {
//        val listWithoutDuplicates = visited.stream()
//                .distinct()
//                .collect(Collectors.toList())
//        println("Amount of steps = " + listWithoutDuplicates.size)
//    }
//
//    internal inner class TAdapter : KeyAdapter() {
//        override fun keyPressed(e: KeyEvent) {
//            val key = e.keyCode
//            if (currState.inGame) {
//                if (key == KeyEvent.VK_LEFT) {
//                    req_dx = -1
//                    req_dy = 0
//                } else if (key == KeyEvent.VK_RIGHT) {
//                    req_dx = 1
//                    req_dy = 0
//                } else if (key == KeyEvent.VK_UP) {
//                    req_dx = 0
//                    req_dy = -1
//                } else if (key == KeyEvent.VK_DOWN) {
//                    req_dx = 0
//                    req_dy = 1
//                } else if (key == KeyEvent.VK_ESCAPE && timer!!.isRunning) {
//                    currState.inGame = false
//                } else if (key == KeyEvent.VK_PAUSE) {
//                    if (timer!!.isRunning) {
//                        timer!!.stop()
//                    } else {
//                        timer!!.start()
//                    }
//                }
//            } else {
//                if (key == 'b'.toInt() || key == 'B'.toInt()) {
//                    dfs = false
//                    currState.inGame = true
//                    initState()
//                }
//                if (key == 'd'.toInt() || key == 'D'.toInt()) {
//                    dfs = true
//                    currState.inGame = true
//                    initState()
//                }
//            }
//        }
//
//        override fun keyReleased(e: KeyEvent) {
//            val key = e.keyCode
//            if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN) {
//                req_dx = 0
//                req_dy = 0
//            }
//        }
//    }
//
//    override fun actionPerformed(e: ActionEvent) {
//        repaint()
//    }
//
//    init {
//        loadImages()
//        initVariables()
//        initBoard()
//    }
//}