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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * todo javadoc
 * @author Serj Sintsov
 */
public class Chunk implements Iterable<Chunk.ChunkLine> {

   private byte[] content;
   private ChunkLine head, tail;
   private int size;

   public Chunk() {
      content = new byte[0];
   }

   private class LinkedListIterator implements Iterator<ChunkLine> {
      private ChunkLine next = head;

      @Override
      public boolean hasNext() {
         return next != null;
      }

      @Override
      public ChunkLine next() {
         if (next == null)
            throw new NoSuchElementException();
         ChunkLine item = next;
         next = next.next;
         return item;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Cannot remove from queue");
      }
   }

   @Override
   public Iterator<ChunkLine> iterator() {
      return new LinkedListIterator();
   }

   public byte[] getContent() {
      return content;
   }

   public void setContent(byte[] c) {
      this.content = c;
   }

   public int size() {
      return size;
   }

   public List<ChunkLine> getLinesList() {
      List<ChunkLine> lines = new ArrayList<>(size);
      ChunkLine next = head;

      while (next != null) {
         lines.add(next);
         next = next.next;
      }

      return lines;
   }

   public ChunkLine addLine(int offset, int length) {
      ChunkLine oldTail = tail;
      tail = new ChunkLine(offset, length);
      if (head == null) head = tail;
      else      oldTail.next = tail;
      size++;

      return head;
   }

   public static class ChunkLine {
      public final int off;
      public final int len;
      public ChunkLine next;

      public ChunkLine(int offset, int length) {
         this.off = offset;
         this.len = length;
      }

      public int compareTo(byte[] c, Chunk.ChunkLine other) {
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
