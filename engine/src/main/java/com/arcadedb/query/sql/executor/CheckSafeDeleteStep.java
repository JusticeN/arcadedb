/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
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
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arcadedb.query.sql.executor;

import com.arcadedb.exception.TimeoutException;

/**
 * <p>
 * Checks if a record can be safely deleted (throws PCommandExecutionException in case).
 * A record cannot be safely deleted if it's a vertex or an edge (it requires additional operations).</p>
 * <p>
 * The result set returned by syncPull() throws an PCommandExecutionException as soon as it finds a record
 * that cannot be safely deleted (eg. a vertex or an edge)</p>
 * <p>This step is used in DELETE statement to make sure that you are not deleting vertices or edges without passing for an
 * explicit DELETE VERTEX/EDGE</p>
 *
 * @author Luigi Dell'Aquila (luigi.dellaquila-(at)-gmail.com)
 */
public class CheckSafeDeleteStep extends AbstractExecutionStep {

  public CheckSafeDeleteStep(final CommandContext context, final boolean profilingEnabled) {
    super(context, profilingEnabled);
  }

  @Override
  public ResultSet syncPull(final CommandContext context, final int nRecords) throws TimeoutException {
    final ResultSet upstream = getPrev().syncPull(context, nRecords);
    return new ResultSet() {
      @Override
      public boolean hasNext() {
        return upstream.hasNext();
      }

      @Override
      public Result next() {
        final Result result = upstream.next();
        final long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
//          if (result.isElement()) {
//
//            Record record = result.getElement().get();
//            if (record instanceof Document) {
          //TODO
//              Document doc = (Document) record;
//              DocumentType typez = doc.getType();
//              if (typez != null) {
//                if (typez.getName().equalsIgnoreCase("V") || typez.isSubClassOf("V")) {
//                  throw new PCommandExecutionException("Cannot safely delete a vertex, please use DELETE VERTEX or UNSAFE");
//                }
//                if (typez.getName().equalsIgnoreCase("E") || typez.isSubClassOf("E")) {
//                  throw new PCommandExecutionException("Cannot safely delete an edge, please use DELETE EDGE or UNSAFE");
//                }
//              }
//            }
//          }
          return result;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
        }
      }

    };
  }

  @Override
  public String prettyPrint(final int depth, final int indent) {
    final String spaces = ExecutionStepInternal.getIndent(depth, indent);
    final StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ CHECK SAFE DELETE");
    if (profilingEnabled) {
      result.append(" (").append(getCostFormatted()).append(")");
    }
    return result.toString();
  }

}
