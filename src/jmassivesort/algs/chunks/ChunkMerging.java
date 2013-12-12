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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Merges sorted chunks {@link Chunk} into one file.
 *
 * @author Serj Sintsov
 */
public class ChunkMerging extends AbstractAlgorithm {

   private static final int BUF_PER_CHUNK = 1*1024*1024; // 1Mb

   private final Debugger dbg = Debugger.create(getClass());

   private ChunkMergingOptions opts;

   public ChunkMerging(ChunkMergingOptions options) {
      if  (options == null)
         throw new IllegalArgumentException("options cannot be null");
      this.opts = options;

      try {
         SequentialChunkReader[] inputRDs = new SequentialChunkReader[this.opts.getNumChunks()];
         Path inPath;
         String parentPath = this.opts.getOutFilePath().getParent().toString();
         for (int i = 0; i < this.opts.getNumChunks(); i++) {
            inPath = Paths.get(parentPath, (i+1) + ".chunk");
            inputRDs[i] = new SequentialChunkReader(BUF_PER_CHUNK, inPath.toFile());
         }
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read chunk file", e);
      }
   }

   @Override
   public void apply() throws SortingAlgorithmException {

   }

}