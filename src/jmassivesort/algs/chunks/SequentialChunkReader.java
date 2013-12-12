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

import java.io.*;

import static jmassivesort.util.IOUtils.closeSilently;
import jmassivesort.algs.chunks.Chunk.ChunkMarker;

/**
 * Reads a file sequentially chunk-by-chunk {@link jmassivesort.algs.chunks.Chunk}.
 * Each chunk is filled with markers {@link ChunkMarker} which correspond to file
 * lines.
 *
 * @author Serj Sintsov
 */
public class SequentialChunkReader implements Closeable {

   private static final byte EOL_EXTRA_SIZE = 2;
   private static final int MAX_CHUNK_SIZE = Integer.MAX_VALUE - EOL_EXTRA_SIZE;

   private final int chunkSz;

   private byte[] buffer;
   private int bufferSz;
   private int nextByte;
   private int chunkEnd;

   private InputStream in;

   public SequentialChunkReader(int chunkSz, File src) throws IOException {
      if (!src.exists() || !src.isFile())
         throw new FileNotFoundException("No such file '" + src.getAbsolutePath() + "'");

      this.chunkSz = chunkSz;

      if (chunkSz < 0)
         throw new IllegalArgumentException("Chunk size must be positive integer");
      if (chunkSz == MAX_CHUNK_SIZE)
         throw new IllegalArgumentException("Chunk size too large. Max value is " + MAX_CHUNK_SIZE + " byte");

      in = new FileInputStream(src);
      buffer = new byte[chunkSz + EOL_EXTRA_SIZE]; // + some extra bytes to determine EOF or EOL
   }

   private void fill() throws IOException {
      bufferSz -= chunkEnd;

      System.arraycopy(buffer, chunkEnd, buffer, 0, bufferSz);
      int n = in.read(buffer, bufferSz, buffer.length-bufferSz);

      if (n < 0)
         buffer[bufferSz] = -1;
      else {
         bufferSz += n;
         if (bufferSz < buffer.length)
            buffer[bufferSz] = -1;
      }

      chunkEnd = 0;
      nextByte = 0;
   }

   public Chunk nextChunk() throws IOException {
      fill();

      if (buffer[nextByte] == -1)
         return null;

      Chunk chunk = new Chunk();
      for (;;)
         if (readLine(chunk) == null) break;

      byte[] chData = new byte[chunkEnd];
      System.arraycopy(buffer, 0, chData, 0, chunkEnd);
      chunk.setRawData(chData);
      return chunk;
   }

   private ChunkMarker readLine(Chunk ch) throws IOException {
      if (nextByte >= chunkSz)
         return null;

      int b;
      int lineLen     = 0;
      int lineOff = nextByte;

      for (;;) {
         b = buffer[nextByte];
         nextByte++;
         lineLen++;

         if (b == -1) { // EOF
            lineLen--;
            nextByte--;
            chunkEnd += nextByte - lineOff;

            if (lineLen <= 0)
               return null;
            else {
               ch.addMarkerUnsafely(nextByte - lineLen, lineLen);
               return null;
            }
         }
         else if (isCR(b) || isLF(b)) { // EOL
            int lnEnd = nextByte-1;
            lineLen -= 1;
            chunkEnd += nextByte - lineOff;

            b = buffer[nextByte];
            if (isCRLF(buffer[lnEnd], b) || isLFCR(buffer[lnEnd], b)) { // it could be either CR, LF or CRLF or LFCR
               nextByte++;
               chunkEnd++;
            }

            return ch.addMarkerUnsafely(lnEnd - lineLen, lineLen);
         }

         if (nextByte == chunkSz) {
            b = buffer[nextByte];
            if (b != -1 && !isCR(b) && !isLF(b)) {
               if (ch.allMarkers().isEmpty())
                  throw new IOException("Chunk size too small to store even one line of text");
               else
                  return null;
            }
            else if (b != -1) {
               chunkEnd++;
               // it could be either CR, LF or CRLF or LFCR
               if (isCRLF(b, buffer[nextByte+1]) || isLFCR(b, buffer[nextByte+1]))
                  chunkEnd++;
            }

            chunkEnd += nextByte - lineOff;

            if (lineLen == 0)
               return null;
            else {
               ch.addMarkerUnsafely(nextByte - lineLen, lineLen);
               return null;
            }
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