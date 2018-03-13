package com.arcadedb.serializer;

import com.arcadedb.database.*;
import com.arcadedb.engine.PBucket;
import com.arcadedb.engine.PFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Map;

public class PSerializerTest {

    @Test
    public void testVarNumber() {
        final PBinary binary = new PBinary();
        binary.putNumber(0);
        binary.putNumber(3);
        binary.putNumber(Short.MIN_VALUE);
        binary.putNumber(Short.MAX_VALUE);
        binary.putNumber(Integer.MIN_VALUE);
        binary.putNumber(Integer.MAX_VALUE);
        binary.putNumber(Long.MIN_VALUE);
        binary.putNumber(Long.MAX_VALUE);

        binary.putShort((short) 0);
        binary.putShort(Short.MIN_VALUE);
        binary.putShort(Short.MAX_VALUE);

        binary.putInt(0);
        binary.putInt(Integer.MIN_VALUE);
        binary.putInt(Integer.MAX_VALUE);

        binary.putLong(0l);
        binary.putLong(Long.MIN_VALUE);
        binary.putLong(Long.MAX_VALUE);

        binary.flip();

        final ByteBuffer dBuffer = ByteBuffer.allocate(1024);
        final PBinary buffer = new PBinary(dBuffer);
        dBuffer.put(binary.toByteArray());

        binary.position(0);
        buffer.position(0);

        Assertions.assertEquals(0, binary.getNumber());
        Assertions.assertEquals(0, buffer.getNumber());

        Assertions.assertEquals(3, binary.getNumber());
        Assertions.assertEquals(3, buffer.getNumber());

        Assertions.assertEquals(Short.MIN_VALUE, binary.getNumber());
        Assertions.assertEquals(Short.MIN_VALUE, buffer.getNumber());

        Assertions.assertEquals(Short.MAX_VALUE, binary.getNumber());
        Assertions.assertEquals(Short.MAX_VALUE, buffer.getNumber());

        Assertions.assertEquals(Integer.MIN_VALUE, binary.getNumber());
        Assertions.assertEquals(Integer.MIN_VALUE, buffer.getNumber());

        Assertions.assertEquals(Integer.MAX_VALUE, binary.getNumber());
        Assertions.assertEquals(Integer.MAX_VALUE, buffer.getNumber());

        Assertions.assertEquals(Long.MIN_VALUE, binary.getNumber());
        Assertions.assertEquals(Long.MIN_VALUE, buffer.getNumber());

        Assertions.assertEquals(Long.MAX_VALUE, binary.getNumber());
        Assertions.assertEquals(Long.MAX_VALUE, buffer.getNumber());

        Assertions.assertEquals(0, binary.getShort());
        Assertions.assertEquals(0, buffer.getShort());

        Assertions.assertEquals(Short.MIN_VALUE, binary.getShort());
        Assertions.assertEquals(Short.MIN_VALUE, buffer.getShort());

        Assertions.assertEquals(Short.MAX_VALUE, binary.getShort());
        Assertions.assertEquals(Short.MAX_VALUE, buffer.getShort());

        Assertions.assertEquals(0, binary.getInt());
        Assertions.assertEquals(0, buffer.getInt());

        Assertions.assertEquals(Integer.MIN_VALUE, binary.getInt());
        Assertions.assertEquals(Integer.MIN_VALUE, buffer.getInt());

        Assertions.assertEquals(Integer.MAX_VALUE, binary.getInt());
        Assertions.assertEquals(Integer.MAX_VALUE, buffer.getInt());

        Assertions.assertEquals(0l, binary.getLong());
        Assertions.assertEquals(0l, buffer.getLong());

        Assertions.assertEquals(Long.MIN_VALUE, binary.getLong());
        Assertions.assertEquals(Long.MIN_VALUE, buffer.getLong());

        Assertions.assertEquals(Long.MAX_VALUE, binary.getLong());
        Assertions.assertEquals(Long.MAX_VALUE, buffer.getLong());
    }

    @Test
    public void testPropertiesInDocument() {
        final PBinarySerializer serializer = new PBinarySerializer();

        new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_WRITE).execute(new PDatabaseFactory.POperation() {
            @Override
            public void execute(PDatabase database) {
                final PModifiableDocument v = database.newDocument();
                v.set("id", 0);
                v.set("idLong", Long.MAX_VALUE);
                v.set("name", "Jay");
                v.set("surname", "Miner");

                final PBinary buffer = serializer.serialize(database, v);

                final ByteBuffer buffer2 = ByteBuffer.allocate(PBucket.DEF_PAGE_SIZE);
                buffer2.put(buffer.toByteArray());
                buffer2.flip();

                PBinary buffer3 = new PBinary(buffer2);
                Map<String, Object> record2 = serializer.deserializeFields((PDatabaseImpl) database, buffer3);

                Assertions.assertEquals(4, record2.size());
                Assertions.assertEquals(0, record2.get("id"));
                Assertions.assertEquals(Long.MAX_VALUE, record2.get("idLong"));
                Assertions.assertEquals("Jay", record2.get("name"), "Jay");
                Assertions.assertEquals("Miner", record2.get("surname"));
            }
        });
    }
}