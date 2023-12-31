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
package com.adobe.acs.commons.wcm.vanity.impl;

import com.adobe.acs.commons.wcm.vanity.WrappedRequestPathInfoWrapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

import java.lang.reflect.Proxy;

public class ExtensionlessRequestWrapper extends SlingHttpServletRequestWrapper {

    public ExtensionlessRequestWrapper(SlingHttpServletRequest wrappedRequest) {
        super(wrappedRequest);
    }

    public RequestPathInfo getRequestPathInfo() {
        final RequestPathInfoWrapper requestPathInfoWrapper = RequestPathInfoWrapper.createRequestPathInfoWrapper(super.getRequestPathInfo(), super.getResource());
        RequestPathInfo wrappedRequestInfo = (RequestPathInfo) Proxy.newProxyInstance(WrappedRequestPathInfoWrapper.class.getClassLoader(), new Class[] { RequestPathInfo.class, WrappedRequestPathInfoWrapper.class  }, requestPathInfoWrapper);
        return wrappedRequestInfo;
    }


}