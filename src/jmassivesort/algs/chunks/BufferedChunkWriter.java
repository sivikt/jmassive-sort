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
import java.util.List;

import static jmassivesort.util.IOUtils.closeSilently;
import static jmassivesort.algs.chunks.Chunk.ChunkMarker;

/**
 * Writes {@link Chunk} to the disk using best approach.
 *
 * @author Serj Sintsov
 */
public class BufferedChunkWriter implements Closeable, Flushable {

   private static final int MAX_BUFFER_SZ = 20 * 1024 * 1024; // 10Mb
   private static final byte[] lns = System.getProperty("line.separator").getBytes();

   private OutputStream out = null;
   private int bufferSz = 0;
   private byte[] buffer = new byte[MAX_BUFFER_SZ];

   public BufferedChunkWriter(File dest) throws FileNotFoundException {
      out = new FileOutputStream(dest);
   }

   public void write(byte[] chunkData, List<ChunkMarker> lines) throws IOException {
      for (ChunkMarker line : lines)
         write(chunkData, line);
   }

   public void write(byte[] chunkData, ChunkMarker line) throws IOException {
      if (bufferSz + line.len + lns.length < MAX_BUFFER_SZ)
         fill(chunkData, line);
      else {
         flush();
         fill(chunkData, line);
      }
   }

   private void fill(byte[] chunkData, ChunkMarker line) {
      System.arraycopy(chunkData, line.off, buffer, bufferSz, line.len);
      bufferSz += line.len;
      System.arraycopy(lns, 0, buffer, bufferSz, lns.length);
      bufferSz += lns.length;
   }

   @Override
   public void close() throws IOException {
      flush();
      closeSilently(out);
   }

   @Override
   public void flush() throws IOException {
      out.write(buffer, 0, bufferSz);
      buffer = new byte[MAX_BUFFER_SZ];
      bufferSz = 0;
   }

}
