package edu.stanford.cs276;

import edu.stanford.cs276.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class RunCorrector {

    public static LanguageModel languageModel;
    public static NoisyChannelModel nsm;

    private static final double LAMBDA = 0.95;

    public static void main(String[] args) throws Exception {

        // Parse input arguments
        String uniformOrEmpirical = null;
        String queryFilePath = null;
        String goldFilePath = null;
        String extra = null;
        BufferedReader goldFileReader = null;

        if (args.length == 2) {
            // Default: run without extra credit code or gold data comparison
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
        } else if (args.length == 3) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            if (args[2].equals("extra")) {
                extra = args[2];
            } else {
                goldFilePath = args[2];
            }
        } else if (args.length == 4) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            extra = args[2];
            goldFilePath = args[3];
        } else {
            System.err.println(
                    "Invalid arguments.  Argument count must be 2, 3 or 4 \n"
                            + "./runcorrector <uniform | empirical> <query file> \n"
                            + "./runcorrector <uniform | empirical> <query file> <gold file> \n"
                            + "./runcorrector <uniform | empirical> <query file> <extra> \n"
                            + "./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n"
                            + "SAMPLE: ./runcorrector empirical data/queries.txt \n"
                            + "SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n"
                            + "SAMPLE: ./runcorrector empirical data/queries.txt extra \n"
                            + "SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
            return;
        }

        if (goldFilePath != null) {
            goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
        }

        // Load models from disk
        languageModel = LanguageModel.load();
        nsm = NoisyChannelModel.load();
        BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
        nsm.setProbabilityType(uniformOrEmpirical);

        String query = null;

    /*quade
     * Each line in the file represents one query. We loop over each query and find
     * the most likely correction
     */
        while ((query = queriesFileReader.readLine()) != null) {
            String correctedQuery = query;
            /*
               * Your code here: currently the correctQuery and original query are the same
               * Complete this implementation so that the spell corrector corrects the
               * (possibly) misspelled query
               */

            Set<String> candidates = CandidateGenerator.get().getCandidates(languageModel, query);

            // score candidates using the language model and the noisy channel model
            // score = P(Q|R) = P(R|Q) x P(Q) = (noisy channel probability) x (language model probability)

            List<Pair<String, Double>> scores = new ArrayList<>();

            for (String candidateQuery : candidates) {

                // compute the edit probability
                double editProbability = nsm.ecm_.editProbability(query, candidateQuery, 0);

                // compute the language model probability
                String[] qArr = query.split("\\s+");
                String[] cArr = candidateQuery.split("\\s+");
                int cumulativeEditDistance = getEditDistance(qArr[0], cArr[0]);
                double pW1 = languageModel.unigramProbabilities.get(cArr[0]);
                double lmProbability = pW1;
                for (int i=1; i<cArr.length; ++i) {

                    String w1 = cArr[i-1];
                    String w2 = cArr[i];
                    String bigram = w1 + " " + w2;

                    // we use linear interpolation to bypass the fact that a bigram might not have occurred in the
                    // corpus, and thus its probability will be zero
                    Double biProb = languageModel.bigramProbabilities.get(bigram);
                    double pBigram = (
                            (LAMBDA * ((biProb == null) ? 0 : biProb) )
                            +
                            (1-LAMBDA)*languageModel.unigramProbabilities.get(w1)
                    );

                    lmProbability += pBigram;

                    // update the edit distance
                    cumulativeEditDistance += getEditDistance(qArr[i], cArr[i]);
                }

                //double score = Math.log(editProbability) + Math.log(lmProbability);
                double score = (editProbability) + (lmProbability);

                // update the scores
                scores.add(new Pair(candidateQuery, score));

            }

            Collections.sort(scores, (p1, p2) -> p1.getSecond().compareTo(p2.getSecond()));
            Collections.reverse(scores);

            if ("extra".equals(extra)) {
            /*
             * If you are going to implement something regarding to running the corrector,
             * you can add code here. Feel free to move this code block to wherever
             * you think is appropriate. But make sure if you add "extra" parameter,
             * it will run code for your extra credit and it will run you basic
             * implementations without the "extra" parameter.
             */
            }

            // If a gold file was provided, compare our correction to the gold correction
            // and output the running accuracy
            if (goldFileReader != null) {
                String goldQuery = goldFileReader.readLine();

            /*
             * You can do any bookkeeping you wish here - track accuracy, track where your solution
             * diverges from the gold file, what type of errors are more common etc. This might
             * help you improve your candidate generation/scoring steps
             */
            }
      
          /*
           * Output the corrected query.
           * IMPORTANT: In your final submission DO NOT add any additional print statements as
           * this will interfere with the autograder
           */
            //System.out.println(correctedQuery);
            System.out.println("~ " + scores.get(0));
        }
        queriesFileReader.close();
    }

    /**
     * @param w1 a valid vocabulary term
     * @param w2 a valid vocabulary term
     * @return   the edit distance from w1 to w2, that was previously calculated by the candidates generator
     */
    private static int getEditDistance(String w1, String w2) {
        int d = -1;
        try {
            d = CandidateGenerator.get().editDistances.get(w1).get(w2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return d;
    }
}
