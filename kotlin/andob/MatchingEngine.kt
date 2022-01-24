package andob

interface MatchingEngine {
    fun match(order: Order) : List<Trade>

    val buyOrders : List<Order>
    val sellOrders: List<Order>

}
