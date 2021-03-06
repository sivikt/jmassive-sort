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
package jmassivesort;

import jmassivesort.algs.SortingAlgorithmBuilder;
import jmassivesort.algs.chunks.ChunkMergingOptions;
import jmassivesort.algs.chunks.ChunkSortingOptions;
import static jmassivesort.JMassiveSortUsageFormatter.printUsage;
import jmassivesort.algs.mergesort.TwoWayMergeSortOptions;
import jmassivesort.util.Debugger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public class JMassiveSort {

   private static final Debugger dbg = Debugger.create(JMassiveSort.class);

   private static final String TWO_WAY_MERGESORT_NAME = "2way-mergesort";
   private static final String CHUNK_SORTING = "chunk-sorting";
   private static final String CHUNK_MERGING = "chunk-merging";

   private static final Map<String, SortingAlgorithmBuilder> algorithms;
   private static final Map<String, String> optionDescriptions;

   private static String algorithmName;
   private static String[] algorithmOptions;

   static {
      algorithms = new HashMap<String, SortingAlgorithmBuilder>() {{
         put(TWO_WAY_MERGESORT_NAME, TwoWayMergeSortOptions.algorithmBuilder());
         put(CHUNK_SORTING, ChunkSortingOptions.algorithmBuilder());
         put(CHUNK_MERGING, ChunkMergingOptions.algorithmBuilder());
      }};

      optionDescriptions = new HashMap<String, String>() {{
         put("<algorithm_name>", "An algorithm to run. Possible value is [" + algorithms.keySet() + "]");
         put("[-options]", "Algorithm specific options");
      }};
   }

   public static SortingAlgorithmBuilder chooseAlgorithm(String algorithmName) {
      if (!algorithms.containsKey(algorithmName)) {
         printUsage("unknown algorithm '" + algorithmName + "'", optionDescriptions);
         System.exit(1);
      }

      return algorithms.get(algorithmName);
   }

   public static void buildOptions(String[] options) throws CliOptionsBuilderException {
      if (options.length == 0) {
         printUsage("specify options", optionDescriptions);
         System.exit(1);
      }

      algorithmName = options[0];
      algorithmOptions = Arrays.copyOfRange(options, 1, options.length);
   }

   public static void main(String[] args) {
      buildOptions(args);
      SortingAlgorithmBuilder alg = chooseAlgorithm(algorithmName);

      try {
         dbg.startFunc("Start alg");
         dbg.startTimer();
         alg.build(algorithmOptions).apply();
         dbg.stopTimer();
         dbg.endFunc("Start alg");

         System.exit(0);
      }
      catch (CliOptionsBuilderException e) {
         printUsage(e.getMessage(), algorithmName, e.getOptionDescriptions());
         System.exit(1);
      }
      catch (Throwable any) { // finish with error status in any case
         any.printStackTrace();
         System.exit(1);
      }
   }

}
