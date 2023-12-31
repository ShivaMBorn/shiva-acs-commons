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
package com.adobe.acs.commons.reports.api;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsPageTest {

  private static final Logger log = LoggerFactory.getLogger(ResultsPageTest.class);

  private static final Stream<Object> RESULTS = Arrays
      .stream(new String[] { "result1", "result2", "result3", "result4" });

  @Test
  public void testGetNextPage() {
    log.info("testGetNextPage");

    ResultsPage hasMore = new ResultsPage(RESULTS, 4, 1, 4);
    assertEquals(2, hasMore.getNextPage());

    ResultsPage noMore = new ResultsPage(RESULTS, 5, 1, 4);
    assertEquals(-1, noMore.getNextPage());

    ResultsPage all = new ResultsPage(RESULTS, 4, -1, 4);
    assertEquals(-1, all.getNextPage());

    log.info("Test successful!");
  }

  @Test
  public void testGetPreviousPage() {
    log.info("testGetPreviousPage");

    ResultsPage hasPrevious = new ResultsPage(RESULTS, 4, 1, -1L);
    assertEquals(0, hasPrevious.getPreviousPage());

    ResultsPage noPrevious = new ResultsPage(RESULTS, 5, 0, -1);
    assertEquals(-1, noPrevious.getPreviousPage());

    ResultsPage all = new ResultsPage(RESULTS, 4, -1, -1);
    assertEquals(-1, all.getPreviousPage());

    log.info("Test successful!");
  }

  @Test
  public void testGetResults() {
    log.info("testGetResults");
    ResultsPage results = new ResultsPage(RESULTS, 4, 1, -1);
    assertEquals(RESULTS, results.getResults());
    log.info("Test successful!");
  }

  @Test
  public void testGetResultsList() {
    log.info("testGetResultsList");
    List<Object> resList = RESULTS.collect(Collectors.toList());
    ResultsPage results = new ResultsPage(resList.stream(), 4, 1, -1);
    assertEquals(resList, results.getResultsList());
    log.info("Test successful!");
  }

  @Test
  public void testGetResultsEnd() {
    log.info("testGetResultsEnd");

    ResultsPage hasMore = new ResultsPage(RESULTS, 4, 1, 4);
    assertEquals(8, hasMore.getResultsEnd());

    ResultsPage first = new ResultsPage(RESULTS, 5, 0, 4);
    assertEquals(4, first.getResultsEnd());

    ResultsPage all = new ResultsPage(RESULTS, 4, -1, 4);
    assertEquals(4, all.getResultsEnd());

    log.info("Test successful!");
  }

  @Test
  public void testGetResultsStart() {
    log.info("testGetResultsStart");

    ResultsPage hasPrevious = new ResultsPage(RESULTS, 4, 1, -1);
    assertEquals(5, hasPrevious.getResultsStart());

    ResultsPage noPrevious = new ResultsPage(RESULTS, 4, 0, -1);
    assertEquals(1, noPrevious.getResultsStart());

    ResultsPage all = new ResultsPage(RESULTS, 4, -1, -1);
    assertEquals(1, all.getResultsStart());

    log.info("Test successful!");
  }
}
