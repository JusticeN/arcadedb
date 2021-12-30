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
 */
package com.arcadedb.query.sql.function.text;

import com.arcadedb.query.sql.executor.SQLMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SQLMethodReplaceTest {

    private SQLMethod method;

    @BeforeEach
    void setUp() {
        method = new SQLMethodReplace();
    }

    @Test
    void testNull() {
        Object result = method.execute(null, null, null, null, null);
        assertThat(result).isNull();

        result = method.execute(null, null, null, null, new Object[]{null, null});
        assertThat(result).isNull();
    }

    @Test
    void testEmpty() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{"", ""});
        checkResult(result, "Arcadedb");

        result = method.execute("Arcadedb", null, null, null, new Object[]{"", "---"});
        checkResult(result, "---A---r---c---a---d---e---d---b---");
    }

    @Test
    void testReplace() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{"db", "-graph"});
        checkResult(result, "Arcade-graph");

        result = method.execute("Arcadedb", null, null, null, new Object[]{"NotExist", "---"});
        checkResult(result, "Arcadedb");

    }


    @Test
    void testReplaceWithRegex() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{"db.*", " db"});
        checkResult(result, "Arcade db");

        result = method.execute("Database is cool", null, null, null, new Object[]{"[\\w]{8}", "Arcadedb"});
        checkResult(result, "Arcadedb is cool");
    }

    private void checkResult(Object result, String check) {
        assertThat(result).isInstanceOf(String.class);
        String stringResult = result.toString();
        assertThat(stringResult).isEqualTo(check);
    }

}
