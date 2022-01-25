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

/**
 * The target instance of the matching engine
 */
val engine = MatchingEngineSystem()
//val getEngine = InstrumentMatchingEngine(Instrument("ETHUSD"))


/**
 * This is the main function to run the engine. It reads stdin, parses orders, and passes them to the matching engine.
 * The engine tries to match the order right away. If a match is found, it is printed on a stdout right away.
 * After all orders are processed, it prints the state of order book.
 * If it encounters an invalid order, it will print an error message but will continue processing orders
 * An EMPTY LINE indicates the end of orders and stops the process
 */
fun runMatchingEngine() {
    // Read stdin. From https://stackoverflow.com/questions/53575064/clean-way-of-reading-all-input-lines-in-kotlin
    val input = generateSequence(::readln)

    try {
        for (l in input) {
            // Stop if an empty is encountered
            if (l.isBlank())
                break

            // Read and order
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
        // This is NOT IDEAL.  It's greedy and will eat ALL runtime exceptions in the engine but continue running
        System.err.println(e)
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