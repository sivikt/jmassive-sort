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

import java.io.*;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public class DebugUtils {

   public static boolean debugOn = true;
   private static long startTime = -1;
   private static PrintStream wr = System.out;

   public static void startFunc(String msg, Object... params) {
      if (!debugOn) return;
      log("dbg: >>> " + String.format(msg, params));
   }

   public static void endFunc(String msg, Object... params) {
      if (!debugOn) return;
      log("dbg: <<< " + String.format(msg, params));
   }

   public static void newLine() {
      if (!debugOn) return;
      log("dbg:");
   }

   public static void startTimer() {
      startTime = System.currentTimeMillis();
      log("dbg: start timer");
   }

   public static void stopTimer() {
      if (startTime < 0) return;
      long end = System.currentTimeMillis();
      log("dbg: stop timer in " + (double) (end - startTime) / 1000 + " s");
   }

   private static void log(String str) {
      wr.println(str);
      wr.flush();
   }

}
