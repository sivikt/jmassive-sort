/**
 * Copyright 2013 Serj Sintsov <ssivikt@gmail.com>
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
package jmassivesort.algs.chunks;

import jmassivesort.CliOptionsBuilderException;
import jmassivesort.algs.SortingAlgorithm;
import jmassivesort.algs.SortingAlgorithmBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Command line options to use {@link ChunkSorting} algorithm.
 * @author Serj Sintsov
 */
public class ChunkSortingOptions {

   private int chunkId;
   private int numChunks;
   private String inputFilePath;

   public static Builder builder() {
      return new Builder();
   }

   public static ChunkSortingBuilder algorithmBuilder() {
      return new ChunkSortingBuilder();
   }

   public static class Builder {
      private final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
         put("<chunkId>", "0 < Integer value <= <numChunks>. The result of sorting is stored into file <chunkId>.txt");
         put("<numChunks>", "Integer value > 0. Together with the <chunkId> used to determine what part of file to sort");
         put("<inputFile>", "Input file");
      }};

      protected int chunkId;
      protected int numChunks;
      protected String inputFilePath;

      public ChunkSortingOptions build(String[] options) throws CliOptionsBuilderException {
         if (options == null || options.length != 3)
            throw new CliOptionsBuilderException(usage("Incorrect usage"), optionDescriptions);

         try {
            numChunks = Integer.parseInt(options[1]);
            if (numChunks < 1)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);

            chunkId = Integer.parseInt(options[0]);
            if (chunkId < 1)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);
         }
         catch (NumberFormatException ex) {
            throw new CliOptionsBuilderException(usage("Incorrect option value"), optionDescriptions);
         }

         inputFilePath = options[2];
         File in = new File(inputFilePath);
         if (!in.exists() || !in.isFile())
            throw new CliOptionsBuilderException(usage("No such file"), optionDescriptions);

         return new ChunkSortingOptions(chunkId, numChunks, inputFilePath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <chunkId> <numChunks> <inputFile>";
      }
   }

   public static class ChunkSortingBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         return new ChunkSorting(ChunkSortingOptions.builder().build(options));
      }
   }

   protected ChunkSortingOptions(int chunksId, int numChunks, String inputFilePath) {
      this.chunkId = chunksId;
      this.numChunks = numChunks;
      this.inputFilePath = inputFilePath;
   }

   public int getChunkId() {
      return chunkId;
   }

   public int getNumChunks() {
      return numChunks;
   }

   public String getInputFilePath() {
      return inputFilePath;
   }

}
