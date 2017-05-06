package edu.stanford.cs276;

/**
 * Implement {@link EditCostModel} interface by assuming
 * that any single edit in the Damerau-Levenshtein distance is equally likely,
 * i.e., having the same probability
 */
public class UniformCostModel implements EditCostModel {

    private static final long serialVersionUID = 1L;

    @Override
    public double editProbability(String original, String R, int distance) {

        // TODO: Your code here
        // calculate the probability of seeing some R given its edit distance from Q

        double p = Math.pow(UNIFORM_EDIT_PROBABILITY, distance);
        if (original.equals(R)) {
            return (ZERO_EDIT_PROBABILITY);
        }
        return p;
    }
}
