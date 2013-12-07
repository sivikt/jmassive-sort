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

import java.io.Closeable;
import java.io.IOException;

/**
 * todo javadoc
 * @author Serj Sintsov
 */
public final class IOUtils {

   public static void closeSilently(Closeable target) {
      try {
         if (target != null) target.close();
      }
      catch (IOException e) { /** nothing to do */ }
   }

}
