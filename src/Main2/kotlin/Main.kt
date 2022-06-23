import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.math.*

class Cell(
    var x : Int,
    var y : Int
) {
    fun up() : Cell {
        if(y < 1) return Cell(x, TABLE_HEIGHT - 1)
        else return Cell(x, y - 1)

    }
    fun down() : Cell {
        if(y >= TABLE_HEIGHT - 1) return Cell(x, 0)
        else return Cell(x, y + 1)
    }
    fun left() : Cell {
        if(x < 1) return Cell(TABLE_WIDTH - 1, y)
        else return Cell(x - 1, y)
    }
    fun right() : Cell {
        if(x >= TABLE_WIDTH - 1) return Cell(0, y)
        else return Cell(x + 1, y)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

}

fun Graphics.setFontSize(size : Float) {
    val currentFont : Font = this.font
    val newFont : Font = currentFont.deriveFont(size)
    this.font = newFont
    //this.fontMetrics.stringWidth("ok")
}

fun Graphics.drawOnCenter(text : String, y : Int, size : Float) {
    this.setFontSize(size)
    val w = this.fontMetrics.stringWidth(text)
    this.drawString(text, WINDOW_WIDTH / 2 - w / 2, y)
}

class Snake() {
    var snake: Queue<Cell> = LinkedList()
    var head : Cell = Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt())
    var pref : Cell = Cell(-1, -1)
    var food : Cell = Cell(-1, -1)
    var bigFood : Cell = Cell(-1, -1)
    var bigFoodLifeTime = 0
    var bigFoodSpawnTimer = 5
    var gameTable = table2
    var tableIndex = 1
    var extraFood = 0
    fun respawnFood(cell : Cell = food) {
        var checkNormalSpawn = false
        while(!checkNormalSpawn) {
            food.x = Random.nextInt(1, TABLE_WIDTH) - 1
            food.y = Random.nextInt(1, TABLE_HEIGHT) - 1
            checkNormalSpawn = true
            for(i in snake) if(food == i) checkNormalSpawn = false
            if(gameTable[food.y][food.x]) checkNormalSpawn = false
        }
    }
    fun respawnBigFood() {
        var checkNormalSpawn = false
        while(!checkNormalSpawn) {
            bigFood.x = Random.nextInt(1, TABLE_WIDTH - 1) - 1
            bigFood.y = Random.nextInt(1, TABLE_HEIGHT - 1) - 1
            checkNormalSpawn = true
            for(i in snake) if(bigFood == i || bigFood.down() == i || bigFood.right() == i || bigFood.down().right() == i) checkNormalSpawn = false
            if(gameTable[bigFood.y][bigFood.x] || gameTable[bigFood.y][bigFood.x + 1] || gameTable[bigFood.y + 1][bigFood.x] || gameTable[bigFood.y + 1][bigFood.x + 1]) checkNormalSpawn = false
        }
    }

    fun restart() {
        direct = "Up"
        bigFoodSpawnTimer = 5
        bigFoodLifeTime = 0
        snake.clear()
        head = Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt())
        pref = Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt() + 1)
        snake.add(Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt() + 4))
        snake.add(Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt() + 3))
        snake.add(Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt() + 2))
        snake.add(Cell((TABLE_WIDTH / 2).toInt(), (TABLE_HEIGHT / 2).toInt() + 1))
        respawnFood()
    }

    fun updateSnake() {
        if(food.x < 0) respawnFood()
        if(snake.size < 3) restart()
        val next = when(direct) {
            "Up" -> head.up()
            "Down" -> head.down()
            "Left" -> head.left()
            "Right" -> head.right()
            else -> head
        }
        var checkDead = false
        for(i in snake) if(i == next) checkDead = true
        if(next == head) checkDead = true
        if(gameTable[next.y][next.x]) checkDead = true
        if(checkDead) restart()
        else {
            snake.add(head)
            pref = head
            head = next
            if(next == food) {
                respawnFood()
                bigFoodSpawnTimer--
                if(bigFoodSpawnTimer == 0) {
                    respawnBigFood()
                    bigFoodLifeTime = 15
                    bigFoodSpawnTimer = 5
                }
            }
            else {
                if(next == bigFood || next == bigFood.down() || next == bigFood.right() || next == bigFood.down().right()) {
                    extraFood = bigFoodLifeTime
                    bigFoodLifeTime = 0
                }
                else {
                    if(extraFood == 0) snake.remove()
                    else extraFood--
                }
            }

        }
    }

    fun display(g: Graphics) {
        for(i in (1..TABLE_HEIGHT)) {
            for(j in (1..TABLE_WIDTH)) {
                if(gameTable[i - 1][j - 1]) g.color = Color.BLACK
                else g.color = Color.WHITE
                g.fillRect(CELL * (j - 1), CELL * (i - 1), CELL, CELL)
            }
        }
        g.color = Color.RED
        g.fillRect(CELL * head.x + 1, CELL * head.y + 1, CELL - 2, CELL - 2)
        g.color = Color.GREEN
        for(i in snake) g.fillRect(CELL * i.x + 1, CELL * i.y + 1, CELL - 2, CELL - 2)
        g.color = Color.YELLOW
        g.fillOval(CELL * food.x, CELL * food.y, CELL, CELL)
        if(bigFoodLifeTime > 0) {
            g.color = Color.BLUE
            g.fillOval(CELL * bigFood.x, CELL * bigFood.y, 2 * CELL, 2 * CELL)
        }
        if(pause) {
            g.color = Color.BLACK
            g.fillRect(WINDOW_WIDTH / 2 - CELL, WINDOW_HEIGHT / 2 - CELL, 2 * CELL / 3, 2 * CELL)
            g.fillRect(WINDOW_WIDTH / 2 + CELL / 3, WINDOW_HEIGHT / 2 - CELL, 2 * CELL / 3, 2 * CELL)

            g.drawOnCenter((snake.size + extraFood + 1).toString(), WINDOW_HEIGHT / 2 - 100, (50).toFloat())
        }
    }
}


