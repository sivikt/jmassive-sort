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

import java.util.Comparator;
import jmassivesort.algs.chunks.Chunk.ChunkMarker;

/**
 *
 * todo javadoc
 * @author Serj Sintsov
 */
public class OrderFunctions {

   private static final class AscComparator implements Comparator<ChunkMarker> {
      private final Chunk ch;

      public AscComparator(Chunk ch) {
         this.ch = ch;
      }

      @Override
      public int compare(ChunkMarker m1, ChunkMarker m2) {
         return ch.compareMarkers(m1, m2);
      }
   }

   public static Comparator<ChunkMarker> asc(Chunk ch) {
      return new AscComparator(ch);
   }

}
