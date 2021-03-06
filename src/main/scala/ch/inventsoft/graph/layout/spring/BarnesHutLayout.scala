package ch.inventsoft.graph.layout
package spring

import scala.language.higherKinds
import scalax.collection._
import GraphPredef._
import ch.inventsoft.graph.vector._

/** Spring layout that uses the barnes hut algorithm for repulsion. Use for larger (> 1000 nodes) graphs. */
object BarnesHutLayout {
  def apply[N, E[X] <: EdgeLikeIn[X]](graph: Graph[N, E], in: Box3, theta: Double): IncrementalLayout[N] =
    apply(graph, _ => Vector3.random(in), theta)

  def apply[N, E[X] <: EdgeLikeIn[X]](graph: Graph[N, E], positions: Layout[N], theta: Double): IncrementalLayout[N] = {
    val in = Box3.containing(graph.nodes.map(_.value).map(positions))
    val springConstant = 1d / (graph.edges.map(_.weight).max * 5)
    implicit val repulsionConstant = RepulsionConstant {
      val density = Math.pow(in.size.volume / graph.size, 1d / 3)
      density * density
    }
    implicit val epsilon = Epsilon(in.size.length / 10000000)
    implicit val mac = MultipoleAcceptanceCriterion(theta)

    val nodes = graph.nodes.map(_.value).toVector
    val nodeMap = nodes.zipWithIndex.toMap
    val springs = graph.edges.map { e =>
      Spring(nodeMap(e._1.value), nodeMap(e._2.value), e.weight, springConstant)
    }
    val bodies = nodes.map(n => Body(positions(n.value)))

    new BarnesHutLayout(nodeMap, springs.toVector, bodies)
  }

  private class BarnesHutLayout[N](
    lookupMap: Map[N, Int],
    springs: Vector[Spring],
    bodies: Vector[Body])(
      implicit repulsionConstant: RepulsionConstant,
      epsilon: Epsilon,
      mac: MultipoleAcceptanceCriterion) extends IncrementalLayout[N] {

    def apply(n: N) = bodies(lookupMap(n)).centerOfMass

    def improve = {
      val oct = Oct.create(bodies)
      val forces = bodies.toArray.par.map(body => body.centerOfMass + oct.force(body))
      springs.foreach { spring =>
        val force = spring.force(bodies(spring.node1).centerOfMass, bodies(spring.node2).centerOfMass)
        forces(spring.node1) -= force
        forces(spring.node2) += force
      }

      new BarnesHutLayout(lookupMap, springs, forces.seq.view.map(Body).toVector)
    }
  }

  private case class RepulsionConstant(value: Double) extends AnyVal
  private case class Epsilon(value: Double) extends AnyVal
  private case class MultipoleAcceptanceCriterion(value: Double) extends AnyVal {
    def accepts(boxSize: Double, distance: Double) = boxSize / distance < value
  }

  private sealed trait Node {
    def mass: Double
    def centerOfMass: Vector3
    def distance(to: Node) = (centerOfMass - to.centerOfMass).length
    def force(against: Body)(implicit repulsionConstant: RepulsionConstant, epsilon: Epsilon, mac: MultipoleAcceptanceCriterion): Vector3
  }
  private case class Body(centerOfMass: Vector3) extends Node {
    override def mass = 1
    def applyForce(f: Vector3) = copy(centerOfMass = centerOfMass + f)
    override def force(against: Body)(implicit repulsionConstant: RepulsionConstant, epsilon: Epsilon, mac: MultipoleAcceptanceCriterion) = {
      val vec = against.centerOfMass - centerOfMass
      val distance = vec.length
      vec * (repulsionConstant.value / (distance * distance * distance + epsilon.value))
    }
  }
  private case object Empty extends Node {
    override def mass = 0d
    override def centerOfMass = Vector3.zero
    override def force(against: Body)(implicit repulsionConstant: RepulsionConstant, epsilon: Epsilon, mac: MultipoleAcceptanceCriterion) =
      Vector3.zero
  }
  private case class Oct private (
    bounds: Box3,
    children: IndexedSeq[Node]) extends Node {
    override val mass = children.foldLeft(0d)(_ + _.mass)
    override val centerOfMass = {
      children.foldLeft(Vector3.zero) { (sum, child) =>
        sum + child.centerOfMass * child.mass
      } / mass
    }
    def size = bounds.size.x //same size in each direction

    override def force(body: Body)(implicit repulsionConstant: RepulsionConstant, epsilon: Epsilon, mac: MultipoleAcceptanceCriterion) = {
      val vec = body.centerOfMass - centerOfMass
      val distance = vec.length
      if (mac.accepts(size, distance)) {
        // distance is big enough so we can threat us as a cluster regarding the body
        vec * (repulsionConstant.value * mass / (distance * distance * distance + epsilon.value))
      } else {
        // need to calculate the force for each child
        val v = children(0).force(body).toMutable
        v += children(1).force(body)
        v += children(2).force(body)
        v += children(3).force(body)
        v += children(4).force(body)
        v += children(5).force(body)
        v += children(6).force(body)
        v += children(7).force(body)
        v.toVector3
      }
    }
  }
  private object Oct {
    def create(contents: Traversable[Body]): Node = {
      val rawBounds = Box3.containing(contents.view.map(_.centerOfMass))
      val size = rawBounds.size.x max rawBounds.size.y max rawBounds.size.z
      val bounds = Box3(rawBounds.origin, Vector3(size, size, size))
      create(bounds, contents)
    }
    def create(bounds: Box3, contents: Traversable[Body]): Node = {
      if (contents.isEmpty) Empty
      else if (contents.tail.isEmpty) contents.head
      else {
        val center = bounds.center
        val array = Array[List[Body]](Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil)
        contents.foreach { body =>
          val p = body.centerOfMass
          val index = (if (p.x < center.x) 0 else 1) +
            (if (p.y < center.y) 0 else 2) +
            (if (p.z < center.z) 0 else 4)
          array(index) = body :: array(index)
        }
        val size = bounds.size / 2
        val children = Array(
          create(Box3(bounds.origin, size), array(0)),
          create(Box3(Vector3(center.x, bounds.origin.y, bounds.origin.z), size), array(1)),
          create(Box3(Vector3(bounds.origin.x, center.y, bounds.origin.z), size), array(2)),
          create(Box3(Vector3(center.x, center.y, bounds.origin.z), size), array(3)),
          create(Box3(Vector3(bounds.origin.x, bounds.origin.y, center.z), size), array(4)),
          create(Box3(Vector3(center.x, bounds.origin.y, center.z), size), array(5)),
          create(Box3(Vector3(bounds.origin.x, center.y, center.z), size), array(6)),
          create(Box3(center, size), array(7)))
        new Oct(bounds, children)
      }
    }
  }

  private case class Spring(node1: Int, node2: Int, strength: Double, springConstant: Double) {
    private val factor = springConstant * strength
    def force(nodeA: Vector3, nodeB: Vector3) = (nodeA - nodeB) * factor
  }
}