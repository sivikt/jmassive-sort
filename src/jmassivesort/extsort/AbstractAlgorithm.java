package jmassivesort.extsort;

import jmassivesort.SortingAlgorithm;
import jmassivesort.SortingAlgorithmException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public abstract class AbstractAlgorithm implements SortingAlgorithm {

   protected void closeSilently(Closeable target) {
      try {
         if (target != null) target.close();
      }
      catch (IOException e) { /** nothing to do */ }
   }

   protected File createNewFile(String path) {
      File newFile = new File(path);
      try {
         if (newFile.createNewFile()) return newFile;
         else throw new SortingAlgorithmException("File '" + path + "' already exists");
      } catch (IOException e) {
         throw new SortingAlgorithmException("Creation of file '" + path + "' is failed due to the error", e);
      }
   }

   protected void debug(String msg, Object... params) {
      System.out.println(">>> debug: " + String.format(msg, params));
   }

}
