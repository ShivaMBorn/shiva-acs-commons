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
package com.adobe.acs.commons.mcp.form;

import org.osgi.annotation.versioning.ProviderType;
import com.adobe.acs.commons.mcp.util.DeserializeException;
import com.adobe.acs.commons.mcp.util.AnnotatedFieldDeserializer;
import javax.jcr.RepositoryException;
import org.apache.sling.api.resource.ValueMap;

/**
 * Handles a particular form of input data and deserializes the form into a bean defined with @FormField annotated fields.
 */
@ProviderType
public interface FormProcessor {
    default void parseInputs(ValueMap input) throws DeserializeException, RepositoryException {
        AnnotatedFieldDeserializer.deserializeFormFields(this, input);
        init();
    }

    void init() throws RepositoryException;
}
