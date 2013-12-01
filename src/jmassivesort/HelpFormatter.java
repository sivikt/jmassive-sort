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
package jmassivesort;

import java.util.Map;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public class HelpFormatter {

   private static final int KEY_LEFT_PADDING = 4;
   private static final int VALUE_LEFT_PADDING = 30;

   public static void printUsage(String error, Map<String, String> paramsDescription) {
      printUsage(error, null, paramsDescription);
   }

   public static void printUsage(String error, String algorithmName, Map<String, String> paramsDescription) {
      StringBuilder buf = new StringBuilder();

      appendLine(buf, "Error: %s", error);

      appendLine(buf, "Usage: java -jar jar_file %s[-options]", algorithmName == null ? "" : algorithmName + " ");
      appendLine(buf, "where options include:");

      for (String option : paramsDescription.keySet())
         appendKeyValue(buf, option, paramsDescription.get(option));

      printMsg(buf.toString());
   }

   private static void appendLine(StringBuilder buf, String line, Object... params) {
      buf.append(String.format(line, params));
      buf.append("\n");
   }

   private static void appendKeyValue(StringBuilder buf, String key, String value) {
      buf.append(spaces(KEY_LEFT_PADDING));
      buf.append(key);
      buf.append(spaces(VALUE_LEFT_PADDING - key.length()));
      appendLine(buf, value);
   }

   private static String spaces(int count) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < count; i++)
         buf.append(' ');

      return buf.toString();
   }

   private static void printMsg(String msg, Object... params) {
      System.out.println(String.format(msg, params));
   }

}
