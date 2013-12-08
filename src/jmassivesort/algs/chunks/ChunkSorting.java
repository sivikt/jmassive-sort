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

import jmassivesort.algs.SortingAlgorithmException;
import jmassivesort.algs.AbstractAlgorithm;
import static jmassivesort.algs.chunks.OrderFunctions.asc;
import jmassivesort.util.Debugger;

import static jmassivesort.util.IOUtils.closeSilently;

import java.io.*;
import java.util.Collections;

/**
 * Sorts a specified part of the input file.
 * <p/>
 * Reads the file's chunk {@link Chunk}, sorts its content
 * line by line and stores the result on to the disk.
 *
 * @author Serj Sintsov
 */
public class ChunkSorting extends AbstractAlgorithm {

   private final Debugger dbg = Debugger.create(getClass());

   private ChunkSortingOptions opts;

   public ChunkSorting(ChunkSortingOptions options) {
      if  (options == null)
         throw new IllegalArgumentException("options cannot be null");
      this.opts = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      dbg.startFunc("apply");
      dbg.startTimer();
      dbg.markFreeMemory();

      Chunk ch = readChunk();
      sort(ch);
      saveChunk(ch);

      dbg.checkMemoryUsage();
      dbg.stopTimer();
      dbg.endFunc("apply");
   }

   private Chunk readChunk() {
      File srcFile = new File(opts.getInputFilePath());
      InMemoryChunkReader cr = null;

      try {
         dbg.startFunc("read chunk");
         dbg.markFreeMemory();

         cr = new InMemoryChunkReader(opts.getChunkId(), opts.getNumChunks(), srcFile);
         Chunk chunk = cr.readChunk();

         dbg.checkMemoryUsage();
         dbg.endFunc("read chunk");

         return chunk;
      }
      catch (FileNotFoundException e) {
         throw new SortingAlgorithmException("Cannot find file '" + opts.getInputFilePath() + "'", e);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read file '" + opts.getInputFilePath() + "'", e);
      }
      finally {
         closeSilently(cr);
      }
   }

   private void sort(Chunk ch) {
      dbg.startFunc("sort");
      dbg.markFreeMemory();
      dbg.startTimer();

      Collections.sort(ch.allMarkers(), asc(ch));

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("sort");
   }

   private void saveChunk(Chunk ch) {
      File outFile = createNewFile(opts.getChunkId() + ".txt");
      BufferedChunkWriter chWr = null;

      try {
         dbg.startFunc("write to disk");
         dbg.markFreeMemory();
         dbg.startTimer();

         chWr = new BufferedChunkWriter(outFile);
         chWr.write(ch.rawData(), ch.allMarkers());

         dbg.stopTimer();
         dbg.checkMemoryUsage();
         dbg.endFunc("write to disk");
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot write to file '" + outFile.getAbsolutePath() + "'", e);
      }
      finally {
         closeSilently(chWr);
      }
   }

}