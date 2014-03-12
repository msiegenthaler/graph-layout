package ch.inventsoft.graph.layout

import ch.inventsoft.graph.vector._


/** Distributes the nodes at random within a box. */
object RandomLayout {
  def apply[N](nodes: Traversable[N], in: Box3): Layout[N] = {
    nodes.map { n =>
      (n, Vector3.random(in))
    }.toMap
  }
}