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
    public Map<String, Map<String, Integer>> editDistances = new HashMap<>();
    private static String originalTerm = null;

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

    public static final Character[] miniAlphabet = alphabet; //{'X','Y'};

    // Generate all candidates for the target query
    public Set<String> getCandidates(LanguageModel lm, String query) throws Exception {

        /*
         * Your code here
         */

        Set<String> candidates = new HashSet<>();
        LinkedList cartesianProductQueue = new LinkedList();
        List<List<String>> lists = new ArrayList<>();

        // holds possible edits to individual terms
        //Map<String, Set<String>> edits = new HashMap<>();

        String[] words = query.trim().toLowerCase().split("\\s+");

        for (String w : words) {

            Set<String> wAlternatives = getStringsWithinEditDistance1(lm, w, 0);

            for (String s : wAlternatives) {
                wAlternatives = getStringsWithinEditDistance1(lm, w, 1);
                lists.add(new ArrayList<>(wAlternatives)); // sort by increasing unigram probabilities ?
            }

        }

        // sort list by increasing sizes, to increase the efficiency of the cartesian product
        //lists.sort((o1, o2) -> Integer.valueOf(o1.size()).compareTo(Integer.valueOf(o2.size())));

        cartesianProductQueue.addAll(lists);

        // cartesian product over the sets
        while (true) {
            if (cartesianProductQueue.size() <= 1) {
                break;
            }

            List<String> result = new ArrayList<>();
            List<String> list1 = (List<String>) cartesianProductQueue.removeFirst();
            List<String> list2 = (List<String>) cartesianProductQueue.removeFirst();

            for (String s1 : list1) {
                for (String s2 : list2) {
                    result.add(s1+" "+s2);
                }
            }

            cartesianProductQueue.addFirst(result);
        }

        List<String> cartesianStrings = (List<String>) cartesianProductQueue.get(0);

        for (String possibleQuery : cartesianStrings) {
            String[] pq = possibleQuery.split("\\s+");
            for (int i=0; i<pq.length; ++i) {
                // weed out some candidates ...
            }
        }

        return new HashSet<>(cartesianStrings);
    }

    private Set<String> getStringsWithinEditDistance1(LanguageModel lm, String w, int passCounter) {

        int dist = passCounter + 1;

        /*
          E.g.:  "stanford"
         insert -> stanfordX, Xstanford, staXnford
         delete -> tanford, stanford, stnford
         subst  -> Xtanford, stanfXrd
         transp -> stanfodr, stanofrd
         */

        Set<String> candidates = new HashSet<>();

        // the original word itself is a candidate, so we add it to the set
        candidates.add(w);

        // only useful for debugging - do not commit uncommented - remove before submit
        /*Set<String>  inserts = new HashSet<>();
        Set<String> deletes = new HashSet<>();
        Set<String>  substitutes = new HashSet<>();
        Set<String> transpose = new HashSet<>();*/

        updateEditDistances(w, w, dist);

        char[] wChars = w.toCharArray();

        for (int m=0; m<wChars.length; ++m) {

            String d = w.substring(0,m) + w.substring(m+1);
            if ( !d.equals(w) && isDictionaryWord(lm, d)) {
                candidates.add(d);
                //deletes.add(d);
                updateEditDistances(w, d, dist);
            }

            if (m!=0) {
                // switch places between the current char with the previous one
                String t = "" + w.substring(0,m-1) + wChars[m] + wChars[m-1] + w.substring(m+1);
                if (!t.equals(w) && isDictionaryWord(lm, t)) {
                    candidates.add(t);
                    //transpose.add(t);
                    updateEditDistances(w, t, dist);
                }
            }

            for (int j=0; j<miniAlphabet.length; ++j) {
                // insert alphabet[j] at the index i, in the string
                String i = w.substring(0,m) + miniAlphabet[j] + w.substring(m+1);
                if (!i.equals(w) && isDictionaryWord(lm, i)) {
                    candidates.add(i);
                    //inserts.add(i);
                    updateEditDistances(w, i, dist);
                }

                // substitute char at index i with alphabet[j]
                String s = w.substring(0,m) + miniAlphabet[j] + w.substring(m+1);
                //System.out.println("::>" + s);
                if (!s.equals(w) && isDictionaryWord(lm, s)) {
                    candidates.add(s);
                    //substitutes.add(s);
                    updateEditDistances(w, s, dist);
                }
            }

        }

        return candidates;
    }

    /** returns true if w appears in the lexicon **/
    private static boolean isDictionaryWord(LanguageModel lm, String w) {
        return (lm==null) || lm.unigrams.map().containsKey(w); // remove lm==null check
    }

    private void updateEditDistances(String from, String to, int distance) {

        if (from.equals(to)) {
            if (!editDistances.containsKey(from)) {
                editDistances.put(from, new HashMap<>());
            }
            editDistances.get(from).put(to, 0);
            return;
        }

        if (distance == 1) {
            if (!editDistances.containsKey(from)) {
                editDistances.put(from, new HashMap<>());
            }
            editDistances.get(from).put(to, distance);
        } else {
            editDistances.get(from).put(to, distance);
        }

    }


}
