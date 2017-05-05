package edu.stanford.cs276.util;

import edu.stanford.cs276.EditCostModel;
import edu.stanford.cs276.LanguageModel;

import java.util.Comparator;
import java.util.List;

public class Comparators {

    // compares strings based on how far they are, in terms of edit distance, from the passed string
    public static Comparator<String> EDIT_DISTANCE_COMPARATOR(String w) {
        return (s1, s2) -> {
            int d1 = DamerauLevenshtein.editDistance(w, s1);
            int d2 = DamerauLevenshtein.editDistance(w, s2);
            if (d1<d2) {
                return -1;
            } else if (d1>d2) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    // compares strings based on their language model / noisy channel model probabilities
    public static Comparator<String> LANGUAGE_AND_NOISY_MODELS_COMPARATOR(String w, LanguageModel lm_, EditCostModel ecm_) {
        return (s1, s2) -> {
            double pUnigram1 = lm_.getUnigramProbability(s1);
            double pUnigram2 = lm_.getUnigramProbability(s2);

            double pNoisy1 = ecm_.editProbability(w, s1, DamerauLevenshtein.editDistance(w, s1));
            double pNoisy2 = ecm_.editProbability(w, s2, DamerauLevenshtein.editDistance(w, s2));

            double p1 = (Math.log(pUnigram1)) + Math.log(pNoisy1);
            double p2 = (Math.log(pUnigram2)) + Math.log(pNoisy2);

            return Double.valueOf(p1).compareTo(Double.valueOf(p2));
        };
    }

    // compares two lists of strings based on their sizes
    public static Comparator<List<String>> SIZE_COMPARATOR() {
        return (list, otherList) -> {
            int sizeDiff = list.size() - otherList.size();

            if      (sizeDiff == 0)   return 0;
            else if (sizeDiff >  0)   return +1;
            else                      return -1;

        };
    }

}