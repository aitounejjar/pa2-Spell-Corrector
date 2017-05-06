package edu.stanford.cs276;

import edu.stanford.cs276.util.Assert;
import edu.stanford.cs276.util.CartesianProduct;
import edu.stanford.cs276.util.Comparators;
import edu.stanford.cs276.util.DamerauLevenshtein;
import edu.stanford.cs276.util.Logger;
import edu.stanford.cs276.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CandidateGenerator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static CandidateGenerator cg_;
    public Map<String, Map<String, Integer>> editDistances = new HashMap<>();
    private static final int MAX_WORD_CANDIDATES = 4;


    /**
     * Constructor
     * IMPORTANT NOTE: As in the NoisyChannelModel and LanguageModel classes,
     * we want this class to use the Singleton design pattern.  Therefore,
     * under normal circumstances, you should not change this constructor to
     * 'public', and you should not call it from anywhere outside this class.
     * You can get a handle to a CandidateGenerator object using the static
     * 'get' method below.
     */
    private CandidateGenerator() {
    }

    public static CandidateGenerator get() throws Exception {
        if (cg_ == null) {
            cg_ = new CandidateGenerator();
        }
        return cg_;
    }

    public static final Character[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', ' ', ',', '\''};

    // Generate all candidates for the target query
    public Set<String> getCandidates(NoisyChannelModel nsm, LanguageModel lm, String query) throws Exception {

        /*
         * Your code here
         */

        List<List<String>> lists = new ArrayList<>();

        String[] words = query.trim().split("\\s+");

        for (int i=0; i<words.length; ++i) {
            String w = words[i];

            Pair<Set<String>, Set<String>> sets = getStringsWithinEditDistance1(lm, w, w, 0);

            // w alternatives that are valid dictionary terms
            Set<String> wAlternatives = sets.getFirst();

            if (wAlternatives.isEmpty()) {
                // come up with strings at edit distance of 2
                Set<String> nonDictAlternatives = sets.getSecond();
                for (String s : nonDictAlternatives) {
                    Set<String> wAlternativesAtDistance2 = getStringsWithinEditDistance1(lm, s, w, 1).getFirst();
                    wAlternatives.addAll(wAlternativesAtDistance2);
                }
                Logger.print(false, "dist2 alternatives of '" + w + "': " + wAlternatives.toString());
            }


            List<String> languageModelMostLikely = new ArrayList<>();
            if (wAlternatives.isEmpty()) {
                if (i + 1 < words.length) {
                    List<String> temp = lm.getWordsThatComeBefore(words[i+1]);
                    languageModelMostLikely.addAll(temp);
                }

                if (i > 0) {
                    List<String> temp = lm.getWordsThatComeAfter(words[i-1]);
                    languageModelMostLikely.addAll(temp);
                }
            }

            // sort based edit distance
            Collections.sort(languageModelMostLikely, Comparators.EDIT_DISTANCE_COMPARATOR(w));
            languageModelMostLikely = truncate(languageModelMostLikely);
            wAlternatives.addAll(languageModelMostLikely);

            // explicitly update edit distances
            for (String lmStrings : languageModelMostLikely) {
                updateEditDistances(w, lmStrings, DamerauLevenshtein.editDistance(w, lmStrings));
            }

            // sort the alternatives
            List<String> wAlternativesSorted = new ArrayList<>(wAlternatives);
            Collections.sort(wAlternativesSorted, Comparators.LANGUAGE_AND_NOISY_MODELS_COMPARATOR(w, lm, nsm.ecm_));
            Collections.reverse(wAlternativesSorted);

            int max = Math.min(wAlternatives.size(), (words.length >= 4) ? 3 : MAX_WORD_CANDIDATES);

            wAlternativesSorted = wAlternativesSorted.subList(0, max);

            lists.add(wAlternativesSorted);

            Logger.print(false, "w: " + w + " --> " + wAlternatives.toString());

        } // end for loop


        List<String> product = CartesianProduct.compute(lists);
        Assert.check(product.size() > 0, "No candidates found for the query: " + query);
        return new HashSet<>(product);
    }

    private Pair<Set<String>, Set<String>> getStringsWithinEditDistance1(LanguageModel lm, String w, String originalW, int passCount) {

        Set<String> dictAlternatives = new HashSet<>();
        Set<String> nonDictAlternatives = new HashSet<>();

        int dist = 1 + passCount;

        if (isDictionaryWord(lm, w)) {
            // the original word itself is a candidate, so we add it to the set
            dictAlternatives.add(w);
            updateEditDistances(originalW, originalW, 0);
            //return new Pair<>(dictAlternatives, nonDictAlternatives);
        }

        char[] wChars = w.toCharArray();

        for (int m = 0; m < wChars.length; ++m) {

            String d = w.substring(0, m) + w.substring(m + 1);

            if (!d.equals(originalW) && isDictionaryWord(lm, d)) {
                dictAlternatives.add(d);
                //deletes.add(d);
                updateEditDistances(originalW, d, dist);
            } else if (!d.equals(originalW) && !isDictionaryWord(lm, d)) {
                nonDictAlternatives.add(d);
                updateEditDistances(originalW, d, dist);
            }

            if (m != 0) {
                // switch places between the current char with the previous one
                String t = "" + w.substring(0, m - 1) + wChars[m] + wChars[m - 1] + w.substring(m + 1);
                if (!t.equals(originalW) && isDictionaryWord(lm, t)) {
                    dictAlternatives.add(t);
                    //transpose.add(t);
                    updateEditDistances(w, t, dist);
                } else if (!t.equals(originalW) && !isDictionaryWord(lm, t)) {
                    nonDictAlternatives.add(t);
                    updateEditDistances(w, t, dist);
                }
            }

            for (int j = 0; j < alphabet.length; ++j) {
                // insert alphabet[j] at the index i, in the string
                String i = null;
                if (m == 0) {
                    i = alphabet[j] + w;
                } else {
                    i = w.substring(0, m) + alphabet[j] + w.substring(m);
                }

                if (!i.equals(originalW) && isDictionaryWord(lm, i)) {
                    dictAlternatives.add(i);
                    //inserts.add(i);
                    updateEditDistances(originalW, i, dist);
                } else if (!i.equals(originalW) && !isDictionaryWord(lm, i)) {
                    nonDictAlternatives.add(i);
                    updateEditDistances(originalW, i, dist);
                }

                // substitute char at index i with alphabet[j]
                String s = w.substring(0, m) + alphabet[j] + w.substring(m + 1);

                if (!s.equals(originalW) && isDictionaryWord(lm, s)) {
                    dictAlternatives.add(s);
                    //substitutes.add(s);
                    updateEditDistances(originalW, s, dist);
                } else if (!s.equals(originalW) && !isDictionaryWord(lm, s)) {
                    nonDictAlternatives.add(s);
                    updateEditDistances(originalW, s, dist);
                }
            }

        }

        return new Pair<>(dictAlternatives, nonDictAlternatives);
    }

    /**
     * returns true if w appears in the lexicon
     **/
    private static boolean isDictionaryWord(LanguageModel lm, String w) {
        return lm.unigrams.map().containsKey(w) || lm.bigrams.map().containsKey(w);
    }

    private void updateEditDistances(String from, String to, int distance) {

        if (!editDistances.containsKey(from)) {
            editDistances.put(from, new HashMap<>());
        }

        if (from.equals(to)) {
            //Assert.check(distance == 0, "Edit distance must be 0 for similar strings.");
            editDistances.get(from).put(to, 0);
            return;
        } else {
            //Assert.check(distance == 1 || distance == 2, "Edit distances other than 1 or 2 are not supported.");
            editDistances.get(from).put(to, distance);
        }

    }

    private List<String> truncate(List<String> list) {
        int max = Math.min(MAX_WORD_CANDIDATES, list.size());
        return list.subList(0, max);
    }

}