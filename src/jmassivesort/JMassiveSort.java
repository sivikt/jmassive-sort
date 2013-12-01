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
package jmassivesort;

import jmassivesort.extsort.TwoWayMergeSortOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Serj Sintsov
 */
public class JMassiveSort {

   private static final String TWO_WAY_MERGESORT_NAME = "2way-mergesort";

   private static final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
      put("<algorithm_name>", "An algorithm to run. Possible value is " + TWO_WAY_MERGESORT_NAME);
      put("[-options]", "Algorithm specific options");
   }};

   private static final Map<String, SortingAlgorithmBuilder> algorithms = new HashMap<String, SortingAlgorithmBuilder>() {{
      put(TWO_WAY_MERGESORT_NAME, TwoWayMergeSortOptions.algorithmBuilder());
   }};

   private static String algorithmName;
   private static String[] algorithmOptions;

   public static void main(String[] args) {
      buildOptions(args);
      SortingAlgorithmBuilder alg = chooseAlgorithm(algorithmName);

      try {
         alg.build(algorithmOptions).apply();
      }
      catch (CliOptionsBuilderException e) {
         HelpFormatter.printUsage(e.getMessage(), algorithmName, e.getOptionDescriptions());
         System.exit(1);
      }
   }

   public static SortingAlgorithmBuilder chooseAlgorithm(String algorithmName) {
      if (!algorithms.containsKey(algorithmName)) {
         HelpFormatter.printUsage("unknown algorithm '" + algorithmName + "'", optionDescriptions);
         System.exit(1);
      }

      return algorithms.get(algorithmName);
   }

   public static void buildOptions(String[] options) throws CliOptionsBuilderException {
      if (options.length == 0) {
         HelpFormatter.printUsage("specify options", optionDescriptions);
         System.exit(1);
      }

      algorithmName = options[0];
      algorithmOptions = Arrays.copyOfRange(options, 1, options.length);
   }

}
