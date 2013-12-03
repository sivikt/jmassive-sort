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

import jmassivesort.SortingAlgorithm;
import jmassivesort.SortingAlgorithmException;

import java.io.*;
import java.util.UUID;

/**
 * Sorts files each line of which contains 8 bytes positive integer numbers.
 * todo javadoc
 * todo make it stateless
 * @author Serj Sintsov
 */
public class TwoWayMergeSort implements SortingAlgorithm {

   private TwoWayMergeSortOptions options;

   public TwoWayMergeSort(TwoWayMergeSortOptions options) {
      if  (options == null) throw new IllegalArgumentException("options cannot be null");
      this.options = options;
   }

   @Override
   public void apply() throws SortingAlgorithmException {
      File sortedFile = createNewFile(sortedFileName());
      File leftBuffer = createNewFile(generateBufferName());
      File rightBuffer = createNewFile(generateBufferName());

      File srcFile = new File(options.getInputFilePath());

      try {
         sort(srcFile, sortedFile, leftBuffer, rightBuffer);
      }
      catch (IOException e) {
         throw new SortingAlgorithmException("Sorting error", e);
      }
      finally {
         leftBuffer.delete();
         rightBuffer.delete();
      }
   }

   private void sort(File src, File out, File leftBuffer, File rightBuffer) throws IOException {
      long i = 1;
      debug("pick series size " + i);
      long N = fillBuffersAndCount(i, src, leftBuffer, rightBuffer);
      debug("merge series size " + i);
      mergeBuffers(i, out, leftBuffer, rightBuffer);

      while ((i *= 2) < N) {
         debug("pick series size " + i);
         fillBuffers(i, out, leftBuffer, rightBuffer);
         debug("merge series size " + i);
         mergeBuffers(i, out, leftBuffer, rightBuffer);
      }

      debug("items number " + N);
   }

   private void mergeBuffers(long seriesSz, File out, File leftBuffer, File rightBuffer) throws IOException {
      BufferedReader leftRd = null;
      BufferedReader rightRd = null;
      BufferedWriter outWr = null;

      try {
         leftRd = new BufferedReader(new FileReader(leftBuffer));
         rightRd = new BufferedReader(new FileReader(rightBuffer));
         outWr = new BufferedWriter(new FileWriter(out));

         while (!mergeSeries(seriesSz, outWr, leftRd, rightRd)) { }
      }
      finally {
         closeSilently(outWr);
         closeSilently(leftRd);
         closeSilently(rightRd);
      }
   }

   /**
    * @return {@code true} if reached EOF
    */
   private boolean mergeSeries(long seriesSz, BufferedWriter outWr, BufferedReader leftRd, BufferedReader rightRd)
         throws IOException {
      long i = 0;
      long j = 0;

      String leftItem = leftRd.readLine();
      String rightItem = rightRd.readLine();

      while (leftItem != null && rightItem != null) {
         long left = Long.parseLong(leftItem);
         long right = Long.parseLong(rightItem);

         if (left <= right) {
            outWr.write(leftItem);
            outWr.newLine();
            i++;
            if (i < seriesSz) leftItem = leftRd.readLine();
            else leftItem = null;
         }
         else if (left > right) {
            outWr.write(rightItem);
            outWr.newLine();
            j++;
            if (j < seriesSz) rightItem = rightRd.readLine();
            else rightItem = null;
         }
      }

      while (leftItem != null) {
         outWr.write(leftItem);
         outWr.newLine();
         i++;
         if (i < seriesSz) leftItem = leftRd.readLine();
         else leftItem = null;
      }

      while (rightItem != null) {
         outWr.write(rightItem);
         outWr.newLine();
         j++;
         if (j < seriesSz) rightItem = rightRd.readLine();
         else rightItem = null;
      }

      return i != seriesSz || j != seriesSz;
   }

   private void fillBuffers(long seriesSz, File src, File leftBuffer, File rightBuffer) throws IOException {
      BufferedWriter leftWr = null;
      BufferedWriter rightWr = null;
      BufferedReader srcRd = null;

      try {
         leftWr = new BufferedWriter(new FileWriter(leftBuffer));
         rightWr = new BufferedWriter(new FileWriter(rightBuffer));
         srcRd = new BufferedReader(new FileReader(src));

         while (read(seriesSz, srcRd, leftWr) == seriesSz && read(seriesSz, srcRd, rightWr) == seriesSz) { }
      }
      finally {
         closeSilently(srcRd);
         closeSilently(leftWr);
         closeSilently(rightWr);
      }
   }

   private long fillBuffersAndCount(long seriesSz, File src, File leftBuffer, File rightBuffer) throws IOException {
      BufferedWriter leftWr = null;
      BufferedWriter rightWr = null;
      BufferedReader srcRd = null;
      long lineNum = 0;

      try {
         leftWr = new BufferedWriter(new FileWriter(leftBuffer));
         rightWr = new BufferedWriter(new FileWriter(rightBuffer));
         srcRd = new BufferedReader(new FileReader(src));

         long leftCount = read(seriesSz, srcRd, leftWr);
         long rightCount = read(seriesSz, srcRd, rightWr);
         lineNum += leftCount + rightCount;

         while (leftCount == seriesSz && rightCount == seriesSz) {
            leftCount = read(seriesSz, srcRd, leftWr);
            rightCount = read(seriesSz, srcRd, rightWr);
            lineNum += leftCount + rightCount;
         }
      }
      finally {
         closeSilently(srcRd);
         closeSilently(leftWr);
         closeSilently(rightWr);
      }

      return lineNum;
   }

   /**
    * @return number of read lines
    */
   private long read(long count, BufferedReader from, BufferedWriter to) throws IOException {
      long i = 0;
      String line;
      while (i != count && (line = from.readLine()) != null) {
         to.write(line);
         to.newLine();
         i++;
      }

      return i;
   }

   private void closeSilently(Closeable target) {
      try {
         if (target != null) target.close();
      }
      catch (IOException e) { /** nothing to do */ }
   }

   private File createNewFile(String path) {
      File newFile = new File(path);
      try {
         if (newFile.createNewFile()) return newFile;
         else throw new SortingAlgorithmException("File '" + path + "' already exists");
      } catch (IOException e) {
         throw new SortingAlgorithmException("Creation of file '" + path + "' is failed due to the error", e);
      }
   }

   private String sortedFileName() {
      return options.getTaskId() + ".txt";
   }

   private String generateBufferName() {
      return options.getTaskId() + "_" + UUID.randomUUID().toString() + ".tmp";
   }

   private void debug(String msg, Object... params) {
      System.out.println(">>> debug: " + String.format(msg, params));
   }

}