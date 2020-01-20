import com.google.gson.FieldNamingPolicy
import kotlin.random.Random
import com.google.gson.GsonBuilder
import java.io.File


/**  ------ MODEL DECLARATION ------ **/

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
    val bandwidthPrice: Int,
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


/** ------ BUSINESS LOGIC ------ **/


val GRAPHS_LIMIT = 2

fun main(args: Array<String>) {
    val gsonPretty = GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create()
    generateNetworks(GRAPHS_LIMIT).forEachIndexed {
        index, network -> File("network_${index}.json").writeText(gsonPretty.toJson(network))

    }
}

fun generateNetworks(graphsLimit: Int): List<NetworkGraph> {
    return List(graphsLimit) { generateNetwork(Random.nextInt(2,4)) }

}

fun generateNetwork(
    linksLimit: Int = 2,
    mode: Modes = Modes.Optimum,
    solveTimeout: Int = 6666,
    q: Double = 0.5,
    l0: Double = 0.001,
    pathRateGoalWeight: Double = 0.0000001
): NetworkGraph {

    val physicalLinks = generatePhysicalLinks(linksLimit)
    val virtualNetwork = physicalLinks.map {
        generateVirtualNetwork(it.id, Random.nextInt(100, 700), linksLimit)
    }

    return NetworkGraph(mode, solveTimeout, q, l0, pathRateGoalWeight, physicalLinks, virtualNetwork)
}

fun generateVirtualNetwork(
    id: Int,
    maxBandwidth: Int,
    linksLimit: Int
): VirtualNetwork {
    val perLinkParameters = mutableMapOf<String, VirtualLinkParameters>()
    for (i in 1..linksLimit) {
        perLinkParameters[i.toString()] =
            generateVirtualLinkParameters(i, Random.nextInt(1, 300), Random.nextDouble(1.0, 15.0))
    }

    return VirtualNetwork(perLinkParameters, id, maxBandwidth)
}

fun generateVirtualLinkParameters(id: Int, assignedBandwidth: Int, priceWeightedFactor: Double): VirtualLinkParameters {
    return VirtualLinkParameters(Link(id), assignedBandwidth, priceWeightedFactor)
}

fun generatePhysicalLinks(linksLimit: Int): List<PhysicalLink> {
    return List(
        linksLimit
    ) {
        PhysicalLink(
            it+1,
            Random.nextInt(100, 1000),
            Random.nextInt(0, 10),
            Random.nextDouble(0.1, 0.9),
            Random.nextInt(1, 30)
        )
    }
}

