/*
 * ACS AEM Commons
 *
 * Copyright (C) 2013 - 2023 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.acs.commons.analysis.jcrchecksum.impl.servlets;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class JSONDumpServletTest {

    @Rule
    public final SlingContext context = new SlingContext(ResourceResolverType.JCR_MOCK);

    private static final String SERVLET_PATH = "/bin/acs-commons/jcr-compare";

    private static final String SERVLET_SELECTORS = "dump";

    private static final String SERVLET_EXTENSION = "json";

    @InjectMocks
    public JSONDumpServlet servlet = new JSONDumpServlet();

    @Before
    public void setUp() throws LoginException {
    }

    @Test
    public void testWithNoPath() throws Exception {
        ResourceResolver resourceResolver = context.resourceResolver();
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setResource(resourceResolver.getResource(SERVLET_PATH));
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
        requestPathInfo.setSelectorString(SERVLET_SELECTORS);
        requestPathInfo.setExtension(SERVLET_EXTENSION);
        request.setMethod("GET");

        MockSlingHttpServletResponse response = new MockSlingHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("ERROR: At least one path must be specified", response
                .getOutputAsString());
    }
}
