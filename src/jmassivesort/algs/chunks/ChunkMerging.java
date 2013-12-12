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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static jmassivesort.util.IOUtils.closeSilently;
import static jmassivesort.util.IOUtils.newFileOnFS;

/**
 * Merges sorted chunks {@link Chunk} into one file.
 *
 * @author Serj Sintsov
 */
public class ChunkMerging extends AbstractAlgorithm {

   private static final int BUF_PER_CHUNK = 1*1024*1024; // 1Mb

   private final Debugger dbg = Debugger.create(getClass());

   private ChunkMergingOptions opts;
   private SequentialChunkReader[] inputRDs;

   public ChunkMerging(ChunkMergingOptions options) {
      if  (options == null)
         throw new IllegalArgumentException("options cannot be null");
      this.opts = options;
   }

   private void createChunksReaders() {
      dbg.startFunc("createChunksReaders");
      dbg.markFreeMemory();
      dbg.startTimer();

      try {
         inputRDs = new SequentialChunkReader[this.opts.getNumChunks()];
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

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("createChunksReaders");
   }

   private BufferedChunkWriter createOutputWriter() {
      try {
         dbg.startFunc("createOutputWriter");
         dbg.markFreeMemory();
         dbg.startTimer();

         File outFile = newFileOnFS(opts.getOutFilePath());
         BufferedChunkWriter wr = new BufferedChunkWriter(outFile);

         dbg.stopTimer();
         dbg.checkMemoryUsage();
         dbg.endFunc("createOutputWriter");
         return wr;
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read chunk file", e);
      }
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      dbg.startFunc("apply");
      dbg.markFreeMemory();
      dbg.startTimer();

      createChunksReaders();
      BufferedChunkWriter wr = createOutputWriter();
      PriorityQueue<ChunkMarkerRef> pq = new PriorityQueue<>(opts.getNumChunks(), asc);

      try {
         for (int i = 0; i < inputRDs.length; i++) {
            Chunk ch = inputRDs[i].nextChunk();
            if (ch != null)
               pq.add(new ChunkMarkerRef(i, ch, 0));
            else
               closeSilently(inputRDs[i]);
         }

         dbg.startFunc("merging");
         dbg.markFreeMemory();
         dbg.startTimer();

         while (!pq.isEmpty()) {
            ChunkMarkerRef min = pq.poll();
            wr.write(min.chunk.rawData(), min.chunk.allMarkers().get(min.marker));

            if (min.chunk.allMarkers().size()-1 == min.marker) {
               SequentialChunkReader inRd = inputRDs[min.chunkId];
               Chunk ch = inRd.nextChunk();
               if (ch != null)
                  pq.add(new ChunkMarkerRef(min.chunkId, ch, 0));
               else
                  closeSilently(inputRDs[min.chunkId]);
            }
            else {
               min.marker++;
               pq.add(min);
            }
         }

         dbg.stopTimer();
         dbg.checkMemoryUsage();
         dbg.endFunc("merging");
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read chunk or write to output", e);
      }

      closeSilently(wr);

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("apply");
   }

   private static class ChunkMarkerRef {
      int chunkId;
      int marker;
      Chunk chunk;

      ChunkMarkerRef(int chunkId, Chunk chunk, int marker) {
         this.chunkId = chunkId;
         this.chunk = chunk;
         this.marker = marker;
      }
   }

   private static Comparator<ChunkMarkerRef> asc = new Comparator<ChunkMarkerRef>() {
      @Override
      public int compare(ChunkMarkerRef ref1, ChunkMarkerRef ref2) {
         return Chunk.compareMarkers(
               ref1.chunk.rawData(), ref1.chunk.allMarkers().get(ref1.marker),
               ref2.chunk.rawData(), ref2.chunk.allMarkers().get(ref2.marker));
      }
   };

}