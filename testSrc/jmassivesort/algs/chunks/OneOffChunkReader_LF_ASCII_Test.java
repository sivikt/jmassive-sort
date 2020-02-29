///**
// * Copyright 2013 Serj Sintsov <ssivikt@gmail.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package jmassivesort.algs.chunks;
//
//import org.apache.commons.io.FileUtils;
//
//import static org.junit.Assert.assertEquals;
//import org.testng.annotations.Test;
//
//import java.io.*;
//
///**
// * Unit test for {@link OneOffChunkReader}.
// *
// * @author Serj Sintsov
// */
//public class OneOffChunkReader_LF_ASCII_Test extends BaseChunkReaderTest {
//
//   @Test(description = "suppose we have an empty file as an input")
//   public void test_onEmptyFile() throws IOException {
//      File src = getFromResource("LF-ASCII/emptyFile.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onEmptyFile"));
//   }
//
//   @Test(description = "input file consists of one empty line")
//   public void test_onOneEmptyLine() throws IOException {
//      File src = getFromResource("LF-ASCII/oneEmptyLine.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onOneEmptyLine"));
//   }
//
//   @Test(description = "input file consists both of empty lines and full of characters lines")
//   public void test_onTextWithEmptyLines() throws IOException {
//      File src = getFromResource("LF-ASCII/textWithEmptyLines.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onTextWithEmptyLines"));
//   }
//
//   @Test(description = "input file consists only of empty lines")
//   public void test_onOnlyEmptyLines() throws IOException {
//      File src = getFromResource("LF-ASCII/onlyEmptyLines.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onOnlyEmptyLines"));
//   }
//
//   @Test(description = "input file consists of single char lines")
//   public void test_onSingleCharsPerLine() throws IOException {
//      File src = getFromResource("LF-ASCII/singleCharsPerLine.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onSingleCharsPerLine"));
//   }
//
//   @Test(description = "input file has two line and the last one is empty line")
//   public void test_onTwoLines() throws IOException {
//      File src = getFromResource("LF-ASCII/twoLines.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onTwoLines"));
//   }
//
//   @Test(description = "suppose there's an input with 2000 8 bytes integers")
//   public void test_onSmallData() throws IOException {
//      File src = getFromResource("autogen/LF-ASCII/inputSmall.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onSmallData"));
//   }
//
//   @Test(description = "suppose there's an input with 100 8 bytes integers")
//   public void test_onTinyData() throws IOException {
//      File src = getFromResource("autogen/LF-ASCII/inputTiny.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onBigData"));
//   }
//
//   @Test(description = "suppose there's a natural text as an input")
//   public void test_onNaturalText1() throws IOException {
//      File src = getFromResource("LF-ASCII/naturalText1.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onNaturalText1"));
//   }
//
//   @Test(description = "suppose there's a natural text as an input")
//   public void test_onNaturalText2() throws IOException {
//      File src = getFromResource("LF-ASCII/naturalText2.txt");
//      checkMergedChunks(1, 200, src, tmpFileName("onNaturalText2"));
//   }
//
//   private void checkMergedChunks(int fromChunksNum, int toChunksNum, File src, String outFileName) throws IOException {
//      for (int i = fromChunksNum; i <= toChunksNum; i++)
//      {
//         File out = createTmpFile(src.getParent(), outFileName + "_" + i);
//
//         try (BufferedOutputStream outWr = new BufferedOutputStream(new FileOutputStream(out)))
//         {
//            readChunksWriteToOutput(i, src, outWr);
//
//            outWr.close();
//            assertEquals("failed on numChunks=" + i, FileUtils.checksumCRC32(src), FileUtils.checksumCRC32(out));
//            FileUtils.forceDelete(out);
//         }
//      }
//   }
//
//   private void readChunksWriteToOutput(int numChunks, File src, BufferedOutputStream outWr) throws IOException {
//      for (int j = 1; j <= numChunks; j++) {
//         OneOffChunkReader reader = new OneOffChunkReader(j, numChunks, src);
//
//         Chunk chunk = reader.readChunk();
//         int lastMarker = chunk.allMarkers().size()-1;
//
//         if (j > 1 && (lastMarker > 0 || (lastMarker == 0 && chunk.allMarkers().get(0).len > 0)))
//            outWr.write(lns, 0, lns.length);
//
//         int k;
//         for (k = 0; k <= lastMarker; k++) {
//            Chunk.ChunkMarker marker = chunk.allMarkers().get(k);
//
//            outWr.write(chunk.rawData(), marker.off, marker.len);
//            if (k < lastMarker || (lastMarker == 0 && marker.len == 0))
//               outWr.write(lns, 0, lns.length);
//         }
//      }
//   }
//
//}
