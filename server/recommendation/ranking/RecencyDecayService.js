/**
 * RecencyDecayService
 * Applies exponential decay to historical user signals so that recent interactions
 * have higher influence than older ones.
 */

class RecencyDecayService {
  constructor(halfLifeDays = 21) {
    // lambda = ln(2) / halfLifeDays
    this.lambda = Math.log(2) / halfLifeDays;
  }

  /**
   * Computes the decay weight in [0, 1] for an interaction that happened at `timestamp`
   * @param {number|Date} timestamp
   * @returns {number}
   */
  calculateWeight(timestamp) {
    if (!timestamp) return 0.5;
    const time = typeof timestamp === 'number' ? timestamp : new Date(timestamp).getTime();
    const now = Date.now();
    const diffMs = Math.max(0, now - time);
    const diffDays = diffMs / (1000 * 60 * 60 * 24);

    return Math.exp(-this.lambda * diffDays);
  }

  /**
   * Decays an existing affinity score based on last update timestamp
   */
  decayScore(score, lastTimestamp) {
    const decayWeight = this.calculateWeight(lastTimestamp);
    return score * decayWeight;
  }
}

const recencyDecayService = new RecencyDecayService();

module.exports = {
  RecencyDecayService,
  recencyDecayService
};
