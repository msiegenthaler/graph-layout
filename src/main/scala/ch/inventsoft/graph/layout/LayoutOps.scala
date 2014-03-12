package ch.inventsoft.graph.layout

import ch.inventsoft.graph.vector._

/** Useful operations on layouts. */
case class LayoutOps[N](layout: Layout[N], nodes: Traversable[N]) {
  lazy val bounds = Box3.containing(positions)

  def positions = nodes.map(layout)

  /** Moves and scales the layout, so it fills the box. The proportions are retained. */
  def inside(box: Box3) = {
    val scale = (box.size / bounds.size).minimal
    val scaled = layout.andThen(p => (p - bounds.origin) * scale + box.origin)
    LayoutOps(scaled, nodes).centerOver(box.center)
  }

  /** Moves the layout, so it's center is at v. */
  def centerOver(v: Vector3) = offset(v - bounds.size / 2 - bounds.origin)

  def scale(by: Vector3) = layout.andThen(_ * by)

  def offset(by: Vector3) = layout.andThen(_ + by)
}

