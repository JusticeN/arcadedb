import com.arcadedb.database.*;
import com.arcadedb.engine.PFile;
import com.arcadedb.exception.PDatabaseIsReadOnlyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CRUDTransactionTest {
    private static final int TOT = 10000;

    @BeforeEach
    public void populate() {
        populate(TOT);
    }

    @AfterEach
    public void drop() {
        final PDatabase db = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_WRITE).acquire();
        db.drop();
    }

    @Test
    public void testPopulate() {
    }

    @Test
    public void testScan() {
        final AtomicInteger total = new AtomicInteger();

        final PDatabase db = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_ONLY).acquire();
        db.begin();
        try {
            db.scanBucket("V", new PRecordCallback() {
                @Override
                public boolean onRecord(final PRecord record) {
                    Assertions.assertNotNull(record);

                    Set<String> prop = new HashSet<String>();
                    for (String p : record.getPropertyNames())
                        prop.add(p);

                    Assertions.assertEquals(3, record.getPropertyNames().size(), 9);
                    Assertions.assertTrue(prop.contains("id"));
                    Assertions.assertTrue(prop.contains("name"));
                    Assertions.assertTrue(prop.contains("surname"));

                    total.incrementAndGet();
                    return true;
                }
            });

            Assertions.assertEquals(TOT, total.get());

            db.commit();

        } finally {
            db.close();
        }
    }

    @Test
    public void testLookupAllRecords() {
        final AtomicInteger total = new AtomicInteger();

        final PDatabase db = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_ONLY).acquire();
        db.begin();
        try {
            db.scanBucket("V", new PRecordCallback() {
                @Override
                public boolean onRecord(final PRecord record) {
                    final PRecord record2 = db.lookupByRID(record.getIdentity());
                    Assertions.assertNotNull(record2);
                    Assertions.assertEquals(record, record2);

                    Set<String> prop = new HashSet<String>();
                    for (String p : record2.getPropertyNames())
                        prop.add(p);

                    Assertions.assertEquals(record2.getPropertyNames().size(), 3);
                    Assertions.assertTrue(prop.contains("id"));
                    Assertions.assertTrue(prop.contains("name"));
                    Assertions.assertTrue(prop.contains("surname"));

                    total.incrementAndGet();
                    return true;
                }
            });

            db.commit();

        } finally {
            db.close();
        }

        Assertions.assertEquals(TOT, total.get());
    }

    @Test
    public void testDeleteAllRecordsReuseSpace() throws IOException {
        final AtomicInteger total = new AtomicInteger();

        final PDatabase db = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_WRITE).acquire();
        db.begin();
        try {
            db.scanBucket("V", new PRecordCallback() {
                @Override
                public boolean onRecord(final PRecord record) {
                    db.deleteRecord(record.getIdentity());
                    total.incrementAndGet();
                    return true;
                }
            });

            db.commit();

        } finally {
            db.close();
        }

        Assertions.assertEquals(TOT, total.get());

        populate();

        final PDatabase db2 = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_WRITE).acquire();
        db2.begin();
        try {
            Assertions.assertEquals(TOT, db2.countBucket("V"));
            db2.commit();
        } finally {
            db2.close();
        }
    }

    @Test
    public void testDeleteFail() {

        Assertions.assertThrows(PDatabaseIsReadOnlyException.class, () -> {
            final PDatabase db = new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_ONLY).acquire();
            db.begin();
            try {
                db.scanBucket("V", new PRecordCallback() {
                    @Override
                    public boolean onRecord(final PRecord record) {
                        db.deleteRecord(record.getIdentity());
                        return true;
                    }
                });

                db.commit();

            } finally {
                db.close();
            }
        });
    }

    private void populate(final int total) {
        new PDatabaseFactory("/temp/proton/testdb", PFile.MODE.READ_WRITE).execute(new PDatabaseFactory.POperation() {
            @Override
            public void execute(PDatabase database) {
                if (!database.getSchema().existsBucket("V"))
                    database.getSchema().createBucket("V");

                for (int i = 0; i < total; ++i) {
                    final PModifiableDocument v = database.newDocument();
                    v.set("id", i);
                    v.set("name", "Jay");
                    v.set("surname", "Miner");

                    v.save("V");
                }
            }
        });
    }
}