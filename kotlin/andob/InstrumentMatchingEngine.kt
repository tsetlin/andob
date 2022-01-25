package andob

/**
 * Implements MatchingEngine interface for a given instrument passed in the constructor
 * This implementation matches orders from HIGHEST to LOWEST and then by timestamp if prices are the same
 * A new SELL order will match the HIGHEST BUY order with a bid higher than the sell price, then next highest
 * until fully filled or there are no higher bids. A new BUY order will start with the LOWEST ask lower than its price
 * Any unmatched orders are added to the current order book
 *
 * The order book is stored in the HIGHEST to LOWEST order described above. N.B. the order is different for BUY and
 * SELL orders. This allows to go through the order book top to bottom until matches are exhausted
 * The sorting order is enforced by the default comparator of tHe Order class
 * The actual filling of the order is delegated to the Order itself
 *
 * @see Order
 */
public class InstrumentMatchingEngine(val instrument: Instrument): MatchingEngine {

    override val buyOrders : MutableList<Order> = mutableListOf()

    override val sellOrders: MutableList<Order> = mutableListOf()

    @Synchronized
    override fun match(order: Order): List<Trade> {
        if (order.instrument != this.instrument)
            return listOf()

        // Try to fill the incoming order
        // println("Match $order")
        val (unfilledOrder, fills) = fillOrder(order)

        // Keep the unmatched order
        if (unfilledOrder.quantity > 0)
            addOrder(unfilledOrder)

        return fills
    }

    /**
     * Fill an order against the current order book
     * Returns the remaining unmatched order and the matched trades
     * @see Order.fill
     */
    private fun fillOrder(order: Order) : Pair<Order, List<Trade>> {
        // Matched trades
        val fills = mutableListOf<Trade>()

        // List of orders to which we will match this order
        val contraOrderList = if (order.side == OrderSide.BUY) sellOrders else buyOrders

        if (contraOrderList.isEmpty())
            return Pair(order, fills)

        var currOrder = order.copy()
        // println("Try filling $order")

        // Fill the order from best-to-worst. BEST is defined as the highest BUY and the lowest SELL
        for (idx in contraOrderList.indices)
        {
            // Stop where there is nothing more to fill
            if (currOrder.quantity == 0)
                break

            // Try filling this order
            val fillResult = currOrder.fill(contraOrderList[idx])

            // Stop when nothing was filled on the current step
            if (fillResult.newOrder.quantity == currOrder.quantity) {
                break
            }

            // Store any found fills
            if (fillResult.trade != null)
                fills.add(fillResult.trade)

            // Update the post fill quantity
            // println("Got a fill on $fillResult.newOrder")
            currOrder = fillResult.newOrder

            // Update the quantity of the contra order in the order book after a match
            // If all quantity was matched, the quantity will be 0
            // All 0's will be removed below as we can't remove in place here
            contraOrderList[idx] = fillResult.newContraOrder
        }

        // Remove all fully matched orders, i.e. with the quantity of 0
        contraOrderList.removeAll{it.quantity <= 0}

        // println("Finish filling $order")
        return Pair(currOrder, fills)
    }

    /**
     * Add an unmatched order to the book, keeping the order book sorted from BEST to WORST
     * @see Order.compareTo
     */
    private fun addOrder(order: Order) {
        val orderList = if (order.side == OrderSide.BUY) buyOrders else sellOrders
        if (orderList.isEmpty()) {
            orderList.add(order)
            return
        }

        // Use binary search to find the insert point
        // binarySearch() returns the inverted insertion point (-insertion point - 1)
        val idx = orderList.binarySearch(order)
        orderList.add(-idx - 1, order)
    }
}