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
package org.jmassivesort.extsort;

import org.jmassivesort.CliOptionsBuilderException;
import org.jmassivesort.SortingAlgorithm;
import org.jmassivesort.SortingAlgorithmBuilder;

/**
 * @author Serj Sintsov
 */
public class TwoWayMergeSortOptions {

   public static TwoWayMergeSortBuilder builder() {
      return new TwoWayMergeSortBuilder();
   }

   public static class TwoWayMergeSortBuilder implements SortingAlgorithmBuilder {
      @Override
      public SortingAlgorithm build(String[] options) throws CliOptionsBuilderException {
         return null;
      }
   }

   protected TwoWayMergeSortOptions() { }

}
