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
package com.adobe.acs.commons.reports.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import com.adobe.acs.commons.reports.api.ReportCellCSVExporter;

/**
 * An exporter for exporting formatted string values
 */
@Model(adaptables = Resource.class)
public class PathReportCellCSVExporter implements ReportCellCSVExporter {

  @Override
  public String getValue(Object result) {
    Resource resource = (Resource) result;
    return resource.getPath();
  }
}