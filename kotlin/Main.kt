import andob.*

/**
 * A helper class to print orders in the requested format
 */
class OrderLine(val order: Order) {
    override fun toString(): String {
        val attrs = listOf(order.orderID, order.side, order.instrument.symbol, order.quantity, order.price)
        return attrs.joinToString(separator = "\t")
    }
}

/**
 * A helper class to print trades in the requested format
 */
class TradeLine(val trade: Trade) {
    override fun toString(): String {
        val order = trade.order
        val contra = trade.contraOrder
        val attrs = listOf("TRADE", order.instrument.symbol, order.orderID, contra.orderID, order.quantity, contra.price)
        return attrs.joinToString(separator = "\t")
    }
}

//val getEngine = InstrumentMatchingEngine(Instrument("ETHUSD"))
val engine = MatchingEngineSystem()


fun runMatchingEngine() {
    // Read stdin. From https://stackoverflow.com/questions/53575064/clean-way-of-reading-all-input-lines-in-kotlin
    val input = generateSequence(::readln)

    try {
        for (l in input) {
            if (l.isNullOrBlank())
                break

            // Read the order
            val order : Order
            try {
                order = buildOrder(l)
            } catch (e: BuildOrderException) {
                // Print error and continue processing orders
                System.err.println(e)
                continue
            }

            // Try to match
            val trades = engine.match(order)
            if (trades.isNotEmpty())
                for (t in trades)
                    println(TradeLine(t))
        }
    }
    catch(e: RuntimeException)
    {
        // This is to catch kotlin.io.ReadAfterEOFException which occurs when there is no blank line before EOF
        // ReadAfterEOFException is an internal class which we can't catch explicitly
    }

    // All orders were processed. Print the state of the order book
    for (o in engine.sellOrders)
        println(OrderLine(o))
    for (o in engine.buyOrders)
        println(OrderLine(o))
}


fun main() {
    runMatchingEngine()
}