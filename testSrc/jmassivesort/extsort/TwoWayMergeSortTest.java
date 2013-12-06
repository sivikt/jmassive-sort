package jmassivesort.extsort; /**
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

import jmassivesort.JMassiveSort;
import org.testng.annotations.Test;

import java.io.*;

/**
 * @author Serj Sintsov
 */
public class TwoWayMergeSortTest {

   @Test
   public void test() throws IOException {
      long start = System.currentTimeMillis();
      JMassiveSort.main(new String[] {"2way-mergesort", "777", "testSrc/resources/inputBig.txt"});
      long end = System.currentTimeMillis();

      System.out.println("Sorted in " + (double)(end - start)/1000 + " s");

      File output = new File("777.txt");
      assert output.exists() && output.isFile();

      BufferedReader br = new BufferedReader(new FileReader(output));

      String key = br.readLine();
      if (key == null) {
         System.out.println("Output file is empty");
         return;
      }
      String prevKey = key;
      for (int i = 2; (key = br.readLine()) != null; i++)
         if (key.compareTo(prevKey) <= 0) throw new IllegalStateException("Incorrect value at position " + i);
   }

}
