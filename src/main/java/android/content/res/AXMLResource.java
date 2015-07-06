/* 
 * Copyright 2015 Red Naga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.content.res;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;

import android.content.res.chunk.ChunkType;
import android.content.res.chunk.ChunkUtil;
import android.content.res.chunk.sections.ResourceSection;
import android.content.res.chunk.sections.StringSection;
import android.content.res.chunk.types.AXMLHeader;
import android.content.res.chunk.types.Chunk;

/**
 * Main AXMLResource object
 * 
 * @author tstrazzere
 */
public class AXMLResource {

    AXMLHeader header;
    StringSection stringSection;
    ResourceSection resourceSection;
    LinkedHashSet<Chunk> chunks;

    public AXMLResource() {
        chunks = new LinkedHashSet<Chunk>();
    }

    public AXMLResource(InputStream stream) throws IOException {
        chunks = new LinkedHashSet<Chunk>();
        if (!read(stream)) {
            throw new IOException();
        }
    }

    public boolean read(InputStream stream) throws IOException {

        IntReader reader = new IntReader(stream, false);

        // Get an attempted size until we know the read size
        int size = stream.available();

        while ((size - reader.getBytesRead()) > 4) {
            // This should just read all the chunks
            Chunk chunk = ChunkUtil.createChunk(reader);

            switch (chunk.getChunkType()) {
            case AXML_HEADER:
                header = (AXMLHeader) chunk;
                size = header.getSize();
                break;
            case STRING_SECTION:
                stringSection = (StringSection) chunk;
                break;
            // operational = true;
            case RESOURCE_SECTION:
                resourceSection = (ResourceSection) chunk;
                break;
            case START_NAMESPACE:
            case END_NAMESPACE:
            case START_TAG:
            case END_TAG:
            case TEXT_TAG:
                chunks.add(chunk);
                break;
            case BUFFER:
                // Do nothing right now, not even add it to the chunk stuff
                break;
            default:
                System.out.println("Weird");
                break;
            }
        }

        if ((header != null) && (stringSection != null) && (resourceSection != null)) {
            if (header.getSize() != reader.getBytesRead()) {
                System.out.println("Potential issue as the bytes read is not equal to the amount of bytes in the file");
            }
            return true;
        }

        return false;
    }

    public void print() {

        log("%s", header.toXML(stringSection, resourceSection, 0));
        Iterator<Chunk> iterator = chunks.iterator();
        int indents = 0;
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            if (chunk.getChunkType() == ChunkType.END_TAG) {
                indents--;
            }
            // if(chunk.getChunkType() == ChunkType.START_NAMESPACE)

            log("%s", chunk.toXML(stringSection, resourceSection, indents));

            if (chunk.getChunkType() == ChunkType.START_TAG) {
                indents++;
            }
        }
    }

    private static void log(String format, Object... arguments) {
        System.out.printf(format, arguments);
        System.out.println();
    }
}