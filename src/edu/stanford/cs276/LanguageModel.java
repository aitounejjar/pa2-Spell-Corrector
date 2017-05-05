package edu.stanford.cs276;

import edu.stanford.cs276.util.Comparators;
import edu.stanford.cs276.util.Dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LanguageModel class constructs a language model from the training corpus.
 * This model will be used to score generated query candidates.
 * <p>
 * This class uses the Singleton design pattern
 * (https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class LanguageModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static LanguageModel lm_;

    Dictionary unigrams = new Dictionary();
    Dictionary bigrams = new Dictionary();

    Map<String, Set<String>> w1w2_map = new HashMap<>();
    Map<String, Set<String>> w2w1_map = new HashMap<>();

    // counts the total number of terms in the corpus
    private int termCounter = 1;

    // maps to store probabilities of unigrams and bigrams
    private Map<String, Double> unigramProbabilities = new HashMap<>();
    private Map<String, Double> bigramProbabilities  = new HashMap<>();

    private static final double LAMBDA = 0.1;

  /*
   * Feel free to add more members here (e.g., a data structure that stores bigrams)
   */

    /**
     * Constructor
     * IMPORTANT NOTE: you should NOT change the access level for this constructor to 'public',
     * and you should NOT call this constructor outside of this class.  This class is intended
     * to follow the "Singleton" design pattern, which ensures that there is only ONE object of
     * this type in existence at any time.  In most circumstances, you should get a handle to a
     * NoisyChannelModel object by using the static 'create' and 'load' methods below, which you
     * should not need to modify unless you are making substantial changes to the architecture
     * of the starter code.
     * <p>
     * For more info about the Singleton pattern, see https://en.wikipedia.org/wiki/Singleton_pattern.
     */
    private LanguageModel(String corpusFilePath) throws Exception {
        constructDictionaries(corpusFilePath);
    }

    /**
     * This method is called by the constructor, and computes language model parameters
     * (i.e. counts of unigrams, bigrams, etc.), which are then stored in the class members
     * declared above.
     */
    public void constructDictionaries(String corpusFilePath) throws Exception {

        System.out.println("Constructing dictionaries...");
        File dir = new File(corpusFilePath);
        for (File file : dir.listFiles()) {
            if (".".equals(file.getName()) || "..".equals(file.getName())) {
                continue; // Ignore the self and parent aliases.
            }
            System.out.printf("Reading data file %s ...\n", file.getName());
            BufferedReader input = new BufferedReader(new FileReader(file));
            String line = null;

            System.out.println(file.getName() + "----------------------------------- ");
            while ((line = input.readLine()) != null) {

                if (line.isEmpty()) {
                    continue;
                }

                /*
                 * Remember: each line is a document (refer to PA2 handout)
                 * TODO: Your code here
                 */

                String[] words = line.trim().toLowerCase().split("\\s+");
                String previousWord = words[0];
                unigrams.add(previousWord);

                for (int i=1; i<words.length; ++i) {
                    String w = words[i];
                    unigrams.add(w);

                    String bigram = previousWord + " " + w ;

                    bigrams.add(bigram);

                    if (!w1w2_map.containsKey(previousWord)) {
                        w1w2_map.put(previousWord, new HashSet<>());
                    }
                    w1w2_map.get(previousWord).add(w);


                    if (!w2w1_map.containsKey(w)) {
                        w2w1_map.put(w, new HashSet<>());
                    }
                    w2w1_map.get(w).add(previousWord);

                    previousWord = w;
                }

            }
            input.close();
        }

        computeUnigramProbabilities();
        computeBigramProbabilities();

        System.out.println("Done.");
    }

    private void computeUnigramProbabilities() {
        HashMap<String, Integer> map = unigrams.map();
        int t = unigrams.termCount();
        for (String w : map.keySet()) {
            int wCount = map.get(w);
            double p =  wCount/(double)t;
            unigramProbabilities.put(w, p);
        }
    }

    private void computeBigramProbabilities() {
        HashMap<String, Integer> map = bigrams.map();
        for (String w1w2 : map.keySet()) {

            String[] pair = w1w2.split("\\s+");

            String w1 = pair[0];
            String w2 = pair[1];

            // compute the bigram's probability
            int w1w2Count = map.get(w1w2);
            int w1Count = unigrams.map().get(w1);
            double p = w1w2Count/(double)w1Count;


            // interpolate the result
            double pInterpolated = (LAMBDA * unigramProbabilities.get(w2)) + ((1-LAMBDA) * p);

            bigramProbabilities.put(w1w2, pInterpolated);
        }
    }

    /**
     * Creates a new LanguageModel object from a corpus. This method should be used to create a
     * new object rather than calling the constructor directly from outside this class
     */
    public static LanguageModel create(String corpusFilePath) throws Exception {
        if (lm_ == null) {
            lm_ = new LanguageModel(corpusFilePath);
        }
        return lm_;
    }

    /**
     * Loads the language model object (and all associated data) from disk
     */
    public static LanguageModel load() throws Exception {
        try {
            if (lm_ == null) {
                FileInputStream fiA = new FileInputStream(Config.languageModelFile);
                ObjectInputStream oisA = new ObjectInputStream(fiA);
                lm_ = (LanguageModel) oisA.readObject();
            }
        } catch (Exception e) {
            throw new Exception("Unable to load language model.  You may not have run buildmodels.sh!");
        }
        return lm_;
    }

    /**
     * Saves the object (and all associated data) to disk
     */
    public void save() throws Exception {
        FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
        save.writeObject(this);
        save.close();
    }

    public double getBigramProbability(String bigram) {
        double d = 0.0;
        if (bigramProbabilities.containsKey(bigram)) {
            d = bigramProbabilities.get(bigram);
        }
        return d;
    }

    public double getUnigramProbability(String unigram) {
        double d = 0.0;
        if (unigramProbabilities.containsKey(unigram)) {
            d = unigramProbabilities.get(unigram);
        }
        return d;
    }

    public List<String> getWordsThatComeBefore(String w) {
        return helper(w, w2w1_map);
    }

    public List<String> getWordsThatComeAfter(String w) {
        return helper(w, w1w2_map);
    }

    private List<String> helper(String w, Map<String, Set<String>> map) {
        List<String> list = new ArrayList<>();

        if (!map.containsKey(w)) {
            return list;
        }

        List<String> words = new ArrayList<>(map.get(w));

        Collections.sort(words, Comparators.myComparator(w, lm_));

        return new ArrayList<>(words);
    }

}
