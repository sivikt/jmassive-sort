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
import static jmassivesort.util.IOUtils.getFileOnFS;

import java.io.File;
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
      protected File inputFile;

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
            Path inPath = Paths.get(URI.create(options[2]));
            inputFile = getFileOnFS(inPath);
         }
         catch (Exception e) {
            throw new CliOptionsBuilderException(usage("Incorrect input file path"), optionDescriptions);
         }

         String outFileName = chunkId + ".chunk";
         Path outputFilePath = Paths.get(inputFile.getParent().toString(), outFileName);

         return new ChunkSortingOptions(chunkId, numChunks, inputFile, outputFilePath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <chunkId> <numChunks> <inputFile>";
      }
   }

   public static class ChunkSortingBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         ChunkSorting chunkSorting = new ChunkSorting(ChunkSortingOptions.builder().build(options));
         return chunkSorting;
      }
   }

   private int chunkId;
   private int numChunks;
   private File inputFile;
   private Path outputFilePath;

   protected ChunkSortingOptions(int chunksId, int numChunks, File inputFile, Path outputFile) {
      this.chunkId = chunksId;
      this.numChunks = numChunks;
      this.inputFile = inputFile;
      this.outputFilePath = outputFile;
   }

   public int getChunkId() {
      return chunkId;
   }

   public int getNumChunks() {
      return numChunks;
   }

   public File getInputFile() {
      return inputFile;
   }

   public Path getOutputFilePath() {
      return outputFilePath;
   }

}
