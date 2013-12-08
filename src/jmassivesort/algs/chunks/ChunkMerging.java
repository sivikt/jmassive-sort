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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Merges sorted chunks {@link Chunk} into one file.
 *
 * @author Serj Sintsov
 */
public class ChunkMerging extends AbstractAlgorithm {

   private final Debugger dbg = Debugger.create(getClass());

   private ChunkMergingOptions opts;

   public ChunkMerging(ChunkMergingOptions options) {
      if  (options == null)
         throw new IllegalArgumentException("options cannot be null");
      this.opts = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      File outFile = createNewFile(opts.getOutFilePath());
      FileInputStream[] chunks = new FileInputStream[opts.getNumChunks()];
      for (int i = 1; i <= opts.getNumChunks(); i++) {
         try {
            chunks[i] = new FileInputStream(i + ".txt");
         }
         catch (FileNotFoundException e) {
            throw new SortingAlgorithmException("Cannot find chunk number " + i, e);
         }
      }


   }

}