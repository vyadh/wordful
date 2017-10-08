import Wordful._

import scala.annotation.tailrec
import scala.io.Source

class WordfulSolver(grid: Grid, edges: Edges) {

  def contains(word: Word): Boolean = {
    val startVertices = grid.scan(word.head)
    val startPaths = startVertices.map(List(_))

    validPaths(startPaths, word, 1).nonEmpty
  }

  /** Recursively iterate the graph, discovering paths for each subsequent letter of the word. */
  @tailrec
  final def validPaths(last: Seq[Path], word: Word, count: Int): Seq[Path] = {
    val nextOne = nextPaths(last, word)
    val nextCount = count + 1

    if (nextOne.nonEmpty && nextCount < word.length) {
      validPaths(nextOne, word, nextCount)
    } else {
      nextOne
    }
  }

  def nextPaths(paths: Seq[Path], word: Word): Seq[Path] = {
    for {
      path <- paths
      nextPath <- next(path)
      if isPrefix(nextPath, word)
    } yield nextPath
  }

  /** Prefix current path with each following (not previously seen) vertex in the graph */
  def next(path: Path): Seq[Path] = {
    val current = path.head
    val next = edges(current)
    val novel = next.filterNot(path.contains)

    novel.map(next => next :: path)
  }

  /** If the possible path is a prefix of the specified word. */
  def isPrefix(path: Path, word: Word): Boolean = {
    val prefix = path.map(_.value).reverse
    word.startsWith(prefix)
  }

}

object Wordful {

  type Value = Char
  type Word = Seq[Value]

  /** A vertex is a letter in the wordful navigation graph */
  trait Vertex {
    def value: Value
  }

  /** How to get to the neighbours of each node. */
  type Edges = Vertex => Seq[Vertex]

  /** Path is a valid, but possibly partial, navigation through edges in reverse order. */
  type Path = List[Vertex]

  /** A graph is created from a grid, may contain identical values with diff neighbours. */
  trait Grid {
    def scan(value: Value): Seq[Vertex]
  }

}

object WordfulSolver extends App {

  /** Grid is represented by a single-line string, e.g. xxxyyyzzz (3x3, left to right) */
  class StringGrid(grid: String) extends Grid {

    val dim: Int = math.sqrt(grid.length).toInt
    def x(i: Int): Int = i % dim
    def y(i: Int): Int = i / dim
    def index(x: Int, y: Int): Int = y * dim + x
    private def vertex(i: Int) = StringVertex(i, grid.charAt(i))

    override def scan(value: Value): Seq[Vertex] = {
      (0 until grid.length)
        .map(vertex)
        .filter(_.value == value)
    }

    def edges(sv: Vertex): Seq[Vertex] = {
      val i = sv.asInstanceOf[StringVertex].index
      for {
        xp <- x(i) - 1 to x(i) + 1
        yp <- y(i) - 1 to y(i) + 1
        if xp >= 0 && xp < dim
        if yp >= 0 && yp < dim
        xpi = index(xp, yp)
        if i != xpi
      } yield vertex(xpi)
    }
  }

  case class StringVertex(index: Int, value: Value) extends Vertex

  override def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("Usage: cat words | scala WordfulSolver <grid>")
      System.exit(1)
    } else {
      val matrix = args(0)
      val grid = new StringGrid(matrix)
      val solver = new WordfulSolver(grid, grid.edges)

      for (line <- readStandardIn() if solver.contains(line)) {
        println(line)
      }
    }
  }

  def readStandardIn(): Stream[String] = {
    Source.fromInputStream(System.in, "UTF-8")
      .getLines()
      .toStream
      .map(_.toLowerCase)
  }

}
