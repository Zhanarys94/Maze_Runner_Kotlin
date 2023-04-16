package mazerunner

import java.io.BufferedReader
import java.io.BufferedWriter
import kotlin.random.Random
import kotlin.random.nextInt
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.charset.Charset
import kotlin.math.sqrt

class Maze() {
    enum class Pattern(val character: String) {
        PASS("  "),
        WALL("\u2588\u2588")
    }

    lateinit var maze: Array<IntArray>
    lateinit var path: Array<Pair<Int, Int>>

    private fun generate() {
        println("Enter the size of a new maze")
        val size = readln().toInt()
        maze = Array(size) { IntArray(size) {1} }
        val row = Random.nextInt(1 until maze.lastIndex)
        val column = Random.nextInt(1 until maze.first().lastIndex)
        maze[row][column] = 0
        val neighbors = mutableListOf<Pair<Int, Int>>()
        val inBetweenCells = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()

        fun addNeighbors(row: Int, column: Int) {
            if (row in 2 until maze.lastIndex && column in 1 until maze.first().lastIndex &&
                maze[row - 2][column] == 1 && !neighbors.contains(Pair(row - 2, column))
            ) {
                neighbors.add(Pair(row - 2, column))
                inBetweenCells[Pair(row - 2, column)] = row - 1 to column
            }
            if (row in 1 until maze.lastIndex - 1 && column in 1 until maze.first().lastIndex &&
                maze[row + 2][column] == 1 && !neighbors.contains(Pair(row + 2, column))
            ) {
                neighbors.add(Pair(row + 2, column))
                inBetweenCells[Pair(row + 2, column)] = row + 1 to column
            }
            if (row in 1 until maze.lastIndex && column in 2 until maze.first().lastIndex &&
                maze[row][column - 2] == 1 && !neighbors.contains(Pair(row, column - 2))
            ) {
                neighbors.add(Pair(row, column - 2))
                inBetweenCells[Pair(row, column - 2)] = row to column - 1
            }
            if (row in 1 until maze.lastIndex && column in 1 until maze.first().lastIndex - 1 &&
                maze[row][column + 2] == 1 && !neighbors.contains(Pair(row, column + 2))
            ) {
                neighbors.add(Pair(row, column + 2))
                inBetweenCells[Pair(row, column + 2)] = row to column + 1
            }
        }

        addNeighbors(row, column)

        while (neighbors.isNotEmpty()) {
            val (neighborRow, neighborColumn) = neighbors.random()
            if (inBetweenCells.containsKey(neighborRow to neighborColumn)) {
                val (inBetweenCellRow, inBetweenCellColumn) = inBetweenCells.getValue(neighborRow to neighborColumn)
                maze[inBetweenCellRow][inBetweenCellColumn] = 0
                inBetweenCells.remove(neighborRow to neighborColumn)
            }
            maze[neighborRow][neighborColumn] = 0
            addNeighbors(neighborRow, neighborColumn)
            neighbors.remove(neighborRow to neighborColumn)
        }
        val northEntrances = mutableListOf<Pair<Int, Int>>()
        val westEntrances = mutableListOf<Pair<Int, Int>>()
        val southExits = mutableListOf<Pair<Int, Int>>()
        val eastExits = mutableListOf<Pair<Int, Int>>()
        for (i in 0..maze.lastIndex) {
            for (x in 0..maze[row].lastIndex) {
                if (i in 1 until maze.lastIndex && x in 1 until maze.first().lastIndex) continue
                when {
                    i == 0 && x in 1 until maze.first().lastIndex && maze[i][x] == 0 -> {
                        northEntrances.add(i to x)
                        maze[i][x] = 1
                    }

                    i == 0 && x in 1 until maze.first().lastIndex && maze[i][x] == 1 && maze[i + 1][x] == 0 -> {
                        northEntrances.add(i to x)
                    }

                    x == 0 && i in 1 until maze.lastIndex && maze[i][x] == 0 -> {
                        westEntrances.add(i to x)
                        maze[i][x] = 1
                    }

                    x == 0 && i in 1 until maze.lastIndex && maze[i][x] == 1 && maze[i][x + 1] == 0 -> {
                        westEntrances.add(i to x)
                    }
                }
                when {
                    i == maze.lastIndex && x in 1 until maze.first().lastIndex && maze[i][x] == 0 -> {
                        southExits.add(i to x)
                        maze[i][x] = 1
                    }

                    i == maze.lastIndex && x in 1 until maze.first().lastIndex && maze[i][x] == 1 && maze[i - 1][x] == 0 -> {
                        southExits.add(i to x)
                    }

                    x == maze.first().lastIndex && i in 1 until maze.lastIndex && maze[i][x] == 0 -> {
                        eastExits.add(i to x)
                        maze[i][x] = 1
                    }

                    x == maze.first().lastIndex && i in 1 until maze.lastIndex && maze[i][x] == 1 && maze[i][x - 1] == 0 -> {
                        eastExits.add(i to x)
                    }
                }
            }
        }
        val entrance = when {
            Random.nextBoolean() -> westEntrances.random()
            else -> northEntrances.random()
        }
        val exit = when {
            westEntrances.contains(entrance) -> eastExits.random()
            else -> southExits.random()
        }
        maze[entrance.first][entrance.second] = 0
        maze[exit.first][exit.second] = 0
        println(printMaze())
        path = findPathDFS(entrance, exit)
    }

