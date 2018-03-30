package performance;

import com.arcadedb.database.*;
import com.arcadedb.engine.PPaginatedFile;

import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceScan {
  private static final String CLASS_NAME = "Person";
  private static final int    MAX_LOOPS  = 1;

  public static void main(String[] args) throws Exception {
    new PerformanceScan().run();
  }

  private void run() {
    final PDatabase database = new PDatabaseFactory(PerformanceTest.DATABASE_PATH, PPaginatedFile.MODE.READ_ONLY).acquire();

    database.asynch().setParallelLevel(4);

    try {
      for (int i = 0; i < MAX_LOOPS; ++i) {
        final long begin = System.currentTimeMillis();

        final AtomicInteger row = new AtomicInteger();

        database.asynch().scanType(CLASS_NAME, new PDocumentCallback() {
          @Override
          public boolean onRecord(final PDocument record) {
            final PImmutableDocument document = ((PImmutableDocument) record);

            document.get("id");

            if (row.incrementAndGet() % 10000000 == 0)
              System.out.println("- Scanned " + row.get() + " elements in " + (System.currentTimeMillis() - begin) + "ms");

            return true;
          }
        });

        System.out.println("Found " + row.get() + " elements in " + (System.currentTimeMillis() - begin) + "ms");
      }
    } finally {
      database.close();
    }
  }
}