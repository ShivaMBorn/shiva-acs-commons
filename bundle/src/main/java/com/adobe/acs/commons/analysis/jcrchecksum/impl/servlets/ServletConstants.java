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

public final class ServletConstants {

    private ServletConstants() {
        // Private cstor
    }

    public static final String SERVLET_PATH = "/bin/acs-commons/jcr-compare";

    public static final String CHECKSUM_SERVLET_SELECTOR = "hashes";

    public static final String CHECKSUM_SERVLET_EXTENSION = "txt";

    public static final String JSON_SERVLET_SELECTOR = "dump";

    public static final String JSON_SERVLET_EXTENSION = "json";

    public static final String OPTIONS_NAME = "optionsName";
}
