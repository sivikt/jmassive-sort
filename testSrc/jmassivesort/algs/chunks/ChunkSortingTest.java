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

import jmassivesort.JMassiveSort;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Serj Sintsov
 */
public class ChunkSortingTest {


   public static void main(String[] args) throws IOException {
      long start = System.currentTimeMillis();
      JMassiveSort.main(new String[]{"chunk-sorting", "1", "12", "testSrc/resources/inputHuge.txt"});
      long end = System.currentTimeMillis();

      System.out.println("Sorted in " + (double)(end - start)/1000 + " s");

      File output = new File("1.txt");
      assert output.exists() && output.isFile();

      BufferedReader br = new BufferedReader(new FileReader(output));

      String key = br.readLine();
      if (key == null) {
         System.out.println("Output file is empty");
         return;
      }
   }

}
