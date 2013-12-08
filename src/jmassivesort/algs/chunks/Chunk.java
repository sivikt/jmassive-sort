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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * todo javadoc
 * @author Serj Sintsov
 */
public class Chunk {

   private byte[] rawData;
   private List<ChunkMarker> markers = new ArrayList<>(0);

   public Chunk(byte[] data) {
      if (data == null)
         throw new IllegalArgumentException("Raw data must be not null");
      this.rawData = data;
   }

   public byte[] rawData() {
      return rawData;
   }

   public List<ChunkMarker> allMarkers() {
      return markers;
   }

   public ChunkMarker addMarker(int offset, int length) {
      if (offset+length >= rawData.length)
         throw new IllegalArgumentException("Marker offset and length is greater than data length");
      ChunkMarker ln = new ChunkMarker(offset, length);
      markers.add(ln);
      return ln;
   }

   public static class ChunkMarker {
      public final int off;
      public final int len;

      public ChunkMarker(int offset, int length) {
         this.off = offset;
         this.len = length;
      }

      public int compareTo(byte[] c, ChunkMarker other) {
         int i = this.off;
         int j = other.off;

         while ((i < this.off+this.len) && (j < other.off+other.len)) {
            if (c[i] != c[j])
               return c[i] - c[j];
            else {
               i++; j++;
            }
         }

         return this.len - other.len;
      }
   }

}
