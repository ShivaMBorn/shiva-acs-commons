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

package com.adobe.acs.commons.workflow.synthetic.impl;

import com.adobe.acs.commons.workflow.synthetic.SyntheticWorkflowRunner;
import com.adobe.acs.commons.workflow.synthetic.SyntheticWorkflowStep;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.NoNextWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.ReadDataWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.RestartWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.SetDataWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.TerminateDataWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.UpdateWorkflowDataWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.WfArgsWorkflowProcess;
import com.adobe.acs.commons.workflow.synthetic.impl.cqtestprocesses.WfDataWorkflowProcess;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyntheticCqWorkflowRunnerImplTest {

    @Mock
    ResourceResolver resourceResolver;

    @Mock
    Session session;

    SyntheticWorkflowRunnerImpl swr = new SyntheticWorkflowRunnerImpl();

    List<SyntheticWorkflowStep> workflowSteps;

    @Before
    public void setUp() {
        workflowSteps = new ArrayList<>();
        when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    }

    @Test
    public void testExecute_WfData() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "test");
        swr.bindCqWorkflowProcesses(new WfDataWorkflowProcess(), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("test",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, false);
    }

    @Test
    public void testExecute_PassingDataBetweenProcesses() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "set");
        swr.bindCqWorkflowProcesses(new SetDataWorkflowProcess(), map);

        map.put("process.label", "read");
        swr.bindCqWorkflowProcesses(new ReadDataWorkflowProcess(), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("set",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));
        workflowSteps.add(swr.getSyntheticWorkflowStep("read",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
               workflowSteps, false, false);
    }

    @Test
    public void testExecute_updateWorkflowData() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "update");
        swr.bindCqWorkflowProcesses(new UpdateWorkflowDataWorkflowProcess(), map);

        map.put("process.label", "read");
        swr.bindCqWorkflowProcesses(new ReadDataWorkflowProcess(), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("update",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));
        workflowSteps.add(swr.getSyntheticWorkflowStep("read",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, false);
    }

    @Test
    public void testExecute_ProcessArgs() throws Exception {
        /** WF Process Metadata */
        Map<String, Object> wfArgs = new HashMap<String, Object>();
        wfArgs.put("hello", "world");

        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("process.label", "wf-args");
        swr.bindCqWorkflowProcesses(new WfArgsWorkflowProcess(wfArgs), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("wf-args",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL,
                wfArgs));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, false);
    }

    @Test
    public void testExecute_Restart() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "restart");
        RestartWorkflowProcess restartWorkflowProcess = spy(new RestartWorkflowProcess());
        swr.bindCqWorkflowProcesses(restartWorkflowProcess, map);

        /** Restart */
        workflowSteps.add(swr.getSyntheticWorkflowStep("restart",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, false);

        verify(restartWorkflowProcess, times(3)).execute(any(WorkItem.class), any(WorkflowSession.class),
                any(MetaDataMap.class));

    }

    @Test
    public void testExecute_Terminate() throws Exception {

        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "terminate");
        TerminateDataWorkflowProcess terminateDataWorkflowProcess = spy(new TerminateDataWorkflowProcess());
        swr.bindCqWorkflowProcesses(terminateDataWorkflowProcess, map);

        map.put("process.label", "nonext");
        swr.bindCqWorkflowProcesses(new NoNextWorkflowProcess(), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("terminate",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));
        workflowSteps.add(swr.getSyntheticWorkflowStep("nonext",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, true, false);
    }


    @Test
    public void testExecute_Terminate_autoSaveAtEnd() throws Exception {
        when(session.hasPendingChanges()).thenReturn(true).thenReturn(false);

        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "terminate");
        TerminateDataWorkflowProcess terminateDataWorkflowProcess = spy(new TerminateDataWorkflowProcess());
        swr.bindCqWorkflowProcesses(terminateDataWorkflowProcess, map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("terminate",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, true);

        verify(terminateDataWorkflowProcess, times(1)).execute(any(WorkItem.class), any(WorkflowSession.class),
                any(MetaDataMap.class));

        verify(session, times(1)).save();
    }

    @Test
    public void testExecute_Complete_noSave() throws Exception {

        Map<Object, Object> map = new HashMap<Object, Object>();

        map.put("process.label", "terminate");
        TerminateDataWorkflowProcess terminateDataWorkflowProcess = spy(new TerminateDataWorkflowProcess());
        swr.bindCqWorkflowProcesses(terminateDataWorkflowProcess, map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("terminate",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps, false, false);

        verify(terminateDataWorkflowProcess, times(1)).execute(any(WorkItem.class), any(WorkflowSession.class),
                any(MetaDataMap.class));

        verify(session, times(0)).save();
    }

    @Test
    public void testExecute_MultipleProcesses() throws Exception {
        Map<String, Object> wfArgs1 = new HashMap<String, Object>();
        wfArgs1.put("hello", "world");

        Map<String, Object> wfArgs2 = new HashMap<String, Object>();
        wfArgs2.put("goodbye", "moon");

        final Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("process.label", "multi1");
        swr.bindCqWorkflowProcesses(new WfArgsWorkflowProcess(wfArgs1), map);

        map.put("process.label", "multi2");
        swr.bindCqWorkflowProcesses(new WfArgsWorkflowProcess(wfArgs2), map);

        workflowSteps.add(swr.getSyntheticWorkflowStep("multi1",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL,
                wfArgs1));
        workflowSteps.add(swr.getSyntheticWorkflowStep("multi2",
                SyntheticWorkflowRunner.WorkflowProcessIdType.PROCESS_LABEL,
                wfArgs2));

        swr.execute(resourceResolver,
                "/content/test",
                workflowSteps,
                false,
                false);
    }
}