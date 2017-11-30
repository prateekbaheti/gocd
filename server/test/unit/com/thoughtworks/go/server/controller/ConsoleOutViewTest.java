/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package com.thoughtworks.go.server.controller;

import com.thoughtworks.go.domain.ConsoleConsumer;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ConsoleOutViewTest {
    private static final List<String> CHARSETS = Arrays.asList("utf-8", "utf-16", "ISO-8859-7", "IBM00858", "KOI8-R", "US-ASCII");

    private String randomCharset;

    @Test
    public void setsUpContentEncoding() throws Exception {
        ConsoleOutView view = new ConsoleOutView(mock(ConsoleConsumer.class), randomCharset());

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(null, null, response);
        assertThat(response.getCharacterEncoding(), is(randomCharset()));
    }

    @Test
    public void setsUpCharset() throws Exception {
        ConsoleOutView view = new ConsoleOutView(mock(ConsoleConsumer.class), randomCharset());

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(null, null, response);
        assertThat(response.getContentType(), is(view.getContentType()));
    }

    @Test
    public void getsContentType() throws Exception {
        ConsoleOutView view = new ConsoleOutView(null, randomCharset());
        assertThat(view.getContentType(), is("text/plain; charset=" + randomCharset()));
    }

    private String randomCharset() {
        if (randomCharset == null) {
            List<String> charsets = new ArrayList<>(CHARSETS);
            Collections.shuffle(charsets);
            randomCharset = charsets.get(new Random().nextInt(charsets.size()));
        }

        return randomCharset;
    }
}