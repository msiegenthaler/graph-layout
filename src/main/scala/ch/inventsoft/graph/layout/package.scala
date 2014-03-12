package ch.inventsoft.graph

import ch.inventsoft.graph.vector._


package object layout {
  type Position = Vector3

  type Layout[N] = N => Position

  trait IncrementalLayout[N] extends Layout[N] {
    /** Improves the layout. */
    def improve: IncrementalLayout[N]

    /** Executes multiple improve steps with one call. */
    def improves(steps: Int): IncrementalLayout[N] = {
      if (steps < 1) this
      else improve.improves(steps - 1)
    }
  }
}