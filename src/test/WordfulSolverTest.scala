import Wordful._
import WordfulSolver.{StringGrid, StringVertex}
import org.scalatest.{FunSuite, Matchers}

class WordfulSolverTest extends FunSuite with Matchers {

  val grid = new NodeGrid()

  val Seq(x, y, z) = "xyz".map(node)
  val Seq(i, j, k) = "ijk".map(node)
  val Seq(w, o, r, d) = "word".map(node)
  val Seq(w1, h1, e1, w2) = Seq(
    Node(1, 'w'),
    Node(1, 'h'),
    Node(1, 'e'),
    Node(2, 'w')
  )

  test("contains with no paths cannot match any words") {
    val s = solver(Map())

    s.contains("word") shouldEqual false
    s.contains("play") shouldEqual false
  }

  test("contains with graph with no path to the word cannot match") {
    val s = solver(Map(
      x -> edges(i, j, k),
      i -> edges(x, y, z)
    ))

    s.contains("word") shouldEqual false
  }

  test("contains with a partial path to the full word cannot match") {
    val s = solver(Map(
      w -> edge(o),
      o -> edge(r)
    ))

    s.contains("word") shouldEqual false
  }

  test("contains with a full path will match") {
    val s = solver(Map(
      w -> edge(o),
      o -> edge(r),
      r -> edge(d)
    ))

    s.contains("word") shouldEqual true
  }

  test("contains does not match even if a duplicate would satisfy path") {
    val s = solver(Map(
      w1 -> edge(h1),
      h1 -> edge(e1),
      e1 -> edge(w1)
    ))

    s.contains("whew") shouldEqual false
  }

  test("contains does match even if a different node with same value satisfies path") {
    val map: Map[Vertex, List[Vertex]] = Map(
      w1 -> edge(h1),
      h1 -> edge(e1),
      e1 -> edge(w2)
    )
    val edges = map.withDefaultValue(List())
    val grid = new Grid {
      override def scan(value: Value) = List(w1)
    }
    val s = new WordfulSolver(grid, edges)

    s.contains("whew") shouldEqual true
  }


  test("next with no neighbours returns no paths") {
    val s = solver(Map())

    s.next(path(y, z)) shouldEqual List()
  }

  test("next with single neighbour returns current path + neighbour") {
    val s = solver(Map(
      y -> edge(z)
    ))

    s.next(path(y, x)) shouldEqual List(path(z, y, x))
  }

  test("next with multiple neighbours returns path for each new neighbour") {
    val s = solver(Map(
      z -> edges(i, j, k)
    ))

    s.next(path(z, y, x)) shouldEqual List(
      path(i, z, y, x),
      path(j, z, y, x),
      path(k, z, y, x)
    )
  }

  test("next with a *different* neighbour of the same value still returns it") {
    val s = solver(Map(
      z -> edge(w2)
    ))

    s.next(path(z, w1)) shouldEqual List(path(w2, z, w1))
  }

  test("next with an existing neighbour does not return that as a valid new path") {
    val s = solver(Map(
      e1 -> edge(h1)
    ))

    s.next(path(e1, h1, w1)) shouldEqual List()
  }


  test("prefix is not true with different words") {
    solver(Map()).isPrefix(path(w1, h1, e1, w2), "worn") shouldEqual false
  }

  test("prefix is true, which requires prefix to be reversed") {
    solver(Map()).isPrefix(path(y, x), "xyz") shouldEqual true
  }

  test("prefix is true even for different node identities") {
    solver(Map()).isPrefix(path(w2, e1, h1, w1), "whew") shouldEqual true
  }


  test("validPaths returns only valid path for the word") {
    val s = solver(Map(
      w -> edges(o, x),
      o -> edges(r, y),
      r -> edges(d, z)
    ))

    val result = s.validPaths(List(path(w)), "word", 1)

    result shouldEqual List(path(d, r, o, w))
  }

  test("validPaths returns all possible paths except duplicates") {
    val s = solver(Map(
      // w2->h1 and e1->w1 are the second path
      w1 -> edge(h1),
      h1 -> edge(e1),
      e1 -> edges(w2, w1),
      w2 -> edge(h1)
    ))

    val result = s.validPaths(
      List(path(w1), path(w2)), // Both starting positions
      "whew",
      1
    )

    result shouldEqual List(
      path(w2, e1, h1, w1),
      path(w1, e1, h1, w2)
    )
  }


  test("string grid scan") {
    val grid = new StringGrid("x...x...x")

    grid.scan('x') shouldEqual List(
      StringVertex(0, 'x'),
      StringVertex(4, 'x'),
      StringVertex(8, 'x')
    )
  }


  test("string grid index conversions") {
    val grid = new StringGrid("123456789") // 123/456/789

    grid.dim shouldEqual 3
    grid.x(0) shouldEqual 0
    grid.y(1) shouldEqual 0
    grid.x(7) shouldEqual 1
    grid.y(8) shouldEqual 2
    grid.index(0, 1) shouldEqual 3
    grid.index(1, 2) shouldEqual 7
  }

  test("string grid on corner provides three neighbours") {
    val grid = new StringGrid("123456789")

    grid.edges(StringVertex(0, '1')).toSet shouldEqual Set(
      StringVertex(1, '2'),
      StringVertex(3, '4'),
      StringVertex(4, '5')
    )
  }

  test("string grid on side provides four neighbours") {
    val grid = new StringGrid("123456789")

    grid.edges(StringVertex(3, '4')).toSet shouldEqual Set(
      StringVertex(0, '1'),
      StringVertex(1, '2'),
      StringVertex(4, '5'),
      StringVertex(6, '7'),
      StringVertex(7, '8')
    )
  }


  test("solve string example (words of length 5, 7 and 4)") {
    val grid = new StringGrid("""
        |eeim
        |tnis
        |sgat
        |tajf
      """.stripMargin)
    val solver = new WordfulSolver(grid, grid.edges)

    solver.contains("mess") shouldEqual false // No path
    solver.contains("sting") shouldEqual true // Valid path
  }

  test("string string example with placeholders") {
    val grid = new StringGrid("""
        |..im
        |..is
        |.nat
        |egjf
      """.stripMargin)
    val solver = new WordfulSolver(grid, grid.edges)

    solver.contains("mess") shouldEqual false // No path
    solver.contains("jasmine") shouldEqual true // Valid path
  }


  def node(value: Value) = Node(1, value)
  def path(nodes: Node*): List[Node] = List(nodes: _*)
  def edges(nodes: Node*): List[Node] = List(nodes: _*)
  def edge(node: Node): List[Node] = List(node)

  case class Node(id: Int, value: Char) extends Vertex

  def solver(map: Map[Vertex, List[Vertex]]): WordfulSolver = {
    val edges: Edges = map.withDefaultValue(List())
    new WordfulSolver(grid, edges)
  }

  class NodeGrid extends Grid {
    override def scan(value: Value): List[Vertex] = List(Node(1, value))
  }

}
