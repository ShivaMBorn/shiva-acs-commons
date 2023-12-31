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
package com.adobe.acs.commons.reports.models.references;

import java.util.Set;

import org.apache.sling.api.resource.Resource;

import com.adobe.granite.references.ReferenceAggregator;
import com.adobe.granite.references.ReferenceList;

public class MockReferencesAggregator implements ReferenceAggregator {
  
  private ReferenceList referenceList;

  public MockReferencesAggregator(ReferenceList referenceList){
    this.referenceList = referenceList;
  }
  
  @Override
  public ReferenceList createReferenceList(Resource resource) {
    return referenceList;
  }

  @Override
  public ReferenceList createReferenceList(Resource resource, String... types) {
    return referenceList;
  }

  @Override
  public Set<String> getTypes() {
    return null;
  }

}
