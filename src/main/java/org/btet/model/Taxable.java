package org.btet.model;

import java.math.BigDecimal;
/**
 * A sealed interface that represents a taxable expense, providing the addTax method to calculate the tax amount.
 * */
public sealed interface Taxable permits Expense {
    /**
     * Calculates the tax amount for the given amount and expense type.
     * @param taxPercentage the tax amount to add to the expense.
     */
    void addTax(BigDecimal taxPercentage);
}
