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

import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

/**
 * Private interface for exposing ThrottledTaskRunner stats
 * **/
public interface ThrottledTaskRunnerStats {
    /**
     * @return the % of CPU being utilized.
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     */
    double getCpuLevel() throws InstanceNotFoundException, ReflectionException;

    /**
     * The % of memory being utilized.
     * @return
     */
    double getMemoryUsage();

    /***
     * @return the OSGi configured max allowed CPU utilization.
     */
    double getMaxCpu();

    /***
     * @return the OSGi configured max allowed Memory (heap) utilization.
     */
    double getMaxHeap();

    /**
     * @return the max number of threads ThrottledTaskRunner will use to execute the work.
     */
    int getMaxThreads();
}