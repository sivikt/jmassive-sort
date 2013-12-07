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

import sun.security.action.GetPropertyAction;

import java.io.OutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;

import static jmassivesort.util.IOUtils.closeSilently;

/**
 * Writes {@link Chunk} to the disk using best approach.
 *
 * @author Serj Sintsov
 */
public class BufferedChunkWriter implements Closeable {

   private static final int MAX_BUFFER_SZ = 10 * 1024 * 1024; // 10Mb
   private static final byte[] lns = AccessController.doPrivileged(new GetPropertyAction("line.separator")).getBytes();

   private OutputStream out = null;

   public BufferedChunkWriter(File dest) throws FileNotFoundException {
      out = new FileOutputStream(dest);
   }

   public void write(byte[] chunk, Chunk.ChunkLine[] lines) throws IOException {
      int bufferSz = 0;
      byte[] buffer = new byte[MAX_BUFFER_SZ];

      for (Chunk.ChunkLine line : lines) {
         if (bufferSz + lines.length + lns.length < MAX_BUFFER_SZ) {
            System.arraycopy(chunk, line.offset, buffer, bufferSz, line.length);
            bufferSz += lines.length;
            System.arraycopy(lns, 0, buffer, bufferSz, lns.length);
            bufferSz += lns.length;
         }
         else {
            out.write(buffer, 0, bufferSz);
            buffer = new byte[MAX_BUFFER_SZ];
            bufferSz = 0;
         }
      }
   }

   @Override
   public void close() throws IOException {
      closeSilently(out);
   }

}
