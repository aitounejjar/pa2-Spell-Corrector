package edu.stanford.cs276;

import edu.stanford.cs276.util.Assert;
import edu.stanford.cs276.util.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Implement {@link EditCostModel} interface. Use the query corpus to learn a model
 * of errors that occur in our dataset of queries, and use this to compute P(R|Q).
 */
public class EmpiricalCostModel implements EditCostModel {
    
    private static final String PIPE = "|";
    private static final long serialVersionUID = 1L;

    // stores the number of occurrences of a given change.
    // E.g.: If 'e' was typed instead 'i' 123 times, then this map will contain the key "e|i" mapped to "123"
    Map<String, Integer> errorCounts = new HashMap<>();

    // stores counts for unigrams and bigrams
    Map<String, Integer> kgramCounts = new HashMap<>();

    // stores empirical probabilities that are discovered in "edits1.txt"
    // E.g.: if P(e|i) = probability of typing 'e' instead of 'i' was equal to 0.1234
    //       then this map will contain the key "e|i" mapped to the value "0.1234"
    // Map<String, Double> empiricalProbabilities = new HashMap<>();

    // You need to add code for this interface method to calculate the proper empirical cost.
    @Override
    public double editProbability(String original, String R, int distance) {

        /*
         * TODO: Your code here
         */

        // determine what has changed from the original string to the new one
        String change = determineChange(original, R);

        double p = 0;
        

        return 0.5;
    }

    public EmpiricalCostModel(String editsFile) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(editsFile));
        System.out.println("Constructing edit distance map...");
        String line = null;
        while ((line = input.readLine()) != null) {
            Scanner lineSc = new Scanner(line);
            lineSc.useDelimiter("\t");
            String noisy = lineSc.next();
            String clean = lineSc.next();
          /*
           * TODO: Your code here
           */

          String change = determineChange(noisy, clean);

            char[] cleanChars = clean.toCharArray();

            updateKGramCounts(String.valueOf(cleanChars[0]));

            for (int i=1; i<cleanChars.length; ++i) {
                String unigram1 = String.valueOf(cleanChars[i-1]);
                String unigram2 = String.valueOf(cleanChars[i]);

                // update counter
                updateKGramCounts(unigram1);
                updateKGramCounts(unigram1 + unigram2);
            }

        }

        input.close();
        System.out.println("Done.");

        Logger.print(true, "size of k-grams " + kgramCounts.size());
    }

    private double  getEmpiricalProbability(String w1w2) {

        Assert.check(w1w2.contains(PIPE), "Pipe character not found - Incorrect format");

        String[] pair = w1w2.split(PIPE);

        Assert.check(pair.length == 2, "Incorrect format found - expected: noisyChars|cleanChar, but got: " + w1w2);

        double w1w2Count = errorCounts.get(w1w2);

        double w1Count = kgramCounts.get(pair[1]);

        double p = (w1w2Count + 1) / (w1Count + kgramCounts.size());

        return p;

    }
    
    // Update empirical term and increase count
    private void storeEmpiricalCount(String EmpericalNoise){
        int count = 0;
        if (errorCounts.containsKey(EmpericalNoise)) {
            count = errorCounts.get(EmpericalNoise);
        }
        errorCounts.put(EmpericalNoise, count+1);
    }

    // Update bigram term and increase count
    private void updateKGramCounts(String bi_gram){
        int count = 0;
        if (kgramCounts.containsKey(bi_gram)) {
            count = kgramCounts.get(bi_gram);
        }
        kgramCounts.put(bi_gram, count+1);
    }

    private String determineChange(String from, String to) {
        char[] cnoisy = from.toCharArray();
        char[] cclean = to.toCharArray();

        int noise_MAX = cnoisy.length;
        int clean_MAX = cclean.length;
        int i=0;

        String EmpericalNoise = null;

        while (i <= noise_MAX || i <= clean_MAX) {

            if (i<clean_MAX) {
                //Store all character unigrams in hash to use as denominator
                String uni_gram = "" + cclean[i];
                updateKGramCounts(uni_gram);

                if (i > 0) {
                    //Store all character bigrams in hash to use as denominator

                    String bi_gram = "" + cclean[i - 1] + cclean[i];
                    updateKGramCounts(bi_gram);

                }
            }

            if (clean_MAX > noise_MAX) {
                //Check deletions
                if (i==noise_MAX) {  // Last character is deleted
                    EmpericalNoise = "" + cnoisy[i - 1] + PIPE + cclean[i-1] + cclean[i];
                    //Logger.print(true, "1 deletion chars: " + EmpericalNoise);
                    break;
                }
                if(cclean[i] != cnoisy[i]) {// First non match
                    EmpericalNoise = "" + cnoisy[i - 1] + PIPE + cclean[i-1] + cclean[i];
                    //Logger.print(true, "2 deletion chars: " + EmpericalNoise);
                    break;
                }

            }else if (clean_MAX < noise_MAX) {
                // Check insertions
                if (i==clean_MAX) {  //Last character is added to noise
                    EmpericalNoise = "" + cnoisy[i - 1] + cnoisy[i] +PIPE + cclean[i-1];
                    //Logger.print(true, "1 insertions chars: " + EmpericalNoise);
                    break;
                }

                if(cclean[i] != cnoisy[i]) {// First non match
                    EmpericalNoise = "" + cnoisy[i - 1] + cnoisy[i] +PIPE + cclean[i-1];
                    //Logger.print(true, "2 insertions chars: " + EmpericalNoise);
                    break;
                }

            }else { // Length matches.  Check Replace, Transposition, Correct
                if((cclean[i] != cnoisy[i]) && (i !=noise_MAX-1)){ // Not last character.  Check future for transpose
                    if (cclean[i+1] != cnoisy[i+1]) { // Transposition
                        EmpericalNoise = "" + cnoisy[i] + cclean[i] +PIPE + cclean[i] + cnoisy[i];
                        //Logger.print(true, "1 Transposition chars: " + EmpericalNoise );
                        break;
                    }else {// Replace
                        EmpericalNoise = "" + cnoisy[i] + PIPE + cclean[i];
                        //Logger.print(true, "1 Replace chars: " +  EmpericalNoise );
                        break;
                    }
                }else if ( (cclean[i] != cnoisy[i]) && (i == noise_MAX-1))  { //Last character.  Must be replace
                    EmpericalNoise = "" + cnoisy[i] + PIPE + cclean[i];
                    break;
                }else if ((cclean[i] == cnoisy[i]) && (i == noise_MAX-1)){ // No errors
                    //Logger.print(true, "No Errors: ");
                    break;
                }
            }
            i++;
        }// while loop

        Assert.check(EmpericalNoise.contains(PIPE), "Pipe character not found - Incorrect format for the change between '" + from + "' and '" + to +"'");

        return EmpericalNoise;
    }
}