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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * todo javadoc
 * @author Serj Sintsov
 */
public class Chunk implements Iterable<Chunk.ChunkLine> {

   private byte[] content;
   private ChunkLine head, tail;

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

   public static class ChunkLine {
      public final int offset;
      public final int length;
      public ChunkLine next;

      public ChunkLine(int offset, int length) {
         this.offset = offset;
         this.length = length;
      }
   }

   public ChunkLine addLine(byte[] src) {
      byte[] n = new byte[content.length + src.length];
      System.arraycopy(content, 0, n, 0, content.length);
      System.arraycopy(src, 0, n, content.length, src.length);
      content = n;

      ChunkLine oldTail = tail;
      tail = new ChunkLine(oldTail == null ? 0 : oldTail.offset + oldTail.length, src.length);
      if (head == null) head = tail;
      else      oldTail.next = tail;

      return head;
   }

   public byte[] getContent() {
      return content;
   }

   public ChunkLine addLine(int offset, int length) {
      ChunkLine oldTail = tail;
      tail = new ChunkLine(offset, length);
      if (head == null) head = tail;
      else      oldTail.next = tail;

      return head;
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

}
