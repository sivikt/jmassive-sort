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

import static jmassivesort.util.IOUtils.closeSilently;
import java.io.*;
import jmassivesort.algs.chunks.Chunk.ChunkMarker;

/**
 * Reads a part of the file's content called {@link Chunk}.
 * <p/>
 * To minimize disk reads this implementation reads the whole
 * file's part into the memory and then separates it into lines
 * (finds lines offsets and etc.).
 * <p/>
 * Also note that this reader supports only Linux like LF markers and
 * doesn't support unicode encoding.
 *
 * @author Serj Sintsov
 */
public class OneOffChunkReader implements Closeable {

   private static final int CHUNK_OVERHEAD_SIZE = 1 * 1024 * 1024; // 1Mb
   private static final int MAX_CHUNK_SIZE      = Integer.MAX_VALUE - CHUNK_OVERHEAD_SIZE;

   private final long chunkSz;
   private final int  chunkOverSz;

   private byte[] buffer;
   private int nextByte;
   boolean countEolEof = false;

   private InputStream in;

   public OneOffChunkReader(int chunkId, int numChunks, File src) throws IOException {
      if (!src.exists() || !src.isFile())
         throw new FileNotFoundException("No such file '" + src.getAbsolutePath() + "'");

      // keep chunk size equals to the closest integer number which
      // is bigger then the decimal chunk size number (e.g. 1.2 -> 2)
      chunkSz  = (long) Math.ceil((double) src.length() / numChunks);
      if (chunkSz >= MAX_CHUNK_SIZE)
         throw new IllegalArgumentException("Chunk size too large. Max value is " + MAX_CHUNK_SIZE + " byte");
      chunkOverSz = (int) chunkSz + CHUNK_OVERHEAD_SIZE;

      long off = chunkId * chunkSz - chunkSz;
      if (off < src.length()) {
         long chunkOff = calcOffset(off, chunkSz, src);
         in = createInputStream(src, chunkOff);
      }
      else // too many chunks
         in = null;
   }

   private void fill() throws IOException {
      int len = chunkOverSz;

      buffer = new byte[chunkOverSz + 1]; // +1 to determine EOF or end of buffer
      int n = in.read(buffer, 0, len);
      if (n < 0)
         buffer[0] = -1;
      else if (n <= chunkSz)
         buffer[n] = -1;
      else
         buffer[n] = -2; // end of buffer

      nextByte = 0;
   }

   public Chunk readChunk() throws IOException {
      if (in == null) // just return empty chunk
         return new Chunk();

      fill();

      Chunk chunk = new Chunk();
      chunk.setRawData(buffer);
      for (;;)
         if (readLine(chunk) == null) break;

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
      if (chunkOff == 0)
         return 0;

      InputStream in = new FileInputStream(f);

      int b;
      int step = (int) chunkSz-1; // Stop reading chunk if the last line exactly fits into chunkSz.
                                  // So we'll skip one byte less to be sure we don't lost EOL.
      long nSkips = 0;
      long jumps = chunkOff;

      try {
         while (jumps > 0) {
            nSkips += step;
            jumps -= chunkSz;
            in.skip(step);

            for (;;) {
               b = in.read();
               nSkips++;

               if (b == -1)
                  return nSkips;
               else if (isLF(b)) {
                  if (jumps == 0) {
                     if (nSkips == f.length())
                        nSkips--;
                     return nSkips;
                  }

                  break;
               }
            }
         }

         return nSkips;
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
      int lineLen     = 0;

      for (;;) {
         b = buffer[nextByte++];

         if (b < 0) { // EOF
            lineLen += lineTailLen;
            nextByte--;

            if (lineLen == 0)
               return null;
            else
               return chunk.addMarker(nextByte - lineLen, lineLen);
         }
         else if (isLF(b)) { // EOL
            int lnEnd = nextByte-1;
            lineLen += lineTailLen;

            if (nextByte > chunkSz)
               return chunk.addMarker(lnEnd - lineLen, lineLen);

            if (!countEolEof && buffer[nextByte] == -1) { // don't forget the last EOL+EOF
               nextByte--;
               countEolEof = true;
            }

            return chunk.addMarker(lnEnd - lineLen, lineLen);
         }
         else
            lineTailLen++;

         if (nextByte == chunkSz) {
            b = buffer[nextByte];
            if (b > 0 && !isLF(b))
               continue;

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addMarker(nextByte - lineLen, lineLen);
         }
         else if (nextByte == chunkOverSz) { // allow chunks of more than the official size
            b = buffer[nextByte];
            if (b > 0 && !isLF(b))
               throw new IOException("Chunk size too small to store even one line of text");

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addMarker(nextByte - lineLen, lineLen);
         }
      }
   }

   private boolean isLF(int c) {
      return c == '\n';
   }

   @Override
   public void close() throws IOException {
      closeSilently(in);
   }

}