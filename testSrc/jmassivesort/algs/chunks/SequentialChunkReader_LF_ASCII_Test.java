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
//import org.testng.annotations.Test;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Unit test for {@link jmassivesort.algs.chunks.OneOffChunkReader}.
// *
// * @author Serj Sintsov
// */
//public class SequentialChunkReader_LF_ASCII_Test extends BaseChunkReaderTest {
//
//   @Test(description = "suppose we have an empty file as an input")
//   public void test_onEmptyFile() throws IOException {
//      File src = getFromResource("LF-ASCII/emptyFile.txt");
//      readChunkByChunk(1, 200, 1, src, tmpFileName("onEmptyFile"));
//   }
//
//   @Test(description = "input file consists of one empty line")
//   public void test_onOneEmptyLine() throws IOException {
//      File src = getFromResource("LF-ASCII/oneEmptyLine.txt");
//      readChunkByChunk(1, 200, 1, src, tmpFileName("onOneEmptyLine"));
//   }
//
//   @Test(description = "input file consists both of empty lines and full of characters lines")
//   public void test_onTextWithEmptyLines() throws IOException {
//      File src = getFromResource("LF-ASCII/textWithEmptyLines.txt");
//      readChunkByChunk(10, 200, 1, src, tmpFileName("onTextWithEmptyLines"));
//   }
//
//   @Test(description = "input file consists only of empty lines")
//   public void test_onOnlyEmptyLines() throws IOException {
//      File src = getFromResource("LF-ASCII/onlyEmptyLines.txt");
//      readChunkByChunk(1, 200, 1, src, tmpFileName("onOnlyEmptyLines"));
//   }
//
//   @Test(description = "input file consists of single char lines")
//   public void test_onSingleCharsPerLine() throws IOException {
//      File src = getFromResource("LF-ASCII/singleCharsPerLine.txt");
//      readChunkByChunk(1, 200, 1, src, tmpFileName("onSingleCharsPerLine"));
//   }
//
//   @Test(description = "input file has two line and the last one is empty line")
//   public void test_onTwoLines() throws IOException {
//      File src = getFromResource("LF-ASCII/twoLines.txt");
//      readChunkByChunk(1, 200, 1, src, tmpFileName("onTwoLines"));
//   }
//
//   @Test(description = "suppose there's an input with 2000 8 bytes integers")
//   public void test_onSmallData() throws IOException {
//      File src = getFromResource("autogen/LF-ASCII/inputSmall.txt");
//      readChunkByChunk(19, 200, 1, src, tmpFileName("onSmallData"));
//   }
//
//   @Test(description = "suppose there's an input with 100 8 bytes integers")
//   public void test_onTinyData() throws IOException {
//      File src = getFromResource("autogen/LF-ASCII/inputTiny.txt");
//      readChunkByChunk(19, 200, 1, src, tmpFileName("onTinyData"));
//   }
//
//   @Test(description = "suppose there's a natural text as an input")
//   public void test_onNaturalText1() throws IOException {
//      File src = getFromResource("LF-ASCII/naturalText1.txt");
//      readChunkByChunk(72, 200, 1, src, tmpFileName("onNaturalText1"));
//   }
//
//   @Test(description = "suppose there's a natural text as an input")
//   public void test_onNaturalText2() throws IOException {
//      File src = getFromResource("LF-ASCII/naturalText2.txt");
//      readChunkByChunk(74, 200, 1, src, tmpFileName("onNaturalText2"));
//   }
//
//   private void readChunkByChunk(int minChunkSz, int maxChunkSz, int inc, File src, String outFileName) throws IOException {
//      for (int i = minChunkSz; i <= maxChunkSz; i += inc)
//      {
//         File out = createTmpFile(src.getParent(), outFileName + "_" + i);
//
//         try (BufferedOutputStream outWr = new BufferedOutputStream(new FileOutputStream(out)))
//         {
//            readAllChunksAndWriteToOutput(i, src, outWr);
//
//            outWr.close();
//            assertEquals("failed on chunkSize=" + i, FileUtils.checksumCRC32(src), FileUtils.checksumCRC32(out));
//            FileUtils.forceDelete(out);
//         }
//      }
//   }
//
//   private void readAllChunksAndWriteToOutput(int chSz, File src, BufferedOutputStream outWr) throws IOException {
//      SequentialChunkReader reader = new SequentialChunkReader(chSz, src);
//      Chunk ch;
//      int chCount = 0;
//      while ((ch = reader.nextChunk()) != null) {
//         chCount++;
//         int lastMarker = ch.allMarkers().size()-1;
//
//         if (chCount > 1 && (lastMarker > 0 || (lastMarker == 0 && ch.allMarkers().get(0).len > 0)))
//            outWr.write(lns, 0, lns.length);
//
//         int k;
//         for (k = 0; k <= lastMarker; k++) {
//            Chunk.ChunkMarker marker = ch.allMarkers().get(k);
//
//            outWr.write(ch.rawData(), marker.off, marker.len);
//            if (k < lastMarker || (lastMarker == 0 && marker.len == 0))
//               outWr.write(lns, 0, lns.length);
//         }
//      }
//   }
//
//}
