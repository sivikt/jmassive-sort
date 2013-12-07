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
import jmassivesort.util.Debugger;

import static jmassivesort.util.IOUtils.closeSilently;

import java.io.*;

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
      Chunk.ChunkLine[] lines = sort(ch);
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

   private void saveChunk(Chunk ch, Chunk.ChunkLine[] lines) {
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

   private Chunk.ChunkLine[] sort(Chunk ch) {
      Chunk.ChunkLine[] lines = ch.getLines();
      quicksort(ch, lines, 0, lines.length - 1);
      return lines;
   }

   private void quicksort(Chunk ch, Chunk.ChunkLine[] a, int low, int high) {
      int i = low, j = high;
      Chunk.ChunkLine pivot = a[low + (high-low)/2];

      while (i <= j) {
         while (compare(ch.getContent(), a[i], pivot) < 0) i++;
         while (compare(ch.getContent(), a[j], pivot) > 0) j--;

         if (i <= j) {
            exchange(a, i, j);
            i++;
            j--;
         }
      }

      if (low < j)
         quicksort(ch, a, low, j);
      if (i < high)
         quicksort(ch, a, i, high);
   }

   private void exchange(Chunk.ChunkLine[] lines, int i, int j) {
      Chunk.ChunkLine temp = lines[i];
      lines[i] = lines[j];
      lines[j] = temp;
   }

   private int compare(byte[] c, Chunk.ChunkLine ln1, Chunk.ChunkLine ln2) {
      int i = ln1.off;
      int j = ln2.off;

      while ((i < ln1.off +ln1.len) && (j < ln2.off +ln2.len)) {
         if (c[i] != c[j])
            return c[i] - c[j];
         else {
            i++; j++;
         }
      }

      return ln1.len - ln2.len;
   }

}