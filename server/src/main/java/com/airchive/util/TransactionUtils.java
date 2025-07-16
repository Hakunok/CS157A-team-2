package com.airchive.util;

/**
 * @TODO: Right now some of our multi-step operations are not atomic transactions, which means that
 * earlier steps can "succeed" even though the later ones failed which is not good.
 * We'll fix this by next code review.
 */
public class TransactionUtils {

}
