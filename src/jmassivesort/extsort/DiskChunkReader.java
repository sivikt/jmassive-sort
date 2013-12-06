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
package jmassivesort.extsort;

import java.io.*;

import static jmassivesort.extsort.IOUtils.closeSilently;

/**
*
* todo javadoc
* @author Serj Sintsov
*/
public class DiskChunkReader implements Closeable {

   private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024; // 1Mb
   private static final int CHUNK_OVERHEAD_SIZE = 1 * 1024 * 1024; // 1Mb

   private final long chunkSz;
   private final long chunkOff;

   private byte[] buffer;
   private int bufferSz;
   private int nextByte;
   private int nBytes;

   private File src;
   private InputStream in;

   public DiskChunkReader(int chunkId, int numChunks, File src) {
      // keep chunk size equals to the closest integer number which
      // is bigger then the decimal chunk size number (e.g. 1.2 -> 2)
      chunkSz  = (long) Math.ceil((double) src.length() / numChunks);
      long off = chunkId * chunkSz - chunkSz;
      chunkOff = off == src.length() ? -1 : off; // too many chunks

      if (chunkSz == Integer.MAX_VALUE)
         throw new IllegalArgumentException("Chunk size too large. Max value is " + (Integer.MAX_VALUE-1) + " byte");

      this.src    = src;
      this.nBytes = 0;
      this.buffer = new byte[0]; // just to avoid null comparisons
   }

   public Chunk readChunk() throws IOException {
      if (chunkOff == -1)
         return new Chunk();

      long realOffset = calcOffset(chunkOff, chunkSz, src);

      in = createInputStream(src, realOffset);
      Chunk chunk = new Chunk();

      DebugUtils.startFunc("read chunk lines");
      DebugUtils.startTimer();

      for (;;)
         if (readLine(chunk) == null) break;

      DebugUtils.stopTimer();
      DebugUtils.endFunc("read chunk lines (" + nBytes + " bytes)");
      DebugUtils.newLine();

      in = createInputStream(src, realOffset);
      byte[] content = new byte[nBytes];
      in.read(content, 0 , nBytes);
      chunk.setContent(content);

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
      DebugUtils.startFunc("calc chunk offset");
      DebugUtils.startTimer();

      InputStream in = new FileInputStream(f);

      try {
         if (chunkOff == 0) { // don't skip bytes for the first chunk
            DebugUtils.stopTimer();
            DebugUtils.endFunc("calc chunk offset");
            return chunkOff;
         }

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
                  DebugUtils.stopTimer();
                  DebugUtils.endFunc("calc chunk offset");
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

         DebugUtils.stopTimer();
         DebugUtils.endFunc("calc chunk offset");

         return realOff;
      }
      finally {
         closeSilently(in);
      }
   }

   private Chunk.ChunkLine readLine(Chunk chunk) throws IOException {
      if (nBytes >= chunkSz)
         return null;

      int b;
      int oldB;
      int lineTailLen = 0;
      int lineLen     = 0;
      int nextLineOff = nextByte;

      for (;;) {
         b = nextByte();
         nBytes++;
         lineTailLen++;

         if (b == -1) { // EOF
            lineLen += lineTailLen-1;
            nBytes--;

            if (lineLen <= 0)
               return null;
            else
               return chunk.addLine(nBytes - lineLen, lineLen);
         }
         else if (isCR(b) || isLF(b)) { // EOL
            int strEnd = nBytes-1;
            lineLen += lineTailLen-1;

            if (nBytes > chunkSz)
               return chunk.addLine(strEnd - lineLen, lineLen);

            oldB = b;
            b = nextByte();
            if (!isCRLF(oldB, b) && !isLFCR(oldB, b)) // it could be either CR,
               nextByte--;                            // LF or CRLF or LFCR
            else                                      // in that case back byte to buf
               nBytes++;                              // or skip it

            return chunk.addLine(strEnd - lineLen, lineLen);
         }

         if (nBytes == chunkSz) {
            b = nextByte();
            if (b != -1 && !isCR(b) && !isLF(b)) {
               nextByte--;
               continue;
            }

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addLine(nBytes - lineLen, lineLen);
         }
         else if (nBytes == chunkSz + CHUNK_OVERHEAD_SIZE) {   // allow chunks of more
            b = nextByte();                                    // than the official size
            if (b != -1 && !isCR(b) && !isLF(b))
               throw new IOException("Chunk size too small to store even one line of text");
            else
               nextByte--;

            lineLen += lineTailLen;
            if (lineLen == 0)
               return null;
            else
               return chunk.addLine(nBytes - lineLen, lineLen);
         }

         if (nextByte == bufferSz) {
            lineLen    += bufferSz - nextLineOff;
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

   private int nextByte() throws IOException {
      if (nextByte >= bufferSz)
         fill();
      return buffer[nextByte++];
   }

   private void fill() throws IOException {
      buffer = new byte[DEFAULT_BUFFER_SIZE+1]; // +1 for EOF mark
      int n = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);
      bufferSz = n > 0 ? n : 0;
      buffer[bufferSz] = -1;
      nextByte = 0;
   }

   @Override
   public void close() throws IOException {
      closeSilently(in);
   }

}