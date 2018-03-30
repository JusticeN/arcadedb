package com.arcadedb.sql.executor;

import com.arcadedb.database.PDocument;
import com.arcadedb.database.PIdentifiable;
import com.arcadedb.database.PRecord;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Expands a result-set.
 * The pre-requisite is that the input element contains only one field (no matter the name)
 */
public class ExpandStep extends AbstractExecutionStep {

  private long cost = 0;

  OResultSet lastResult      = null;
  Iterator   nextSubsequence = null;
  OResult    nextElement     = null;

  public ExpandStep(OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    if (prev == null || !prev.isPresent()) {
      throw new PCommandExecutionException("Cannot expand without a target");
    }
    return new OResultSet() {
      long localCount = 0;

      @Override
      public boolean hasNext() {
        if (localCount >= nRecords) {
          return false;
        }
        if (nextElement == null) {
          fetchNext(ctx, nRecords);
        }
        if (nextElement == null) {
          return false;
        }
        return true;
      }

      @Override
      public OResult next() {
        if (localCount >= nRecords) {
          throw new IllegalStateException();
        }
        if (nextElement == null) {
          fetchNext(ctx, nRecords);
        }
        if (nextElement == null) {
          throw new IllegalStateException();
        }

        OResult result = nextElement;
        localCount++;
        nextElement = null;
        fetchNext(ctx, nRecords);
        return result;
      }

      @Override
      public void close() {

      }

      @Override
      public Optional<OExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override
      public Map<String, Long> getQueryStats() {
        return null;
      }
    };
  }

  private void fetchNext(OCommandContext ctx, int n) {
    do {
      if (nextSubsequence != null && nextSubsequence.hasNext()) {
        long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
          Object nextElementObj = nextSubsequence.next();
          if (nextElementObj instanceof OResult) {
            nextElement = (OResult) nextElementObj;
          } else if (nextElementObj instanceof PIdentifiable) {
            PRecord record = ((PIdentifiable) nextElementObj).getRecord();
            if (record == null) {
              continue;
            }
            nextElement = new OResultInternal();
            ((OResultInternal) nextElement).setElement((PDocument) record);
          } else {
            nextElement = new OResultInternal();
            ((OResultInternal) nextElement).setProperty("value", nextElementObj);
          }
          break;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
        }
      }

      if (nextSubsequence == null || !nextSubsequence.hasNext()) {
        if (lastResult == null || !lastResult.hasNext()) {
          lastResult = getPrev().get().syncPull(ctx, n);
        }
        if (!lastResult.hasNext()) {
          return;
        }
      }

      OResult nextAggregateItem = lastResult.next();
      long begin = profilingEnabled ? System.nanoTime() : 0;
      try {
        if (nextAggregateItem.getPropertyNames().size() == 0) {
          continue;
        }
        if (nextAggregateItem.getPropertyNames().size() > 1) {
          throw new IllegalStateException("Invalid EXPAND on record " + nextAggregateItem);
        }

        String propName = nextAggregateItem.getPropertyNames().iterator().next();
        Object projValue = nextAggregateItem.getProperty(propName);
        if (projValue == null) {
          continue;
        }
        if (projValue instanceof PIdentifiable) {
          PRecord rec = ((PIdentifiable) projValue).getRecord();
          if (rec == null) {
            continue;
          }
          OResultInternal res = new OResultInternal();
          res.setElement((PDocument) rec);

          nextSubsequence = Collections.singleton(res).iterator();
        } else if (projValue instanceof OResult) {
          nextSubsequence = Collections.singleton((OResult) projValue).iterator();
        } else if (projValue instanceof Iterator) {
          nextSubsequence = (Iterator) projValue;
        } else if (projValue instanceof Iterable) {
          nextSubsequence = ((Iterable) projValue).iterator();
        }
      } finally {
        if (profilingEnabled) {
          cost += (System.nanoTime() - begin);
        }
      }
    } while (true);

  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    String result = spaces + "+ EXPAND";
    if (profilingEnabled) {
      result += " (" + getCostFormatted() + ")";
    }
    return result;
  }

  @Override
  public long getCost() {
    return cost;
  }
}
