/**
 * Copyright 2013 Serj Sintsov <ssivikt@gmail.com></ssivikt@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jmassivesort.extsort;

import jmassivesort.SortingAlgorithmException;

import static jmassivesort.extsort.IOUtils.closeSilently;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Sorts a part of the input file.
 * todo javadoc
 * todo make it stateless
 * @author Serj Sintsov
 */
public class ChunkSorting extends AbstractAlgorithm {

   private ChunkSortingOptions options;

   public ChunkSorting(ChunkSortingOptions options) {
      if  (options == null) throw new IllegalArgumentException("options cannot be null");
      this.options = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      readChunk();
      //sort(lines);
      //saveChunk(lines);
   }

   private Chunk readChunk() {
      File srcFile = new File(options.getInputFilePath());
      InMemoryChunkReader cr = null;

      try {
         cr = new InMemoryChunkReader(options.getChunkId(), options.getNumChunks(), srcFile);
         return cr.readChunk();
      }
      catch (FileNotFoundException e) {
         throw new SortingAlgorithmException("Cannot find file '" + options.getInputFilePath() + "'", e);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read file '" + options.getInputFilePath() + "'", e);
      }
      finally {
         closeSilently(cr);
      }
   }

   /**
    * Uses quick-sort
    */
   private void sort(String[] lines) {

   }

   private void saveChunk(String[] lines) {
      File outFile = createNewFile(options.getChunkId() + ".txt");
      BufferedWriter outWr = null;

      try {
         outWr = new BufferedWriter(new FileWriter(outFile));
         for (int i = 0; i < lines.length; i++) {
            outWr.write(lines[i]);
            if (i < lines.length-1)
               outWr.newLine();
         }
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot write to file '" + outFile.getAbsolutePath() + "'", e);
      }
      finally {
         closeSilently(outWr);
      }
   }

}