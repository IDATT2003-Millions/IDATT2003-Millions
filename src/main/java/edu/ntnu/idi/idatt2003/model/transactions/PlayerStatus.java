package edu.ntnu.idi.idatt2003.model.transactions;

/**
 * Represents the progression level of a {@link Player} in the Millions game.
 *   <li>{@link #NOVICE} – starting level; no requirements</li>
 *   <li>{@link #INVESTOR} – traded at least 10 weeks and net worth grew ≥ 20%</li>
 *   <li>{@link #SPECULATOR} – traded at least 20 weeks and net worth at least doubled</li>
 * </ul>
 */
public enum PlayerStatus {
    NOVICE,
    INVESTOR,
    SPECULATOR
}
