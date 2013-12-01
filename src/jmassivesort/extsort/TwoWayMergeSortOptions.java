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

import jmassivesort.CliOptionsBuilderException;
import jmassivesort.SortingAlgorithm;
import jmassivesort.SortingAlgorithmBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Serj Sintsov
 */
public class TwoWayMergeSortOptions {

   public static TwoWayMergeSortBuilder builder() {
      return new TwoWayMergeSortBuilder();
   }

   public static class TwoWayMergeSortBuilder implements SortingAlgorithmBuilder {
      private static final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
         put("<taskId>", "The result of sorting is stored into file <taskId>.txt");
         put("<numTasks>", "Number of tasks");
         put("<inputFile>", "Input file to sort");
      }};

      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         throw new CliOptionsBuilderException("Options are undefined", optionDescriptions);

         //return new TwoWayMergeSort(new TwoWayMergeSortOptions());
      }
   }

   protected TwoWayMergeSortOptions() { }

}
