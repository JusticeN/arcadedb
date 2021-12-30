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
import com.arcadedb.query.sql.function.text.SQLMethodLike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SQLMethodLikeTest {

    private SQLMethod method;

    @BeforeEach
    void setUp() {
        method = new SQLMethodLike();
    }

    @Test
    void testNull() {
        Object result = method.execute(null, null, null, null, null);
        assertThat(result).isNull();

        result = method.execute("Arcadedb", null, null, null, null);
        assertThat(result).isNull();

        result = method.execute(null, null, null, null, new Object[]{"db"});
        assertThat(result).isNull();
    }

    @Test
    void testEmptyLike() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{""});
        checkResult(result, false);
    }
    @Test
    void testLikeWithRegexDotStar() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{".*"});
        checkResult(result, true);

        result = method.execute("Arcadedb", null, null, null, new Object[]{"Arcade.*"});
        checkResult(result, true);
    }
    @Test
    void testLikeWithIncorrectRegexDotStar() {
        Object result = method.execute("Arcadedb", null, null, null, new Object[]{"graph.*"});
        checkResult(result, false);
    }

    private void checkResult(Object result, boolean check) {
        assertThat(result).isInstanceOf(Boolean.class);
        boolean boolResult = (Boolean)result;
        if (check) assertThat(boolResult).isTrue();
        else assertThat(boolResult).isFalse();
    }

}
