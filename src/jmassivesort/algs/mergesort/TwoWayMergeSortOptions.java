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
package jmassivesort.algs.mergesort;

import jmassivesort.CliOptionsBuilderException;
import jmassivesort.algs.SortingAlgorithm;
import jmassivesort.algs.SortingAlgorithmBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public class TwoWayMergeSortOptions {

   private int outputId;
   private String inputFilePath;

   public static Builder builder() {
      return new Builder();
   }

   public static TwoWayMergeSortBuilder algorithmBuilder() {
      return new TwoWayMergeSortBuilder();
   }

   public static class Builder {
      private final Map<String, String> optionDescriptions = new HashMap<String, String>() {{
         put("<outputId>", "Integer value. The result of sorting is stored into file <outputId>.txt");
         put("<inputFile>", "Input file to sort");
      }};

      protected int outputId;
      protected String inputFilePath;

      public TwoWayMergeSortOptions build(String[] options) throws CliOptionsBuilderException {
         if (options == null || options.length != 2)
            throw new CliOptionsBuilderException(usage("Incorrect usage"), optionDescriptions);

         try {
            outputId = Integer.parseInt(options[0]);
            if (outputId < 0)
               throw new CliOptionsBuilderException(usage("Incorrect option format"), optionDescriptions);
         }
         catch (NumberFormatException ex) {
            throw new CliOptionsBuilderException(usage("Incorrect option value"), optionDescriptions);
         }

         inputFilePath = options[1];
         File in = new File(inputFilePath);
         if (!in.exists() || !in.isFile())
            throw new CliOptionsBuilderException(usage("No such file"), optionDescriptions);

         return new TwoWayMergeSortOptions(outputId, inputFilePath);
      }

      private String usage(String error) {
         return error + ". Specify options in order <outputId> <inputFile>";
      }
   }

   public static class TwoWayMergeSortBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         return new TwoWayMergeSort(TwoWayMergeSortOptions.builder().build(options));
      }
   }

   protected TwoWayMergeSortOptions(int outputId, String inputFilePath) {
      this.outputId = outputId;
      this.inputFilePath = inputFilePath;
   }

   public int getOutputId() {
      return outputId;
   }

   public String getInputFilePath() {
      return inputFilePath;
   }

}
