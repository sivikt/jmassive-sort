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
package jmassivesort.util;

import java.io.*;
import java.util.Stack;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public class Debugger {

   private Class clazz;
   private PrintStream wr = System.out;
   private Stack<Long> memoryMarkers = new Stack<>();
   private Stack<Long> timeMarkers = new Stack<>();

   private Debugger(Class clazz) {
      this.clazz = clazz;
   }

   public static Debugger create(Class clazz) {
      return new Debugger(clazz);
   }

   public void startFunc(String msg, Object... params) {
      log(prefixIn() + String.format(msg, params));
   }

   public void endFunc(String msg, Object... params) {
      log(prefixOut() + String.format(msg, params));
   }

   public void newLine() {
      log(prefix());
   }

   public void echo(String msg) {
      log(prefix() + msg);
   }

   public void startTimer() {
      timeMarkers.push(System.currentTimeMillis());
      log(prefix() + "start timer");
   }

   public void stopTimer() {
      long end = System.currentTimeMillis();
      log(prefix() + "stop timer in " + (double) (end - timeMarkers.pop()) / 1000 + " s");
   }

   private String prefix() {
      return "dbg: [" + clazz.getSimpleName() + "]: ";
   }

   private String prefixIn() {
      return prefix() + ">>> ";
   }

   private String prefixOut() {
      return prefix() + "<<< ";
   }

   private long calcFreeMemory() {
      Runtime runtime = Runtime.getRuntime();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long maxMemory = runtime.maxMemory();
      long usedMemory = totalMemory - freeMemory;
      long availableMemory = maxMemory - usedMemory;
      return availableMemory;
   }

   public void markFreeMemory() {
      long memoryMarker = calcFreeMemory();
      memoryMarkers.push(memoryMarker);
      log(prefix() + "mark free memory on " + bytesAndMb(memoryMarker));
   }

   public void checkMemoryUsage() {
      log(prefix() + "memory usage " + bytesAndMb(memoryMarkers.pop()-calcFreeMemory()));
   }

   private String bytesAndMb(long amount) {
      return amount + " bytes (" + amount/1024/1024 + " Mb)";
   }

   private void log(String str) {
      wr.println(str);
      wr.flush();
   }

}
