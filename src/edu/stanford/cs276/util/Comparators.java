package edu.stanford.cs276.util;

import edu.stanford.cs276.EditCostModel;
import edu.stanford.cs276.LanguageModel;

import java.util.Comparator;

public class Comparators {

    public static Comparator<String> myComparator(String w, LanguageModel lm_) {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String bigram1 = w + " " + s1;
                String bigram2 = w + " " + s2;

                double pBigram1 = lm_.getBigramProbability(bigram1);
                double pBigram2 = lm_.getBigramProbability(bigram2);
                double pUnigram1 = lm_.getUnigramProbability(s1);
                double pUnigram2 = lm_.getUnigramProbability(s2);

                double p1 = Math.log(pBigram1) + Math.log(pUnigram1);
                double p2 = Math.log(pBigram2) + Math.log(pUnigram2);

                return (p1 > p2) ? 1 : -1;
            }
        };
    }

    public static Comparator<String> getEditDistanceComparator(String w) {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int d1 = DamerauLevenshtein.editDistance(w, s1);
                int d2 = DamerauLevenshtein.editDistance(w, s2);
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

    private static final double LAMBDA = 0.1;
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
                    return -1;
                } else if (p1 < p2) {
                    return +1;
                } else {
                    return 0;
                }
            }
        };
    }

}