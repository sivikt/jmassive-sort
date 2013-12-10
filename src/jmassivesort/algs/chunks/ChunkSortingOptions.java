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
import jmassivesort.util.Debugger;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Command line options to use {@link ChunkSorting} algorithm.
 * @author Serj Sintsov
 */
public class ChunkSortingOptions {

   private static final Debugger dbg = Debugger.create(ChunkSortingOptions.class);

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
         put("<inputFile>", "Input file path in RFC 2396 format");
      }};

      protected int chunkId;
      protected int numChunks;
      protected Path inputFilePath;

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

         try {
            inputFilePath = Paths.get(URI.create(options[2]));
         }
         catch (Exception e) {
            throw new CliOptionsBuilderException(usage("Incorrect input file path"), optionDescriptions);
         }

         String outFileName = chunkId + ".chunk";
         Path outputFilePath = Paths.get(inputFilePath.getParent().toString(), outFileName);

         return new ChunkSortingOptions(chunkId, numChunks, inputFilePath, outputFilePath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <chunkId> <numChunks> <inputFile>";
      }
   }

   public static class ChunkSortingBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         dbg.startFunc("build options and alg instance");
         dbg.startTimer();
         ChunkSorting chunkSorting = new ChunkSorting(ChunkSortingOptions.builder().build(options));
         dbg.stopTimer();
         dbg.endFunc("build options and alg instance");

         return chunkSorting;
      }
   }

   private int chunkId;
   private int numChunks;
   private Path inputFilePath;
   private Path outputFilePath;

   protected ChunkSortingOptions(int chunksId, int numChunks, Path inputFilePath, Path outputFilePath) {
      this.chunkId = chunksId;
      this.numChunks = numChunks;
      this.inputFilePath = inputFilePath;
      this.outputFilePath = outputFilePath;
   }

   public int getChunkId() {
      return chunkId;
   }

   public int getNumChunks() {
      return numChunks;
   }

   public Path getInputFilePath() {
      return inputFilePath;
   }

   public Path getOutputFilePath() {
      return outputFilePath;
   }

}
