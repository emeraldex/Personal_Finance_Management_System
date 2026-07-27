package com.expense.core.network;

import com.expense.core.util.Money;

import java.time.LocalDate;

/**
 * One transaction pulled from a bank feed, before the user has confirmed it.
 * Follows the project sign convention: debits (expenses) are negative, credits
 * (income) are positive, so the importer can route each entry to the expense or
 * income flow by sign alone.
 *
 * @param postedOn    the date the bank posted the transaction
 * @param description the bank's free-text narrative (feeds the categoriser)
 * @param amount      signed amount in the bank account's currency
 * @param externalId  the bank's unique id for the transaction, used to
 *                    de-duplicate entries seen across overlapping fetches
 */
public record BankFeedEntry(LocalDate postedOn, String description, Money amount, String externalId) {
}
