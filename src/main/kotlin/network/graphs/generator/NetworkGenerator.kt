package network.graphs.generator

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.random.Random

/** NETWORK MODEL DECLARATION **/

data class NetworkGraph(
    val mode: Modes,
    val solveTimeout: Int,
    val q: Double,
    val l0: Double,
    val pathRateGoalWeight: Double,
    val physicalLinks: List<PhysicalLink>,
    val virtualNetworks: List<VirtualNetwork>
)

enum class Modes(val modeName: String) {
    Nash("nash"),
    Optimum("optimum")
}

data class PhysicalLink(
    val id: Int,
    val bandwidthCapacity: Int,
    val bandwidthPrice: Double,
    val congestionPrice: Double,
    val propagationDelay: Int
)

data class VirtualNetwork(
    val perLinkParams: Map<String, VirtualLinkParameters>,
    val id: Int,
    val maxBandwidthSum: Int
)

data class VirtualLinkParameters(
    val link: Link,
    val assignedBandwidth: Int,
    val priceWeightedFactor: Double
)

data class Link(val id: Int)

/** GRAPH MODEL DECLARATION **/

data class Node(val id: Int)

data class Edge(val from: Node, val to: Node) {

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Edge

        if (from != other.from) return false
        if (to != other.to) return false

        return true
    }
}

data class Graph(
    private val limitOfNodes: Int,
    val nodesToNeighbours: MutableMap<Node, MutableSet<Edge>> = mutableMapOf(),
    var mapNodeToVisited: MutableMap<Node, Boolean> = mutableMapOf()
) {


    fun addEdge(first: Node, second: Node) {
        if (nodesToNeighbours[first].isNullOrEmpty()) nodesToNeighbours[first] = mutableSetOf()
        if (nodesToNeighbours[second].isNullOrEmpty()) nodesToNeighbours[second] = mutableSetOf()

        nodesToNeighbours[first]!!.add(Edge(first, second))
    }

    fun areAllNodesReachable(): Boolean {
        if (nodesToNeighbours.isEmpty()) return false

        resetMapToNodeVisited()
        dfs(nodesToNeighbours.entries.first().key)

        return mapNodeToVisited.all { it.value }
    }

    private fun resetMapToNodeVisited() {
        mapNodeToVisited = nodesToNeighbours.map {
            it.key to false
        }.toMap().toMutableMap()
    }

    fun dfs(node: Node) {
        mapNodeToVisited[node] = true
        nodesToNeighbours[node]!!.forEach {
            if (!mapNodeToVisited[it.to]!!) {
                dfs(it.to)
            }
        }
    }

    fun printAdjacentNodes() {
        nodesToNeighbours.forEach {
            print("${it.key} => ")
            it.value.forEach { edge -> print("${edge.to} ") }
            println()
        }
    }

    fun isGraphNodesLimitReached(): Boolean {
        return nodesToNeighbours.size == limitOfNodes
    }
}

/** BUSINESS LOGIC **/

fun main(args: Array<String>) {

    val graphs = List(1) {
        createNetwork(6)
    }

    val gson = GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create()

    graphs.forEachIndexed { index, network ->
        File("data_$index.json").writeText(gson.toJson(network))
    }
}

fun createNetwork(
    nodesLimit: Int = 4,
    mode: Modes = Modes.Optimum,
    solveTimeout: Int = 6666,
    q: Double = 0.5,
    l0: Double = 0.001,
    pathRateGoalWeight: Double = 0.0000001
): NetworkGraph {
    val physicalLinks = createPhysicalLinks(nodesLimit)
    val graph = generateGraph(nodesLimit)
    graph.printAdjacentNodes()
    println("-------------")
    val virtualNetwork = graph.nodesToNeighbours
        .map {
            createVirtualNetwork(it.key.id, Random.nextInt(100, 600), nodesLimit)
        }

    return NetworkGraph(mode, solveTimeout, q, l0, pathRateGoalWeight, physicalLinks, virtualNetwork)
}

fun createVirtualNetwork(
    id: Int,
    maxBandWidthSum: Int,
    linksLimit: Int
): VirtualNetwork {
    val perLinkParameters = mutableMapOf<String, VirtualLinkParameters>()
    for (i in 1..linksLimit) {
        perLinkParameters[i.toString()] =
            generateVirtualLinkParameters(i, Random.nextInt(1, 250), Random.nextDouble(1.0, 10.0))
    }

    return VirtualNetwork(perLinkParameters, id, maxBandWidthSum)
}

fun generateVirtualLinkParameters(id: Int, assignedBandwidth: Int, priceWeightedFactor: Double): VirtualLinkParameters {
    return VirtualLinkParameters(Link(id), assignedBandwidth, priceWeightedFactor)
}

fun createPhysicalLinks(physicalLinksLimit: Int): List<PhysicalLink> {
    return List(physicalLinksLimit) {
        PhysicalLink(
            it + 1,
            Random.nextInt(50, 700),
            0.0,
            Random.nextDouble(0.0, 5.0),
            Random.nextInt(1, 20)
        )
    }
}

fun generateGraph(limitOfNodes: Int = 6): Graph {
    val nodes = List(limitOfNodes) { Node(it + 1) }
    val newGraph = Graph(limitOfNodes)
    while (!(newGraph.isGraphNodesLimitReached() && newGraph.areAllNodesReachable())) {
        val n1 = nodes[Random.nextInt(0, nodes.size)]
        var n2 = nodes[Random.nextInt(0, nodes.size)]
        while (n1 == n2) {
            n2 = nodes[Random.nextInt(0, nodes.size)]
        }
        newGraph.addEdge(n1, n2)
    }

    return newGraph
}



