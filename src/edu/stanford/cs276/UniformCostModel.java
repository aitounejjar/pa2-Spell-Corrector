package edu.stanford.cs276;

/**
 * Implement {@link EditCostModel} interface by assuming
 * that any single edit in the Damerau-Levenshtein distance is equally likely,
 * i.e., having the same probability
 */
public class UniformCostModel implements EditCostModel {

    private static final long serialVersionUID = 1L;

    private static final double UNIFORM_EDIT_PROBABILITY = 0.01;

    // probability that user entered the correct term (R = Q)
    private static final double ZERO_EDIT_PROBABILITY = 0.95;

    @Override
    public double editProbability(String original, String R, int distance) {

        // TODO: Your code here
        // calculate the probability of seeing some R given its edit distance from Q

        double p = 0;

        if (distance == 0) {
            // this means that R = Q
            p = ZERO_EDIT_PROBABILITY;
        } else {
            p = UNIFORM_EDIT_PROBABILITY;
        }


        return p;
    }
}
