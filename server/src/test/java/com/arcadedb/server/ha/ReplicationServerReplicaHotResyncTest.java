/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server.ha;

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.server.ArcadeDBServer;
import com.arcadedb.server.TestCallback;
import com.arcadedb.utility.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicLong;

public class ReplicationServerReplicaHotResyncTest extends ReplicationServerTest {
  private final    AtomicLong totalMessages = new AtomicLong();
  private volatile boolean    slowDown      = true;
  private          boolean    hotResync     = false;
  private          boolean    fullResync    = false;

  public ReplicationServerReplicaHotResyncTest() {
    GlobalConfiguration.HA_QUORUM.setValue("MAJORITY");
    GlobalConfiguration.HA_REPLICATION_QUEUE_SIZE.setValue(10);
  }

  @AfterEach
  @Override
  public void endTest() {
    super.endTest();
    GlobalConfiguration.HA_REPLICATION_QUEUE_SIZE.setValue(512);
  }

  @Override
  protected void onAfterTest() {
    Assertions.assertTrue(hotResync);
    Assertions.assertFalse(fullResync);
  }

  @Override
  protected void onBeforeStarting(final ArcadeDBServer server) {
    if (server.getServerName().equals("ArcadeDB_2"))
      server.registerTestEventListener(new TestCallback() {
        @Override
        public void onEvent(final TYPE type, final Object object, final ArcadeDBServer server) {
          if (slowDown) {
            // SLOW DOWN A SERVER AFTER 5TH MESSAGE
            if (totalMessages.incrementAndGet() > 5) {
              try {
                LogManager.instance().info(this, "TEST: Slowing down response from replica server 2...");
                Thread.sleep(10000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          } else {
            if (type == TYPE.REPLICA_HOT_RESYNC) {
              LogManager.instance().info(this, "TEST: Received hot resync request");
              hotResync = true;
            } else if (type == TYPE.REPLICA_FULL_RESYNC) {
              LogManager.instance().info(this, "TEST: Received full resync request");
              fullResync = true;
            }
          }
        }
      });

    if (server.getServerName().equals("ArcadeDB_0"))
      server.registerTestEventListener(new TestCallback() {
        @Override
        public void onEvent(final TYPE type, final Object object, final ArcadeDBServer server) {
          // SLOW DOWN A SERVER
          if ("ArcadeDB_2".equals(object) && type == TYPE.REPLICA_OFFLINE) {
            LogManager.instance().info(this, "TEST: Replica 2 is offline removing latency...");
            slowDown = false;
          }
        }
      });
  }
}