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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;
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
      }};

      protected int chunkId;
      protected int numChunks;
      protected Path inPath;

      public ChunkSortingOptions build(String[] options) throws CliOptionsBuilderException {
         if (options == null || options.length != 2)
            throw new CliOptionsBuilderException(usage("Incorrect usage"), optionDescriptions);

         try {
            numChunks = Integer.parseInt(options[1]);
            if (numChunks < 1)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);

            chunkId = Integer.parseInt(options[0]);
            if (chunkId < 0 || chunkId >= numChunks)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);
            chunkId++; // just to avoid +1 increments
         }
         catch (NumberFormatException ex) {
            throw new CliOptionsBuilderException(usage("Incorrect option value"), optionDescriptions);
         }

         try {
            inPath = new Path(URI.create("hdfs:///in/input"));
         }
         catch (Exception e) {
            throw new CliOptionsBuilderException(usage("Incorrect input file path"), optionDescriptions);
         }

         String outFileName = chunkId + ".chunk";
         Path outPath = new Path("hdfs:///tmp", outFileName);

         return new ChunkSortingOptions(chunkId, numChunks, inPath, outPath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <chunkId> <numChunks>";
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
   private Path inPath;
   private Path outPath;
   private FileSystem fs;

   protected ChunkSortingOptions(int chunksId, int numChunks, Path inPath, Path outPath) {
      this.chunkId = chunksId;
      this.numChunks = numChunks;
      this.inPath = inPath;
      this.outPath = outPath;

      Configuration conf = new Configuration();
      try {
         fs = FileSystem.get(conf);
      } catch (IOException e) {
         throw new RuntimeException("hdfs error", e);
      }
   }

   public int getChunkId() {
      return chunkId;
   }

   public int getNumChunks() {
      return numChunks;
   }

   public FileSystem getFs() {
      return fs;
   }

   public org.apache.hadoop.fs.Path getInPath() {
      return inPath;
   }

   public org.apache.hadoop.fs.Path getOutPath() {
      return outPath;
   }
}
