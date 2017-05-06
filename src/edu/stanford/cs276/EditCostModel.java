package edu.stanford.cs276;

import java.io.Serializable;

public interface EditCostModel extends Serializable {


    double UNIFORM_EDIT_PROBABILITY = 0.01;

    // probability that user entered the correct term (R = Q)
    double ZERO_EDIT_PROBABILITY = 0.95;

    /**
     * This interface method calculates the P(R|Q) given the edit distance. Depending on the
     * channel model you implement (uniform or empirical), the details of the method will differ.
     *
     * @param original
     * @param R
     * @param distance
     * @return
     */
    public double editProbability(String original, String R, int distance);
}
