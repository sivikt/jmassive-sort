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

import jmassivesort.algs.chunks.Chunk;
import jmassivesort.algs.chunks.InMemoryChunkReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Serj Sintsov
 */
public class InMemoryChunkReaderTest {

   public static void main(String[] args) throws IOException {
      test();
   }

  // @Test
   public static void test() throws IOException {
      File src = new File("testSrc/resources/inputHuge.txt");
      BufferedReader rd = new BufferedReader(new FileReader(src));
      int numChunks = 50;

      double[] times = new double[5];

      for (int k = 0; k < 1; k++) {
         long fullStart = System.currentTimeMillis();

         for (int i = 1; i <= numChunks; i++) {
            InMemoryChunkReader reader = new InMemoryChunkReader(i, numChunks, src);
            Chunk chunk = reader.readChunk();

//            System.out.println(">>> chunk " + i + " content");
//            int j = 0;
//            for (Chunk.ChunkLine line : chunk) {
//               j++;
//
//               String str = new String(chunk.rawData(), line.offset, line.len, Charset.defaultCharset());
//               System.out.println(str);
//
//               if (!rd.readLine().equals(str))
//                  throw new IllegalStateException("Incorrect line at pos " + j + ", chunk " + i);
//            }
//            System.out.println("<<< chunk " + i + " content");
//            System.out.println();
         }

         long fullEnd = System.currentTimeMillis();
         times[k] = (double)(fullEnd - fullStart)/1000;
      }

      for (int i = 0; i < times.length; i++)
         System.out.println("Finished in " + times[i] + " s");
   }

}
