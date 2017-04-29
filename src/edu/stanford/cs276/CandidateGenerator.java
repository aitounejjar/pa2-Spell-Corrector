package edu.stanford.cs276;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CandidateGenerator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static CandidateGenerator cg_;

    /**
     * Constructor
     * IMPORTANT NOTE: As in the NoisyChannelModel and LanguageModel classes,
     * we want this class to use the Singleton design pattern.  Therefore,
     * under normal circumstances, you should not change this constructor to
     * 'public', and you should not call it from anywhere outside this class.
     * You can get a handle to a CandidateGenerator object using the static
     * 'get' method below.
     */
    private CandidateGenerator() {}

    public static CandidateGenerator get() throws Exception {
        if (cg_ == null) {
            cg_ = new CandidateGenerator();
        }
        return cg_;
    }

    public static final Character[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', ' ', ','};

    public static final Character[] miniAlphabet = {'X','Y'};

    // Generate all candidates for the target query
    public Set<String> getCandidates(LanguageModel lm, String query) throws Exception {

        /*
         * Your code here
         */

        Set<String> candidates = new HashSet<>();

        LinkedList<List<String>> cartesianProductQueue = new LinkedList();
        List<List<String>> lists = new ArrayList<>();

        // holds possible edits to individual terms
        Map<String, Set<String>> edits = new HashMap<>();

        String[] words = query.trim().toLowerCase().split("\\s+");

        for (String w : words) {

            Set<String> wAlternatives = getStringsWithinEditDistance1(lm, w);
            List<String> l = new ArrayList<>();
            l.addAll(wAlternatives); // sort by increasing unigram probabilities ?
            /*for (String s : set) {
                candidates.addAll(getStringsWithinEditDistance1(s));
            }*/
            edits.put(w, wAlternatives);

        }

        // sort list by increasing sizes, to increase the efficiency of the cartesian product
        lists.sort((o1, o2) -> Integer.valueOf(o1.size()).compareTo(Integer.valueOf(o2.size())));

        cartesianProductQueue.addAll(lists);

        // cartesian product over the sets
        while (true) {
            if (cartesianProductQueue.size() <= 1) {
                break;
            }

            List<String> result = new ArrayList<>();
            List<String> list1 = cartesianProductQueue.removeFirst();
            List<String> list2 = cartesianProductQueue.removeFirst();

            for (String s1 : list1) {
                for (String s2 : list2) {
                    result.add(s1+" "+s2);
                }
            }

            cartesianProductQueue.addFirst(result);
        }

        List<String> cartesianStrings = cartesianProductQueue.get(0);

        for (String possibleQuery : cartesianStrings) {
            String[] pq = possibleQuery.split("\\s+");
            for (int i=0; i<pq.length; ++i) {

            }
        }

        return candidates;
    }

    private static Set<String> getStringsWithinEditDistance1(LanguageModel lm, String w) {

        /*
          E.g.:  "stanford"
         insert -> stanfordX, Xstanford, staXnford
         delete -> tanford, stanford, stnford
         subst  -> Xtanford, stanfXrd
         transp -> stanfodr, stanofrd
         */

        Set<String> candidates = new HashSet<>();

        // only useful for debugging - to remove before submit
        Set<String> inserts = new HashSet<>();
        Set<String> deletes = new HashSet<>();
        Set<String>  substitutes = new HashSet<>();
        Set<String> transpose = new HashSet<>();


        char[] chars = w.toCharArray();

        for (int i=0; i<chars.length; ++i) {

            String d = w.substring(0,i) + w.substring(i+1);
            if (isDictionaryWord(lm, d)) {
                candidates.add(d);
                deletes.add(d);
            }


            if (i!=0) {
                // switch places between the current char with the previous one
                String t = "" + w.substring(0,i-1) + chars[i] + chars[i-1] + w.substring(i+1);
                if (isDictionaryWord(lm, t)) {
                    candidates.add(t);
                    transpose.add(t);
                }
            }

            for (int j=0; j<miniAlphabet.length; ++j) {
                // insert alphabet[j] at the index i, in the string
                String insert = w.substring(0,i) + miniAlphabet[j] + w.substring(i+1);
                if (isDictionaryWord(lm, insert)) {
                    candidates.add(insert);
                    inserts.add(insert);
                }

                // substitute char at index i with alphabet[j]
                String sub = w.substring(0,i) + miniAlphabet[j] + w.substring(i+1);
                System.out.println("::>" + sub);
                if (isDictionaryWord(lm, sub)) {
                    candidates.add(sub);
                    substitutes.add(sub);
                }
            }

        }

        return candidates;
    }

    /** returns true if w appears in the lexicon **/
    private static boolean isDictionaryWord(LanguageModel lm, String w) {
        return lm.unigrams.map().containsKey(w);

    }


    /* public static void main (String[] args) {
        // compute all words that are within an edit distance of 1
        Set<String> all = new HashSet<>();
        Set<String> set = getStringsWithinEditDistance1("stanford");
        all.addAll(set);
        for (String c : set) {
            Set<String> s = getStringsWithinEditDistance1(c);
            all.addAll(s);
        }
    }*/

}
