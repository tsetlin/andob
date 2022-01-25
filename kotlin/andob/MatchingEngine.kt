package andob

/**
 * An interface describing a matching engine. The main method that this interface declares is match().
 * The implementation determines how orders are matched against one another
 */
interface MatchingEngine {
    /**
     * Process an order and return any matches found
     */
    fun match(order: Order) : List<Trade>

    /**
     * The current state of the BUY side of the order book
     */
    val buyOrders : List<Order>

    /**
     * The current state of the SELL side of the order book
     */
    val sellOrders: List<Order>

}
