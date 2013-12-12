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

import jmassivesort.util.Debugger;

import static jmassivesort.util.IOUtils.closeSilently;
import java.io.*;
import jmassivesort.algs.chunks.Chunk.ChunkMarker;

/**
 * Reads a part of the file's content called {@link Chunk}.
 * <p/>
 * To minimize disk reads this implementation reads the whole
 * file's part into the memory and then separates it into lines
 * (finds lines offsets and etc.).
 *
 * @author Serj Sintsov
 */
public class OneOffChunkReader implements Closeable {

   private final Debugger dbg = Debugger.create(getClass());

   private static final int CHUNK_OVERHEAD_SIZE = 1 * 1024 * 1024; // 1Mb
   private static final int MAX_CHUNK_SIZE      = Integer.MAX_VALUE - CHUNK_OVERHEAD_SIZE - 1;

   private final long chunkSz;

   private byte[] buffer;
   private int bufferSz;
   private int nextByte;

   private InputStream in;

   public OneOffChunkReader(int chunkId, int numChunks, File src) throws IOException {
      if (!src.exists() || !src.isFile())
         throw new FileNotFoundException("No such file '" + src.getAbsolutePath() + "'");

      // keep chunk size equals to the closest integer number which
      // is bigger then the decimal chunk size number (e.g. 1.2 -> 2)
      chunkSz  = (long) Math.ceil((double) src.length() / numChunks);
      if (chunkSz == MAX_CHUNK_SIZE)
         throw new IllegalArgumentException("Chunk size too large. Max value is " + MAX_CHUNK_SIZE + " byte");

      long off = chunkId * chunkSz - chunkSz;
      if (off < src.length()) {
         long chunkOff = calcOffset(off, chunkSz, src);
         in = createInputStream(src, chunkOff);
      }
      else // too many chunks
         in = null;
   }

   private void fill(int len) throws IOException {
      buffer = new byte[len+1]; // +1 for EOF mark
      int n = in.read(buffer, 0, len);
      bufferSz = n > 0 ? n : 0;
      buffer[bufferSz] = -1;
      nextByte = 0;
   }

   public Chunk readChunk() throws IOException {
      dbg.startFunc("readChunk");
      dbg.markFreeMemory();
      dbg.startTimer();

      if (in == null) // just return empty chunk
         return new Chunk();

      dbg.startFunc("fill buffer");
      dbg.markFreeMemory();
      dbg.startTimer();
      fill((int)chunkSz + CHUNK_OVERHEAD_SIZE + 1);
      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("fill buffer");

      Chunk chunk = new Chunk();
      chunk.setRawData(buffer);

      dbg.startFunc("read chunk lines");
      dbg.markFreeMemory();
      dbg.startTimer();

      for (;;)
         if (readLine(chunk) == null) break;

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.endFunc("read chunk lines");

      dbg.stopTimer();
      dbg.checkMemoryUsage();
      dbg.echo("total number of lines " + chunk.rawData().length + ". total data size " + nextByte + " bytes, " + nextByte/1024/1024 + " Mb");
      dbg.endFunc("readChunk");

      return chunk;
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   private InputStream createInputStream(File f, long offset) throws IOException {
      InputStream in = new FileInputStream(f);
      try {
         in.skip(offset);
         return in;
      }
      catch (IOException e) {
         closeSilently(in);
         throw e;
      }
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   private long calcOffset(long chunkOff, long chunkSz, File f) throws IOException {
      dbg.startFunc("calc chunk offset");
      dbg.startTimer();

      InputStream in = new FileInputStream(f);

      try {
         long realOff = 0;
         int b;
         int oldB;
         long nSkip = chunkSz-1;
         long jumps = chunkOff;

         while (jumps > 0) {
            realOff += nSkip;
            jumps -= chunkSz;
            in.skip(nSkip);
            nSkip = chunkSz-1;

            for (;;) {
               b = in.read();
               realOff++;

               if (b == -1) {
                  dbg.stopTimer();
                  dbg.endFunc("calc chunk offset");
                  return realOff;
               }
               else if (isCR(b) || isLF(b)) {
                  oldB = b;
                  b = in.read();

                  if (!isCRLF(oldB, b) && !isLFCR(oldB, b))
                     nSkip--;
                  else
                     realOff++;

                  break;
               }
            }
         }

         dbg.stopTimer();
         dbg.endFunc("calc chunk offset");

         return realOff;
      }
      finally {
         closeSilently(in);
      }
   }

   private ChunkMarker readLine(Chunk chunk) throws IOException {
      if (nextByte >= chunkSz)
         return null;

      int b;
      int lineTailLen = 0;
      int nextLineOff = nextByte;
      int lineLen     = 0;

      for (;;) {
         b = buffer[nextByte];
         nextByte++;
         lineTailLen++;

         if (b == -1) { // EOF
            lineLen += lineTailLen-1;
            nextByte--;

            if (lineLen <= 0)
               return null;
            else
               return chunk.addMarker(nextByte - lineLen, lineLen);
         }
         else if (isCR(b) || isLF(b)) { // EOL
            int lnEnd = nextByte-1;
            lineLen += lineTailLen-1;

            if (nextByte > chunkSz)
               return chunk.addMarker(lnEnd - lineLen, lineLen);

            b = buffer[nextByte];
            if (isCRLF(buffer[lnEnd], b) || isLFCR(buffer[lnEnd], b)) // it could be either CR,
               nextByte++;                                            // LF or CRLF or LFCR

            return chunk.addMarker(lnEnd - lineLen, lineLen);
         }

         if (nextByte == chunkSz) {
            b = buffer[nextByte];
            if (b != -1 && !isCR(b) && !isLF(b))
               continue;

            nextByte++;
            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addMarker(nextByte-1 - lineLen, lineLen);
         }
         else if (nextByte == chunkSz + CHUNK_OVERHEAD_SIZE) {   // allow chunks of more
            b = buffer[nextByte];                                // than the official size
            if (b != -1 && !isCR(b) && !isLF(b))
               throw new IOException("Chunk size too small to store even one line of text");

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addMarker(nextByte - lineLen, lineLen);
         }

         if (nextByte == bufferSz) {
            lineLen    += bufferSz-nextLineOff;
            lineTailLen = 0;
            nextLineOff = 0;
         }
      }
   }

   private boolean isCR(int c) {
      return c == '\r';
   }

   private boolean isLF(int c) {
      return c == '\n';
   }

   private boolean isCRLF(int c1, int c2) {
      return c1 == '\r' && c2 == '\n';
   }

   private boolean isLFCR(int c1, int c2) {
      return c1 == '\n' && c2 == '\r';
   }

   @Override
   public void close() throws IOException {
      closeSilently(in);
   }

}