class GamePanel : JPanel() {
    init {
        preferredSize = Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
    }

    var snake = Snake()

    val keyListener = object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if(e.keyCode == KeyEvent.VK_SPACE && !menu) {
                pause = !pause
                if(pause) println(snake.snake.size + snake.extraFood + 1)
            }
            if(e.keyCode == KeyEvent.VK_R) snake.restart()
            if(e.keyCode == KeyEvent.VK_ESCAPE && !menu) {
                snake.restart()
                menu = true
                pause = true
            }
            if(menu) {
                if(e.keyCode == KeyEvent.VK_E) snake.tableIndex = (snake.tableIndex + 1) % numOfTables
                if(e.keyCode == KeyEvent.VK_Q) snake.tableIndex = (snake.tableIndex - 1 + numOfTables) % numOfTables
                if(snake.tableIndex == 0) snake.gameTable = table1
                if(snake.tableIndex == 1) snake.gameTable = table2
                if(e.keyCode == KeyEvent.VK_SPACE) {
                    pause = false
                    menu = false
                }
            }
            if(!pause && !menu) {
                val pastDirect = direct
                direct = when(e.keyCode) {
                    KeyEvent.VK_W -> "Up"
                    KeyEvent.VK_A -> "Left"
                    KeyEvent.VK_S -> "Down"
                    KeyEvent.VK_D -> "Right"
                    else -> direct
                }
                val thNext = when(direct) {
                    "Up" -> snake.head.up()
                    "Down" -> snake.head.down()
                    "Left" -> snake.head.left()
                    "Right" -> snake.head.right()
                    else -> snake.head
                }
                if(thNext == snake.pref) direct = pastDirect
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        snake.display(g)
    }


    private fun update() {
        if(!pause && !menu) snake.updateSnake()
    }


    @OptIn(ExperimentalTime::class)
    fun loop() {
        val duration = measureTime {
            update()
            repaint()
            Thread.sleep(200L)
            snake.bigFoodLifeTime = max(0, snake.bigFoodLifeTime - 1)
        }
    }


}

class GameWindow(val gamePanel: GamePanel) : JFrame() {
    init {
        add(gamePanel)
        addKeyListener(gamePanel.keyListener) // обрабатыватель кнопок нужно приклеивать к непосредственно окну
        isResizable = false;
        pack();
        title = "Snaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaake";
        setLocationRelativeTo(null);
        defaultCloseOperation = EXIT_ON_CLOSE;
    }
}


fun main() {
    val gameWindow = GameWindow(GamePanel())
    gameWindow.isVisible = true
    while (true) {
        gameWindow.gamePanel.loop()
    }
}