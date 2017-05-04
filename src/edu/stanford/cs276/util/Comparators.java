package edu.stanford.cs276.util;

import edu.stanford.cs276.LanguageModel;

import java.util.Comparator;

public class Comparators {

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

}
