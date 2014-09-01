# graph-layout

Scala library to layout a scalax.collection.Graph in three dimensions.
Currently supported:

* 3D RandomLayout
* 3D [SpringLayout](http://en.wikipedia.org/wiki/Force-directed_graph_drawing)
* 3D SpringLayout based on [Barnes-Hut](http://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation)


## Usage
### Add Dependency

    libraryDependencies += "ch.inventsoft.graph" %% "graph-layout" % "1.0.2"

### Basics
A layout maps the nodes of the graph to positions (Vector3). The definition is (N => Position), so i.e.
a Map[N,Position] is also a layout.
Some layouts are incremental, they improve with each iteration. They contain a .improve method that returns
a 'better' copy of the layout.

### Random Layout (3D)
Just lays out the nodes at random inside a box.

    val graph = ...
    val layout = RandomLayout(graph, Box3(2000))
    // nodes are layed out randomly within (-1000, -1000, -1000) - (1000, 1000, 1000)

### Spring Layout (3D)
Spring layout (also called force-directed layout) is an incremental layout. The nodes repulse each other
and the edges pull together the nodes. While CPU-time intensive O(n^2) the results are usually quite good.

    val graph = ...
    val layout = SpringLayout(graph, Box3(2000)).improve

    //or for multi-core enabled layouting:
    val layout = ParallelSpringLayout(graph, Box3(2000)).improve


## Barnes-Hut based Spring Layout (3D)
Same as spring layout, but uses a more efficient algorithm O(n*log n) to repulse the nodes. Processing time can
be traded vs a (slight) error with the theta parameter.

    val graph = ...
    val layout = BarnesHutLayout(graph, Box3(2000), 0.7d).improve
