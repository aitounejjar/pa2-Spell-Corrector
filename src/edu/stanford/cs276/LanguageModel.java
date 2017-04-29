package edu.stanford.cs276;

import edu.stanford.cs276.util.Dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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


    Map<String, Set<String>> w1map = new HashMap<>();
    Map<String, Set<String>> w2map = new HashMap<>();

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

                    updateBigramsMappings(previousWord, w);

                    previousWord = w;
                    termCounter++;
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
            double p = Math.log(wCount) - Math.log(t);
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
            double p = Math.log(w1w2Count) - Math.log(w1Count);

            // interpolate the result
            double pInterpolated = (LAMBDA * unigramProbabilities.get(w2)) + ((1-LAMBDA) * p);

            bigramProbabilities.put(w1w2, pInterpolated);
        }
    }

    private void updateBigramsMappings(String w1, String w2) {

        String bigram = w1+" "+w2;

        if (!w1map.containsKey(w1)) {
            w1map.put(w1, new HashSet<>());
        }
        w1map.get(w1).add(bigram);

        if (!w2map.containsKey(w2)) {
            w2map.put(w2, new HashSet<>());
        }
        w2map.get(w2).add(bigram);

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
}
