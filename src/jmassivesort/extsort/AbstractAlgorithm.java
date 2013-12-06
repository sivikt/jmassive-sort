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

import java.io.File;
import java.io.IOException;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public abstract class AbstractAlgorithm implements SortingAlgorithm {

   protected File createNewFile(String path) {
      File newFile = new File(path);
      try {
         if (newFile.createNewFile()) return newFile;
         else throw new SortingAlgorithmException("File '" + path + "' already exists");
      } catch (IOException e) {
         throw new SortingAlgorithmException("Creation of file '" + path + "' is failed due to the error", e);
      }
   }

   protected void debug(String msg, Object... params) {
      System.out.println(">>> debug: " + String.format(msg, params));
   }

}