    fun menu() {
        while (true) {
            if (!::maze.isInitialized) {
                println("""
                    === Menu ===
                    1. Generate a new maze.
                    2. Load a maze.
                    0. Exit
                """.trimIndent())
            } else {
                println("""
                    === Menu ===
                    1. Generate a new maze.
                    2. Load a maze.
                    3. Save the maze.
                    4. Display the maze.
                    5. Find the escape
                    0. Exit
                """.trimIndent())
            }
            when (readln()) {
                "1" -> generate()
                "2" -> loadMaze()
                "3" -> saveMaze()
                "4" -> println(printMaze())
                "5" -> println(printMaze(true))
                "0" -> {
                    println("Bye!")
                    break
                }
                else -> println("Incorrect option. Please try again")
            }
        }
    }

    private fun saveMaze() {
        val name = readln()
        val file = File(name)
        val writer = BufferedWriter(FileWriter(file))
        writer.use {
            for (row in maze) {
                for (i in row.indices) {
                    it.write(row[i].toString())
                    if (i < row.size - 1) {
                        it.write(" ")
                    }
                }
                it.newLine()
            }
        }
    }

    private fun loadMaze() {
        val name = readln()
        val file = File(name)
        if (!file.exists()) {
            println("The file $name does not exist")
            return
        } else if (!file.isFile || !file.name.contains(".txt")) {
            println("Cannot load the maze. It has an invalid format")
            return
        }
        val reader = BufferedReader(FileReader(file))
        val lines = mutableListOf<IntArray>()
        reader.use {
            it.forEachLine { line ->
                val tokens = line.split(" ").map { it.toInt() }.toIntArray()
                lines.add(tokens)
            }
        }
        maze = lines.toTypedArray()
        lateinit var entranceOfLoadedMaze: Pair<Int, Int>
        for (row in 0 until maze.lastIndex) {
            for (col in 0 until maze[row].lastIndex) {
                if (row > 0 && col > 0) continue
                if (row == 0 && maze[row][col] == 0) {
                    entranceOfLoadedMaze = 0 to col
                    break
                } else if (row in 1 until maze.lastIndex && maze[row][col] == 0) {
                    entranceOfLoadedMaze = row to 0
                    break
                }
            }
        }
        lateinit var exitOfLoadedMaze: Pair<Int, Int>
        loop@for (row in 1..maze.lastIndex) {
            for (col in 1..maze[row].lastIndex) {
                if (row < maze.lastIndex && col < maze[row].lastIndex) continue
                if (row == maze.lastIndex && maze[row][col] == 0) {
                    exitOfLoadedMaze = row to col
                    break@loop
                } else if (row in 1..maze.lastIndex && maze[row][col] == 0) {
                    exitOfLoadedMaze = row to col
                    break@loop
                }
            }
        }
        path = findPathDFS(entranceOfLoadedMaze, exitOfLoadedMaze)
    }

    private fun printMaze(printPath: Boolean = false): String {
        val builder = StringBuilder()
        for (row in maze.indices) {
            for (cell in maze[row].indices) {
                builder.append(
                    if (maze[row][cell] == 1) Pattern.WALL.character
                    else if (printPath && maze[row][cell] == 0 && path.contains(row to cell)) "//"
                    else Pattern.PASS.character
                )
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun findPathDFS(entrance: Pair<Int, Int>, exit: Pair<Int, Int>): Array<Pair<Int, Int>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val pathFinder = mutableListOf<Pair<Int, Int>>()

        fun dfs(row: Int, column: Int): Boolean {
            if (row < 0 || row >= maze.size || column < 0 || column >= maze[0].size || maze[row][column] == 1 || visited.contains(row to column)) {
                return false
            }
            visited.add(row to column)
            pathFinder.add(row to column)
            if (row == exit.first && column == exit.second) {
                return true
            }
            if (dfs(row + 1, column) || dfs(row, column + 1) || dfs(row - 1, column) || dfs(row, column - 1)) {
                return true
            }
            pathFinder.removeAt(pathFinder.lastIndex)
            return false
        }
        dfs(entrance.first, entrance.second)
        return pathFinder.toTypedArray()
    }
}

fun main() {
    val newMaze = Maze()
    newMaze.menu()
}