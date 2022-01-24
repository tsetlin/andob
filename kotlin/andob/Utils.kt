package andob

import java.util.Date
import java.time.Instant

class BuildOrderException(override val message: String?, override val cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * A utility to build an orders from a space-separated line. Expects a line with 5 parameters:
 * order ID, BUY/SELL, instrument, order quantity, order price
 */
public fun buildOrder(line: String) : Order
{
    if (line.isNullOrBlank())
        throw BuildOrderException("Unable to build an order. Line not found")

    val arr = line.trimEnd().split(regex = "\\s+".toRegex())

    // Print error on the stderr
    if (arr.size != 5)
        throw BuildOrderException("Unable to build an order from: $line")

    val (orderID, side, instrument, size, price) =  arr
    val now = Date.from(Instant.now())

    return try {
        Order(orderID = orderID, side = OrderSide.valueOf(side.uppercase()),
            instrument = Instrument(instrument.uppercase()), quantity = size.toInt(), price = price.toFloat(),
            timestamp = now)
    } catch (e: Exception) {
        throw BuildOrderException("Unable to build an order from: $line", e)
    }
}

