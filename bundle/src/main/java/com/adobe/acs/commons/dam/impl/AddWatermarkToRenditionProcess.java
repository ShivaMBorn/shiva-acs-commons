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
package com.adobe.acs.commons.dam.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.adobe.acs.commons.util.RequireAem;
import com.adobe.acs.commons.util.WorkflowHelper;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.dam.AbstractRenditionModifyingProcess;
import com.day.cq.dam.api.Rendition;
import com.day.cq.workflow.WorkflowSession;
import com.day.image.Layer;

/**
 * Workflow process which adds a watermark to a rendition. Watermarks will always be anchored in the bottom left corner.
 * 
 * Arguments:
 * <ul>
 * <li>watermark - The repository path of the watermark.</li>
 * </ul>
 */
@Component
@Service
@Property(name = "process.label", value = "Add Watermark to Rendition")
public final class AddWatermarkToRenditionProcess extends AbstractRenditionModifyingProcess implements WorkflowProcess {

    private static ConcurrentMap<String, Object> watermarkLogCache = new ConcurrentHashMap<String, Object>();

    // Disable this feature on AEM as a Cloud Service
    @Reference(target="(distribution=classic)")
    RequireAem requireAem;

    @Reference
    private WorkflowHelper workflowHelper;

    @Reference
    private MimeTypeService mimeTypeService;

    private static void logMissingWatermark(final String path) {
        if (watermarkLogCache.putIfAbsent(path, new Object()) == null) {
            log.warn("Watermark path {} is not found.", path);
        }
    }

    private static void logInvalidWatermark(final String path) {
        if (watermarkLogCache.putIfAbsent(path, new Object()) == null) {
            log.warn("Watermark path {} is not valid.", path);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AddWatermarkToRenditionProcess.class);

    private static final String ARG_WATERMARK = "watermark";

    private static final String WATERMARK_SPECIFIER = ARG_WATERMARK;

    private void addWatermark(Layer layer, Layer watermark) {
        layer.blit(watermark, 0, layer.getHeight() - watermark.getHeight(), watermark.getWidth(),
                watermark.getHeight(), 0, 0);
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        execute(workItem, workflowSession, metaDataMap, workflowHelper);
    }

    private Layer getLayer(String path, WorkflowSession session) throws LoginException {
        if (path != null) {
            ResourceResolver resolver = workflowHelper.getResourceResolver(session);
            Resource resource = resolver.getResource(path);
            if (resource != null) {
                Layer layer = resource.adaptTo(Layer.class);
                if (layer != null) {
                    return layer;
                } else {
                    logInvalidWatermark(path);
                }
            } else {
                logMissingWatermark(path);
            }
        }
        return null;
    }

    @Override
    protected String getTempFileSpecifier() {
        return WATERMARK_SPECIFIER;
    }

    @Override
    protected Layer processLayer(Layer layer, Rendition rendition, WorkflowSession workflowSession, String[] args) {
        final String watermarkPath = workflowHelper.getValuesFromArgs(ARG_WATERMARK, args).size() > 0 ? workflowHelper.getValuesFromArgs(
                ARG_WATERMARK, args).get(0) : null;

        if (watermarkPath != null) {
            Layer watermark = null;
            try {
                watermark = getLayer(watermarkPath, workflowSession);

                if (watermark != null) {
                    addWatermark(layer, watermark);
                }
            } catch (LoginException e) {
                log.error("Unable to log into repository.", e);
            } finally {
                if (watermark != null) {
                    watermark.dispose();
                    watermark = null;
                }
            }
        } else {
            log.info("No watermark specified. Skipping.");
        }
        return layer;
    }

}
