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
import static jmassivesort.algs.chunks.ChunkOrdering.asc;
import jmassivesort.util.Debugger;

import static jmassivesort.util.IOUtils.closeSilently;

import java.io.*;
import java.util.Collections;
import java.util.List;

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

   private ChunkSortingOptions options;

   public ChunkSorting(ChunkSortingOptions options) {
      if  (options == null) throw new IllegalArgumentException("options cannot be null");
      this.options = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      Chunk ch = readChunk();

      dbg.startFunc("sort");
      dbg.startTimer();
      List<Chunk.ChunkLine> lines = sort(ch);
      dbg.stopTimer();
      dbg.endFunc("sort");

      dbg.startFunc("write to disk");
      dbg.startTimer();
      saveChunk(ch, lines);
      dbg.stopTimer();
      dbg.endFunc("write to disk");
   }

   private Chunk readChunk() {
      File srcFile = new File(options.getInputFilePath());
      InMemoryChunkReader cr = null;

      try {
         cr = new InMemoryChunkReader(options.getChunkId(), options.getNumChunks(), srcFile);
         return cr.readChunk();
      }
      catch (FileNotFoundException e) {
         throw new SortingAlgorithmException("Cannot find file '" + options.getInputFilePath() + "'", e);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read file '" + options.getInputFilePath() + "'", e);
      }
      finally {
         closeSilently(cr);
      }
   }

   private List<Chunk.ChunkLine> sort(Chunk ch) {
      List<Chunk.ChunkLine> lines = ch.getLinesList();
      Collections.sort(lines, asc(ch));
      return lines;
   }

   private void saveChunk(Chunk ch, List<Chunk.ChunkLine> lines) {
      File outFile = createNewFile(options.getChunkId() + ".txt");
      BufferedChunkWriter chWr = null;

      try {
         chWr = new BufferedChunkWriter(outFile);
         chWr.write(ch.getContent(), lines);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot write to file '" + outFile.getAbsolutePath() + "'", e);
      }
      finally {
         closeSilently(chWr);
      }
   }

}