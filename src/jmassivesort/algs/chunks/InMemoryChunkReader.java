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
package jmassivesort.algs.chunks;

import jmassivesort.util.Debugger;

import static jmassivesort.util.IOUtils.closeSilently;
import java.io.*;

/**
 * Reads a part of the file's content called {@link Chunk}.
 * <p/>
 * To minimize disk reads this implementation reads the whole
 * file's part into the memory and then separates the part into
 * lines (finds lines offsets and etc.)
 *
 * @author Serj Sintsov
 */
public class InMemoryChunkReader implements Closeable {

   private final Debugger dbg = Debugger.create(getClass());

   private static final int CHUNK_OVERHEAD_SIZE = 1 * 1024 * 1024; // 1Mb
   private static final int MAX_CHUNK_SIZE      = Integer.MAX_VALUE - CHUNK_OVERHEAD_SIZE - 1;

   private final long chunkSz;
   private final long chunkOff;

   private byte[] buffer;
   private int bufferSz;
   private int nextByte;

   private File src;
   private InputStream in;

   public InMemoryChunkReader(int chunkId, int numChunks, File src) {
      // keep chunk size equals to the closest integer number which
      // is bigger then the decimal chunk size number (e.g. 1.2 -> 2)
      chunkSz  = (long) Math.ceil((double) src.length() / numChunks);
      long off = chunkId * chunkSz - chunkSz;
      chunkOff = off == src.length() ? -1 : off; // too many chunks

      if (chunkSz == MAX_CHUNK_SIZE)
         throw new IllegalArgumentException("Chunk size too large. Max value is " + MAX_CHUNK_SIZE + " byte");

      this.src = src;
   }

   public Chunk readChunk() throws IOException {
      if (chunkOff == -1)
         return new Chunk();

      long realOffset = calcOffset(chunkOff, chunkSz, src);

      in = createInputStream(src, realOffset);

      fill((int)chunkSz + CHUNK_OVERHEAD_SIZE + 1);

      Chunk chunk = new Chunk();

      dbg.startFunc("read chunk lines");
      dbg.startTimer();

      for (;;)
         if (readLine(chunk) == null) break;

      chunk.setContent(buffer);

      dbg.stopTimer();
      dbg.endFunc("read chunk lines (" + nextByte + " bytes)");
      dbg.newLine();

      return chunk;
   }

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

   private long calcOffset(long chunkOff, long chunkSz, File f) throws IOException {
      dbg.startFunc("calc chunk offset");
      dbg.startTimer();

      InputStream in = new FileInputStream(f);

      try {
         long realOff = 0;
         int b;
         int oldB;
         int delta = 0;
         long jumps = chunkOff;

         while (jumps > 0) {
            in.skip(chunkSz-1 - delta);
            jumps -= chunkSz;
            realOff += chunkSz-1;
            delta = 0;

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
                     delta = 1;
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

   private Chunk.ChunkLine readLine(Chunk chunk) throws IOException {
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
               return chunk.addLine(nextByte - lineLen, lineLen);
         }
         else if (isCR(b) || isLF(b)) { // EOL
            int lnEnd = nextByte-1;
            lineLen += lineTailLen-1;

            if (nextByte > chunkSz)
               return chunk.addLine(lnEnd - lineLen, lineLen);

            b = buffer[nextByte];
            if (isCRLF(buffer[nextByte-1], b) || isLFCR(buffer[nextByte-1], b)) // it could be either CR,
               nextByte++;                                                      // LF or CRLF or LFCR

            return chunk.addLine(lnEnd - lineLen, lineLen);
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
               return chunk.addLine(nextByte-1 - lineLen, lineLen);
         }
         else if (nextByte == chunkSz + CHUNK_OVERHEAD_SIZE) {   // allow chunks of more
            b = buffer[nextByte];                                // than the official size
            if (b != -1 && !isCR(b) && !isLF(b))
               throw new IOException("Chunk size too small to store even one line of text");

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addLine(nextByte - lineLen, lineLen);
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

   private void fill(int len) throws IOException {
      buffer = new byte[len+1]; // +1 for EOF mark
      int n = in.read(buffer, 0, len);
      bufferSz = n > 0 ? n : 0;
      buffer[bufferSz] = -1;
      nextByte = 0;
   }

   @Override
   public void close() throws IOException {
      closeSilently(in);
   }

}