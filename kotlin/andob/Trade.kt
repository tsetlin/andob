package andob

public data class Trade(val order: Order, val contraOrder: Order)
{
    init {
        require(order.quantity > 0) { "Invalid order in trade" }
        require(contraOrder.quantity > 0) { "Invalid contra order in trade" }
        require(order.quantity == contraOrder.quantity) { "Order and contra order quantity must be equal" }
    }
}
