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

import jmassivesort.algs.AbstractAlgorithm;
import jmassivesort.algs.SortingAlgorithmException;
import jmassivesort.util.Debugger;

/**
 * Merges sorted chunks {@link Chunk} into one file.
 *
 * @author Serj Sintsov
 */
public class ChunkMerging extends AbstractAlgorithm {

   private final Debugger dbg = Debugger.create(getClass());

   private ChunkMergingOptions options;

   public ChunkMerging(ChunkMergingOptions options) {
      if  (options == null) throw new IllegalArgumentException("options cannot be null");
      this.options = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {

   }

}