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
package com.adobe.acs.commons.httpcache.exception;

/**
 * Custom exception representing a conflict in resolving cache config.
 */
@SuppressWarnings({"serial", "squid:S2166"})
public class HttpCacheConfigConflictException extends HttpCacheException {
    public HttpCacheConfigConflictException() {
    }

    public HttpCacheConfigConflictException(String message) {
        super(message);
    }

    public HttpCacheConfigConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpCacheConfigConflictException(Throwable cause) {
        super(cause);
    }

    public HttpCacheConfigConflictException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
