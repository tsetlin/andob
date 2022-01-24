package andob

/**
 * Implements the matching getEngine interface. Uses the composite pattern to delegate actual order matching to
 * the InstrumentMatchingEngine for each instrument
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
            // This is a little clumsy but I couldn't
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