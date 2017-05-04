package edu.stanford.cs276.util;

import edu.stanford.cs276.EditCostModel;
import edu.stanford.cs276.LanguageModel;

import java.util.Comparator;

public class Comparators {

    /**
     *
     * Returns a comparator that compares two strings based on their language model probabilities
     *
     * @param w     a string
     * @return      a comparator
     */
    public static Comparator<String> myComparator(String w) {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String bigram1 = w + " " + s1;
                String bigram2 = w + " " + s2;

                double pBigram1 = LanguageModel.getBigramProbability(bigram1);
                double pBigram2 = LanguageModel.getBigramProbability(bigram2);
                double pUnigram1 = LanguageModel.getUnigramProbability(s1);
                double pUnigram2 = LanguageModel.getUnigramProbability(s2);

                double p1 = Math.log(pBigram1) + Math.log(pUnigram1);
                double p2 = Math.log(pBigram2) + Math.log(pUnigram2);

                return (p1 > p2) ? 1 : -1;
            }
        };
    }

    /**
     * Returns a comparator than compares two strings based on their Damerau-Levenshtein edit distance from the passed string
     * @param from  the original word from which the distance is compared
     * @return      the comparator
     */
    public static Comparator<String> getEditDistanceComparator(String from) {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int d1 = DamerauLevenshtein.editDistance(from, s1);
                int d2 = DamerauLevenshtein.editDistance(from, s2);
                if (d1<d2) {
                    return -1;
                } else if (d1>d2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     *
     * Returns a comparator that compares two strings based on the following :
     *      P(Q|R) = P(R|Q) x P(Q) = P(noisy channel) x P(language model)
     *
     * @param w     a string
     * @param lm_   language model
     * @param ecm_  edit cost model - can be uniform or empirical based on the command line args passed to runcorrector
     * @return      a comparator
     */
    public static Comparator<String> myComparator3(String w, LanguageModel lm_, EditCostModel ecm_) {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                double pUnigram1 = lm_.getUnigramProbability(s1);
                double pUnigram2 = lm_.getUnigramProbability(s2);
                double pNoisy1 = ecm_.editProbability(w, s1, DamerauLevenshtein.editDistance(w, s1));
                double pNoisy2 = ecm_.editProbability(w, s2, DamerauLevenshtein.editDistance(w, s2));

                double p1 = Math.log(pUnigram1) + Math.log(pNoisy1);
                double p2 = Math.log(pUnigram2) + Math.log(pNoisy2);

                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

}
