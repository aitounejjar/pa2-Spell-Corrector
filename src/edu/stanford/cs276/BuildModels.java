package edu.stanford.cs276;

public class BuildModels {

  public static double MU = .05;
  public static LanguageModel languageModel;
  public static NoisyChannelModel noisyChannelModel;

  public static void main(String[] args) throws Exception {

    String trainingCorpus = null;
    String editsFile = null;
    String extra = null;
    if (args.length == 2 || args.length == 3) {
      trainingCorpus = args[0];
      editsFile = args[1];
      if (args.length == 3) extra = args[2];
    } 
    else {
      System.err.println(
          "Invalid arguments.  Argument count must 2 or 3 \n" 
          + "./buildmodels <training corpus dir> <training edit1s file> \n"
          + "./buildmodels <training corpus dir> <training edit1s file> <extra> \n"
          + "SAMPLE: ./buildmodels data/corpus data/edit1s.txt \n"
          + "SAMPLE: ./buildmodels data/corpus data/edit1s.txt extra \n");
      return;
    }
    System.out.println("training corpus: " + args[0]);

    languageModel = LanguageModel.create(trainingCorpus);
    noisyChannelModel = NoisyChannelModel.create(editsFile);

    // Save the models to disk
    noisyChannelModel.save();
    languageModel.save();

    if ("extra".equals(extra)) {
      /*
       * If you want to experiment with some form of extra credit in the 
       * model-building process, you can add code to this block.  You should 
       * also feel free to move this block to any other location you feel is 
       * appropriate.  The two things to verify are: 
       * 
       * 1. When you run the assignment scripts WITHOUT the 'extra' parameter, 
       * your basic implementations run correctly, and without any of your 
       * extra credit code.  
       * 
       * 2. When you run the scripts WITH the 'extra' parameter, your extra 
       * credit code runs as expected. 
       */
    }
  }
}
