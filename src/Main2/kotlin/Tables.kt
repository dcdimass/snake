import java.awt.Color
import java.awt.Dimension
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

val CELL = 30
val TABLE_WIDTH = 20
val TABLE_HEIGHT = 20

val WINDOW_HEIGHT = CELL * TABLE_HEIGHT
val WINDOW_WIDTH = CELL * TABLE_WIDTH

var direct : String = "Up"
var pause = false
var menu = false

val table1 : List<List<Boolean>> = List<List<Boolean>>(TABLE_HEIGHT){List<Boolean>(TABLE_WIDTH){false}}
val table2 : List<List<Boolean>> = (1..TABLE_HEIGHT).map{it -> (1..TABLE_WIDTH).map { elem-> !(elem != 1 && elem != TABLE_WIDTH && it != 1 && it != TABLE_HEIGHT) }}
var numOfTables = 2
