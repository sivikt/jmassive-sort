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
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import java.io.*;
import java.util.*;

import static jmassivesort.util.IOUtils.closeSilently;

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
         List<Path> chPaths = listChunks();
         inputRDs = new SequentialChunkReader[chPaths.size()];
         for (int i = 0; i < chPaths.size(); i++) {
            inputRDs[i] = new SequentialChunkReader(BUF_PER_CHUNK, opts.getFs(), chPaths.get(i));
         }
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read chunk file", e);
      }

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("createChunksReaders");
   }

   private List<Path> listChunks() throws IOException {
      List<Path> paths = new ArrayList<>();
      RemoteIterator<LocatedFileStatus> it = opts.getFs().listFiles(this.opts.getChunksDirPath(), false);

      while (it.hasNext())
         paths.add(it.next().getPath());

      return paths;
   }

   private BufferedChunkWriter createOutputWriter() {
      try {
         dbg.startFunc("createOutputWriter");
         dbg.markFreeMemory();
         dbg.startTimer();

         BufferedChunkWriter wr = new BufferedChunkWriter(opts.getFs(), opts.getOutPath());

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
      PriorityQueue<ChunkMarkerRef> pq = new PriorityQueue<>(inputRDs.length, asc);

      try {
         prefillHeapWithFirstMarkers(pq);
         mergeMarkers(pq, wr);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Cannot read chunk or write to output", e);
      }

      closeSilently(wr);

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("apply");
   }

   private void prefillHeapWithFirstMarkers(PriorityQueue<ChunkMarkerRef> pq) throws IOException {
      dbg.startFunc("prefillHeapWithFirstMarkers");
      dbg.markFreeMemory();
      dbg.startTimer();

      for (int i = 0; i < inputRDs.length; i++) {
         Chunk ch = inputRDs[i].nextChunk();
         if (ch != null)
            pq.add(new ChunkMarkerRef(i, ch, 0));
         else
            closeSilently(inputRDs[i]);
      }

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("prefillHeapWithFirstMarkers");
   }

   private void mergeMarkers(PriorityQueue<ChunkMarkerRef> pq, BufferedChunkWriter wr) throws IOException {
      dbg.startFunc("mergeMarkers");
      dbg.markFreeMemory();
      dbg.startTimer();

      while (!pq.isEmpty()) {
         ChunkMarkerRef min = pq.poll();
         wr.write(min.chunk.rawData(), min.chunk.allMarkers().get(min.marker));

         if (min.chunk.allMarkers().size()-1 == min.marker) {
            Chunk ch = inputRDs[min.chunkId].nextChunk();
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
      dbg.endFunc("mergeMarkers");
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