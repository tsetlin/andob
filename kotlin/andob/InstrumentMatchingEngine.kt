package andob

/**
 * Implements a matching getEngine for a given instrument
 */
public class InstrumentMatchingEngine(val instrument: Instrument): MatchingEngine {

    override val buyOrders : MutableList<Order> = mutableListOf()

    override val sellOrders: MutableList<Order> = mutableListOf()

    @Synchronized
    override fun match(order: Order): List<Trade> {
        if (order.instrument != this.instrument)
            return listOf()

        // println("Match $order")
        val (unfilledOrder, fills) = fillOrder(order)

        if (unfilledOrder.quantity > 0)
            addOrder(unfilledOrder)

        return fills
    }

    /**
     * Add an unmatched order to the book, keeping them sorted from BEST to WORST
     */
    private fun addOrder(order: Order) {
        val orderList = if (order.side == OrderSide.BUY) buyOrders else sellOrders
        if (orderList.isEmpty()) {
            orderList.add(order)
            return
        }

        // binarySearch() returns the inverted insertion point (-insertion point - 1)
        val idx = orderList.binarySearch(order)
        orderList.add(-idx - 1, order)
    }

    /**
     * Try to match this order against the current order book
     * Returns the remaining unmatched order and the matched trades
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

            // println("Got a fill on $fillResult.newOrder")
            currOrder = fillResult.newOrder

            // Update the quantity in the order book after a match
            // If an existing order was matched, the quantity will be 0
            contraOrderList[idx] = fillResult.newContraOrder
        }

        // Remove any fully matched orders, i.e. with 0 quantity
        contraOrderList.removeAll{it.quantity <= 0}

        // println("Finish filling $order")
        return Pair(currOrder, fills)
    }
}