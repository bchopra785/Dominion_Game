# WeightedPlayer Weight Optimization Log

## Overview
Automated optimization of WeightedPlayer card weights using evolutionary algorithm testing framework.

**Final Result:** 85 wins → **92 wins** (+8.2% improvement, 92% win rate vs BigMoney and Strategy)

---

## Optimization History

### Phase 1: Quick Optimization (3 generations)
**Date:** 2026-04-19
**Configuration:** 3 generations, 2 configs, 2 games per config
**Result:** 100% win rate in test environment (statistically insignificant - too few games)

**Best Config (Generation 3):**
```
Circulation: 3.59
Bridging:    3.54
Attack:      2.37
Money:       1.76
Defense:     1.42
Points:      1.08
Threat Minor Threshold: 2
Threat Moderate Threshold: 6
```

**Tournament Result:** 83 wins (-2 vs baseline 85)
**Conclusion:** Quick optimization failed to improve real performance. Likely overfit to test environment.

---

### Phase 2: Medium Optimization (5 generations) ✅ SUCCESSFUL
**Date:** 2026-04-19
**Configuration:** 5 generations, 5 configs/gen, 10 games per config
**Result:** 100% win rate across all top 5 configurations at generation 5

**Best Config (Generation 5):**
```
Circulation: 3.67
Bridging:    4.82    ← +38% increase from baseline
Attack:      1.59    ← -36% decrease from baseline
Money:       2.58    ← +29% increase from baseline
Defense:     1.00    ← -33% decrease from baseline
Points:      1.16    ← +16% increase from baseline
Threat Minor Threshold: 3 (vs 2)
Threat Moderate Threshold: 5 (vs original 5)
```

**Tournament Result:** 92 wins (+7 vs baseline 85) ✅
**Win Rate:** 92% (92/100 games against BigMoney#1 and Strategy#1)

---

## Weight Comparison

| Category | Original | Quick Opt | Medium Opt | Delta from Original |
|----------|----------|-----------|-----------|---------------------|
| **Circulation** | 4.00 | 3.59 | 3.67 | -8% |
| **Bridging** | 3.50 | 3.54 | 4.82 | **+38%** ⬆️ |
| **Attack** | 2.50 | 2.37 | 1.59 | **-36%** ⬇️ |
| **Money** | 2.00 | 1.76 | 2.58 | +29% |
| **Defense** | 1.5 | 1.42 | 1.00 | -33% |
| **Points** | 1.00 | 1.08 | 1.16 | +16% |

---

## Key Insights

### Strategic Discovery
The optimization revealed that **card generation (Bridging) is significantly more valuable than aggressive card plays (Attack)**. This represents a fundamental shift in strategy:

1. **Bridging Priority +38%:** Card draw and action chaining enable more total plays per turn
2. **Attack Reduction -36%:** Direct aggression against opponents is less effective than maximizing own tempo
3. **Money Boost +29%:** Resource production became more important (enables budget for better cards)
4. **Defense Minimization -33%:** Defensive cards provide minimal value; better to focus on tempo

### Game Theory Implication
In this Dominion-like game, **the best defense is a good offense (through card draw)**, not literal defense cards. A player that generates more actions and draws more cards naturally has better board position without needing explicit defense.

### Threat Threshold Adjustment
Minor threat threshold increased from 2→3, meaning WeightedPlayer is less reactive to opponent attack acquisitions. This aligns with reduced defense weight — the strategy is more "offense-oriented" and less defensive.

---

## Tournament Results Summary

### Baseline (Original Weights)
- Weighted#1: 85 wins (85%)
- BigMoney#1: 13 wins (13%)
- Strategy#1: 8 wins (8%)
- **Gap:** 72 wins over 2nd place

### Optimized (Medium Optimization Weights)
- Weighted#1: 92 wins (92%)
- BigMoney#1: 6 wins (6%)
- Strategy#1: 6 wins (6%)
- **Gap:** 86 wins over 2nd place (dominance increased by 20%)

---

## Implementation Details

### Files Modified
1. **WeightedPlayer.java**
   - Updated static weight initialization with optimized values
   - Adjusted threat thresholds

### Framework Created
1. **WeightConfig.java** - Encapsulates weight configuration with mutation support
2. **WeightedPlayerOptimizer.java** - Evolutionary algorithm framework
3. **WeightOptimizationTest.java** - JUnit test harness for optimization runs

### How to Re-Run Optimization

**Quick test (for validation):**
```bash
mvn test -Dtest=WeightOptimizationTest#testQuickWeightOptimization
```

**Medium test (recommended for production):**
```bash
mvn test -Dtest=WeightOptimizationTest#testMediumWeightOptimization
```

**Full optimization (thorough but slow):**
```bash
mvn test -Dtest=WeightOptimizationTest#testFullWeightOptimization
```

---

## Future Optimization Opportunities

1. **Full Optimization Run** - 10 generations, 8 configs could potentially find 93-95 win configurations
2. **Fine-tuning Attack Cards** - Attack weight of 1.59 might be slightly too low; could test 1.8-2.0
3. **Action Card Values** - The static actionCardValues map for lookahead search could also be optimized
4. **Adaptive Weights** - Could implement game-phase-aware weights that shift strategy mid-game
5. **Opponent-Specific Strategies** - Different weight sets for different opponent combinations

---

## Notes for Future Development

- The optimizer framework is fully functional and can be re-run anytime
- Medium optimization takes ~5-10 minutes and provides good results
- All 5 top configurations from medium optimization achieved 100% win rate in test environment
- The diversity of top configs (different weight combinations all equally effective) suggests a robust solution space

---

## Related Documentation
- See `src/main/java/edu/brandeis/cosi103a/groupb/rating/WeightedPlayerOptimizer.java` for optimizer details
- See `src/main/java/edu/brandeis/cosi103a/groupb/WeightedPlayer.java` for current strategy implementation
- See `src/test/java/edu/brandeis/cosi103a/groupb/WeightOptimizationTest.java` for testing approach
