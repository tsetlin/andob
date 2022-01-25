package andob

import kotlin.math.min
import java.util.Date
import kotlin.Comparable

public enum class OrderSide (val side: Int) {
    BUY(side = -1),
    SELL(side = 1)
}

data class Instrument(val symbol: String) : Comparable<Instrument> {
    override fun compareTo(other: Instrument) = symbol.compareTo(other.symbol)
}

/**
 * Represents an order sent into the MatchingEngine. Beside having attributes for an order, it has two other
 * important functions:
 *  - Knows how to fill this order against a contra order enforcing bid <= ask rule
 *  - Implements the sort order needed to facilitate filling orders from HIGH to LOW
 */
public data class Order(
    val orderID: String, val side: OrderSide, val instrument: Instrument, val quantity: Int, val price: Float,
    val timestamp: Date) : Comparable<Order> {

    init {
        require(quantity >= 0) { "Order size must be positive" }
        require(price > 0) { "Order price must be positive" } /* Maybe relaxed to negative prices? */
    }

    /**
     * An internal helper field. Ensure that:
     * BUY orders are sorted high-to-low price (has the negative signed price)
     * SELL orders are sorted low-to-high (has the positive signed price)
     */
    private val signedPrice : Float
    get() = side.side * price

    override fun compareTo(other: Order) = compareValuesBy(this, other,
        { it.instrument },
        { it.side },
        { it.signedPrice },
        { it.timestamp})

    /**
     * Do a partial or a full fill given a contra order
     * @return an order fill object. If it can match against the contra order, the order fill will contain
     * a trade object with the quantity filled. Otherwise, the trade object is null
     */
    fun fill(contraOrder: Order) : OrderFill {
        // How much of the other order we can fill with this order
        if (side == contraOrder.side || quantity == 0 || contraOrder.quantity == 0)
            return OrderFill(this, contraOrder)

        // I am a BUY order
        if (side == OrderSide.BUY && contraOrder.price > price) {
            // I am buying at a lower price, can't fill
            return OrderFill(this, contraOrder)
        }

        // I am a SELL order
        if (side == OrderSide.SELL && contraOrder.price < price) {
            // I am selling at a higher price than the BUY wants to pay, won't fill
            return OrderFill(this, contraOrder)
        }

        // Compute the quantity we can fill
        val filledQuantity = min(quantity, contraOrder.quantity)

        val filledOrder = copy(quantity=filledQuantity)
        val newOrder = copy(quantity=(quantity - filledQuantity))

        val filledContraOrder = contraOrder.copy(quantity=filledQuantity)
        val newContraOrder = contraOrder.copy(quantity=(contraOrder.quantity - filledQuantity))

        return OrderFill(newOrder, newContraOrder, Trade(filledOrder, filledContraOrder))
    }
}

