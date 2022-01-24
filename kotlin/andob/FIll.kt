package andob

/**
 * Represents the result of a fill attempt
 */
public data class OrderFill(val newOrder: Order, val newContraOrder: Order, val trade: Trade? = null)
