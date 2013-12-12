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
import java.nio.charset.Charset;

/**
 * @author Serj Sintsov
 */
public class SequentialChunkReaderTest {

   public static void main(String[] args) throws IOException {
      test();
   }

  // @Test
   public static void test() throws IOException {
      final byte[] lns = System.getProperty("line.separator").getBytes();

      File src = new File("testSrc/resources/1.chunk");
      File dest = new File("testSrc/resources/testOut");
      OutputStream out = new FileOutputStream(dest);

      SequentialChunkReader chRd = new SequentialChunkReader(1*1024*1024, src);

      Chunk chunk;
      int nChunks = 0;
      while ((chunk = chRd.nextChunk()) != null) {
         nChunks++;
         System.out.println("chunk " + nChunks);
         for (int i = 0; i < chunk.allMarkers().size(); i++) {
            Chunk.ChunkMarker marker = chunk.allMarkers().get(i);
            out.write(chunk.rawData(), marker.off, marker.len);
            out.write(lns, 0, lns.length);
         }
      }

      out.close();

      BufferedReader expected = new BufferedReader(new FileReader(src));
      BufferedReader actual = new BufferedReader(new FileReader(dest));
      String expectedStr;
      String actualStr;
      int nLines = 0;
      while ((expectedStr = expected.readLine()) != null) {
         nLines++;
         actualStr = actual.readLine();
         System.out.println(nLines + ": expected=" + expectedStr + ";  actual=" + actualStr);
         if (actualStr == null)
            throw new IllegalStateException("not enough lines");
         if (!expectedStr.equals(actualStr)) {
            throw new IllegalStateException("not equal lines");
         }
      }

      expected.close();
      actual.close();
   }

}
