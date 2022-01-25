package andob

/**
 * Implements MatchingEngine interface. It creates and maintains a MatchingEngine for each instrument encountered.
 * Uses the Composite Pattern to delegate actual order matching tothe InstrumentMatchingEngine for each instrument
 *
 * @see InstrumentMatchingEngine
 */
public class MatchingEngineSystem() : MatchingEngine {

    /* This can probably be converted to a set */
    private val instrumentEngineMap: MutableMap<Instrument, InstrumentMatchingEngine>  = mutableMapOf()

    /**
     * Delegate matching to the matching getEngine of each instrument
     */
    @Synchronized
    override fun match(order: Order): List<Trade> {
        if (!instrumentEngineMap.containsKey(order.instrument))
            instrumentEngineMap[order.instrument] = InstrumentMatchingEngine(order.instrument)

        return instrumentEngineMap[order.instrument]!!.match(order = order)
    }

    override val buyOrders: List<Order>
        @Synchronized
        get() {
            // This is a little clumsy, but I couldn't find a better way
            val allOrders = mutableListOf<Order>()
            for (o in instrumentEngineMap.values.map { it.buyOrders })
                allOrders.addAll(o)
            return allOrders
        }

    override val sellOrders: List<Order>
        @Synchronized
        get() {
            val allOrders = mutableListOf<Order>()
            for (o in instrumentEngineMap.values.map { it.sellOrders })
                allOrders.addAll(o)
            return allOrders
        }
}