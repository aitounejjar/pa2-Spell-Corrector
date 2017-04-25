package edu.stanford.cs276;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * NoisyChannelModel class constructs a channel model (which is a model of errors that
 * occur in our dataset of queries - the probability of a character getting inserted into a
 * query, deleted from a query, substituted by another character or transposed with a neighboring
 * character in the query).
 * 
 * This class uses the Singleton design pattern
 * (https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class NoisyChannelModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
  private static NoisyChannelModel ncm_ = null;
  EditCostModel ecm_ = null;

  EmpiricalCostModel empiricalCostModel = null;
  UniformCostModel uniformCostModel = null;

  /*
   * Feel free to add more members here.
   * Your code here ...
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
   *
   * For more info about the Singleton pattern, see https://en.wikipedia.org/wiki/Singleton_pattern.  
   */
  private NoisyChannelModel(String editsFile) throws Exception {
    empiricalCostModel = new EmpiricalCostModel(editsFile);
    uniformCostModel = new UniformCostModel();
  }

  /**
   * Creates a new NoisyChannelModel object from the query corpus. This method should be used to
   * create a new object rather than calling the constructor directly from outside this class
   */
  public static NoisyChannelModel create(String editsFile) throws Exception {
    if (ncm_ == null) {
      ncm_ = new NoisyChannelModel(editsFile);
    }
    return ncm_;
  }

  /**
   * Loads the model object (and all associated data) from disk
   */
  public static NoisyChannelModel load() throws Exception {
    try {
      // Don't load from disk if it's already been loaded.
      if (ncm_ == null) {
        FileInputStream fiA = new FileInputStream(Config.noisyChannelFile);
        ObjectInputStream oisA = new ObjectInputStream(fiA);
        ncm_ = (NoisyChannelModel) oisA.readObject();
        oisA.close();
      }
    } catch (Exception e) {
      throw new Exception("Unable to load noisy channel model.  You may not have run buildmodels.sh!");
    }
    return ncm_;
  }

  /**
   * Saves the object (and all associated data, e.g. EditCostModel) to disk
   */
  public void save() throws Exception {
    FileOutputStream saveFile = new FileOutputStream(Config.noisyChannelFile);
    ObjectOutputStream save = new ObjectOutputStream(saveFile);
    save.writeObject(this);
    save.close();
  }

  /**
   * Set the EditCostModel to be used
   */
  public void setProbabilityType(String type) throws Exception {
    if (type.equals("empirical")) {
      ecm_ = this.empiricalCostModel;
    } else if (type.equals("uniform")) {
      ecm_ = this.uniformCostModel;
    } else {
      throw new Exception("Invalid noisy channel probability type: "
          + "must be one of <uniform | empirical>");
    }
  }
}
