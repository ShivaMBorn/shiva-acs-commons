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
package com.adobe.acs.commons.fam.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A runnable future for {@link TimedRunnable} which implements comparable for
 * the purpsoe of priority execution.
 */
public class TimedRunnableFuture extends FutureTask implements Comparable<TimedRunnableFuture> {

    private TimedRunnable timedRunnable;

    /**
     * {@inheritDoc}
     */
    public TimedRunnableFuture(Callable callable) {
        super(callable);
    }

    /**
     * {@inheritDoc}
     */
    public TimedRunnableFuture(Runnable runnable, Object result) {
        super(runnable, result);
        if (runnable instanceof TimedRunnable) {
            timedRunnable = (TimedRunnable) runnable;
        }
    }

    @Override
    public int compareTo(TimedRunnableFuture other) {
        TimedRunnable otherTimedRunnable = other.timedRunnable;
        if (otherTimedRunnable != null && timedRunnable != null) {
            return timedRunnable.compareTo(otherTimedRunnable);
        } else {
            return 0;
        }
    }
}
