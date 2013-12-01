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
package org.jmassivesort;

import org.jmassivesort.extsort.TwoWayMergeSortOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Serj Sintsov
 */
public class JMassiveSort {

   private static final String TWO_WAY_MERGESORT_NAME = "2way-mergesort";

   private static final Map<String, SortingAlgorithmBuilder> algorithms = new HashMap<String, SortingAlgorithmBuilder>() {{
      put(TWO_WAY_MERGESORT_NAME, TwoWayMergeSortOptions.builder());
   }};

   private static String algorithmName;
   private static String[] algorithmOptions;

   public static void main(String[] args) {
      buildOptions(args);
      go(algorithmName, algorithmOptions);
   }

   public static void go(String algorithmName, String[] algorithmOptions) {
      try {
         SortingAlgorithmBuilder alg = algorithms.get(algorithmName);
         if (alg == null) {
            printUsage("unknown algorithm");
         }

         alg.build(algorithmOptions).apply();
      }
      catch (CliOptionsBuilderException e) {
         printUsage(e);
      }
   }

   public static void buildOptions(String... options) throws CliOptionsBuilderException {
      algorithmName = "";
      algorithmOptions = new String[] {};
   }

   private static void printUsage(String error) {
      printMsg("Error: %s", error);
      printMsg("Usage: java -jar jarfile [-options]");
      printMsg("where options include:");
      printMsg("    <algorithm_name> An algorithm to run. possible values: " + TWO_WAY_MERGESORT_NAME + ".");
      printMsg("    [-OPTIONS] algorithm specific options.");
   }

   private static void printUsage(CliOptionsBuilderException e) {
      printMsg("Error: %s", e.getMessage());
      printMsg("Usage: java -jar jarfile -[-options]");
      printMsg("where options include:");

      for (String option : e.getOptionDescriptions().keySet())
         printMsg("    %s %s", option, e.getOptionDescriptions().get(option));
   }

   private static void printMsg(String msg, Object... params) {
      System.out.println(String.format(msg, params));
   }

}
