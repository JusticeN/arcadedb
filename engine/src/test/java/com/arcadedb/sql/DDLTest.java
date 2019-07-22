package com.arcadedb.sql;

import com.arcadedb.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class DDLTest extends BaseTest {
    @Override
    protected void beginTest() {

        database.transaction(db -> {
            db.command("sql", "CREATE VERTEX TYPE V");
            db.command("sql", "CREATE EDGE TYPE E");
        });

    }

    @Test
    void testGraphWithSql() {

        final int numOfElements = 10;
        //create schema: sript
        database.execute("sql",
                "BEGIN;" +
                        "CREATE VERTEX TYPE Person EXTENDS V; " +
                        "CREATE PROPERTY Person.name STRING;" +
                        "CREATE PROPERTY Person.id INTEGER;" +
                        "CREATE INDEX Person.id ON Person (id) UNIQUE;" +
                        "CREATE VERTEX TYPE Car EXTENDS V; " +
                        "CREATE PROPERTY Car.id INTEGER;" +
                        "CREATE PROPERTY Car.model STRING;" +
                        "CREATE INDEX Car.id ON Car (id) UNIQUE;" +
                        "CREATE EDGE TYPE Drives EXTENDS E;" +
                        "COMMIT;  " +
                        "");

        //vertices
        database.transaction(db -> {

            IntStream.range(0, numOfElements)
                    .forEach(i -> {
                        db.command("sql", "INSERT INTO Person set id=?,  name=?, surname=?", i, "Jay", "Miner" + i);
                        db.command("sql", "INSERT INTO Car set id=?,  brand=?, model=?", i, "Ferrari", "450" + i);
                    });


        });
        //edges
        database.transaction(db -> {

            IntStream.range(0, numOfElements)
                    .forEach(i -> {
                        db.command("sql", "CREATE EDGE Drives FROM (SELECT FROM Person WHERE id=?) TO (SELECT FROM Car WHERE id=?)", i, i);
                    });


        });

        database.transaction(db -> {

            db.query("sql", "SELECT FROM Drives")
                    .stream()
                    .map(r -> r.getEdge().get())
                    .peek(e -> assertThat(e.getIn()).isNotNull())
                    .peek(e -> assertThat(e.getOut()).isNotNull())
                    .forEach(e -> assertThat(e.getType()).isEqualTo("Drives"));
        });

        database.transaction(db -> {

            final Long persons = db.command("sql", "SELECT count(*) as persons FROM Person ")
                    .next().<Long>getProperty("persons");

            Assertions.assertEquals(numOfElements, persons);

            final Long cars = db.command("sql", "SELECT count(*) as cars FROM Car")
                    .next().<Long>getProperty("cars");

            Assertions.assertEquals(numOfElements, cars);

            final Long vs = db.command("sql", "SELECT count(*) as vs FROM V")
                    .next().<Long>getProperty("vs");

            Assertions.assertEquals(numOfElements * 2, vs);

            final Long edges = db.command("sql", "SELECT count(*) as edges FROM Drives")
                    .next().<Long>getProperty("edges");

        });

    }


}