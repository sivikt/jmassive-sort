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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Command line options to use {@link ChunkMerging} algorithm.
 * @author Serj Sintsov
 */
public class ChunkMergingOptions {

   private static final Debugger dbg = Debugger.create(ChunkMergingOptions.class);

   public static Builder builder() {
      return new Builder();
   }

   public static ChunkMergingBuilder algorithmBuilder() {
      return new ChunkMergingBuilder();
   }

   public static class Builder {
      private final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
         put("<numMerge>", "Integer value > 0. Merge number");
         put("<numChunks>", "Integer value > 0. Number of chunks it has to merge");
      }};

      protected int numChunks;
      protected Path outFilePath;
      protected Path chunksDirPath;

      public ChunkMergingOptions build(String[] options) throws CliOptionsBuilderException {
//         if (options == null || options.length != 1)
//            throw new CliOptionsBuilderException(usage("Incorrect usage"), optionDescriptions);
//
//         try {
//            numChunks = Integer.parseInt(options[0]);
//            if (numChunks < 1)
//               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);
//         }
//         catch (NumberFormatException ex) {
//            throw new CliOptionsBuilderException(usage("Incorrect option value"), optionDescriptions);
//         }

         try {
            outFilePath = new Path(URI.create("hdfs:///out/merge.out"));
            chunksDirPath = new Path(URI.create("hdfs:///tmp/"));
         }
         catch (Exception e) {
            throw new CliOptionsBuilderException(usage("Incorrect path"), optionDescriptions);
         }


         return new ChunkMergingOptions(outFilePath, chunksDirPath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <numChunks> <outputFile>";
      }
   }

   public static class ChunkMergingBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         dbg.startFunc("build options and alg instance");
         dbg.startTimer();
         dbg.markFreeMemory();
         ChunkMerging chunkMerging = new ChunkMerging(ChunkMergingOptions.builder().build(options));
         dbg.checkMemoryUsage();
         dbg.stopTimer();
         dbg.endFunc("build options and alg instance");

         return chunkMerging;
      }
   }

   private Path outFilePath;
   private Path chunksDirPath;
   private FileSystem fs;

   protected ChunkMergingOptions(Path outFilePath, Path chunksDirPath) {
      this.outFilePath = outFilePath;
      this.chunksDirPath = chunksDirPath;

      Configuration conf = new Configuration();
      try {
         fs = FileSystem.get(conf);
      } catch (IOException e) {
         throw new RuntimeException("hdfs error", e);
      }
   }

   public Path getOutPath() {
      return outFilePath;
   }

   public Path getChunksDirPath() {
      return chunksDirPath;
   }

   public FileSystem getFs() {
      return fs;
   }
}
