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

import java.util.HashMap;
import java.util.Map;

/**
 * Command line options to use {@link ChunkMerging} algorithm.
 * @author Serj Sintsov
 */
public class ChunkMergingOptions {

   private int numChunks;
   private String outFilePath;

   public static Builder builder() {
      return new Builder();
   }

   public static ChunkMergingBuilder algorithmBuilder() {
      return new ChunkMergingBuilder();
   }

   public static class Builder {
      private final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
         put("<numChunks>", "Integer value > 0. The number of chunks it has to merge");
         put("<outputFile>", "Output sorted file");
      }};

      protected int numChunks;
      protected String outFilePath;

      public ChunkMergingOptions build(String[] options) throws CliOptionsBuilderException {
         if (options == null || options.length != 2)
            throw new CliOptionsBuilderException(usage("Incorrect usage"), optionDescriptions);

         try {
            numChunks = Integer.parseInt(options[0]);
            if (numChunks < 1)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);
         }
         catch (NumberFormatException ex) {
            throw new CliOptionsBuilderException(usage("Incorrect option value"), optionDescriptions);
         }

         outFilePath = options[1];
         return new ChunkMergingOptions(numChunks, outFilePath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <numChunks> <outputFile>";
      }
   }

   public static class ChunkMergingBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         return new ChunkMerging(ChunkMergingOptions.builder().build(options));
      }
   }

   protected ChunkMergingOptions(int numChunks, String outFilePath) {
      this.numChunks = numChunks;
      this.outFilePath = outFilePath;
   }

   public int getNumChunks() {
      return numChunks;
   }

   public String getOutFilePath() {
      return outFilePath;
   }

}
