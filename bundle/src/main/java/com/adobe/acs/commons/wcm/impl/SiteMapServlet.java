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
package com.adobe.acs.commons.wcm.impl;

import com.adobe.acs.commons.util.ParameterUtil;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;

@Component(metatype = true, label = "ACS AEM Commons - Site Map Servlet", description = "Page and Asset Site Map Servlet", configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Service
@SuppressWarnings("serial")
@Properties({
        @Property(name = "sling.servlet.resourceTypes", unbounded = PropertyUnbounded.ARRAY, label = "Sling Resource Type", description = "Sling Resource Type for the Home Page component or components."),
        @Property(name = "sling.servlet.selectors", value = "sitemap", propertyPrivate = true),
        @Property(name = "sling.servlet.extensions", value = "xml", propertyPrivate = true),
        @Property(name = "sling.servlet.methods", value = "GET", propertyPrivate = true),
        @Property(name = "webconsole.configurationFactory.nameHint", value = "Site Map for: {externalizer.domain}, on resource types: [{sling.servlet.resourceTypes}]") })
public final class SiteMapServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(SiteMapServlet.class);

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    private static final boolean DEFAULT_INCLUDE_LAST_MODIFIED = false;

    private static final boolean DEFAULT_INCLUDE_INHERITANCE_VALUE = false;

    private static final String DEFAULT_EXTERNALIZER_DOMAIN = "publish";

    private static final boolean DEFAULT_EXTENSIONLESS_URLS = false;

    private static final boolean DEFAULT_REMOVE_TRAILING_SLASH = false;

    private static final boolean DEFAULT_USE_VANITY_URL = true;

    @Property(value = DEFAULT_EXTERNALIZER_DOMAIN, label = "Externalizer Domain", description = "Must correspond to a configuration of the Externalizer component. If blank the externalization will prepend the current request's scheme combined with the current request's host header.")
    private static final String PROP_EXTERNALIZER_DOMAIN = "externalizer.domain";

    @Property(boolValue = DEFAULT_INCLUDE_LAST_MODIFIED, label = "Include Last Modified", description = "If true, the last modified value will be included in the sitemap.")
    private static final String PROP_INCLUDE_LAST_MODIFIED = "include.lastmod";

    @Property(label = "Change Frequency Properties", unbounded = PropertyUnbounded.ARRAY, description = "The set of JCR property names which will contain the change frequency value.")
    private static final String PROP_CHANGE_FREQUENCY_PROPERTIES = "changefreq.properties";

    @Property(label = "Priority Properties", unbounded = PropertyUnbounded.ARRAY, description = "The set of JCR property names which will contain the priority value.")
    private static final String PROP_PRIORITY_PROPERTIES = "priority.properties";

    @Property(label = "DAM Folder Property", description = "The JCR property name which will contain DAM folders to include in the sitemap.")
    private static final String PROP_DAM_ASSETS_PROPERTY = "damassets.property";

    @Property(label = "DAM Asset MIME Types", unbounded = PropertyUnbounded.ARRAY, description = "MIME types allowed for DAM assets.")
    private static final String PROP_DAM_ASSETS_TYPES = "damassets.types";

    @Property(label = "Exclude Pages (by properties of boolean values) from Sitemap Property", description = "The boolean [cq:Page]/jcr:content property name which indicates if the Page should be hidden from the Sitemap.")
    private static final String PROP_EXCLUDE_FROM_SITEMAP_PROPERTY = "exclude.property";

    @Property(label = "URL Rewrites", unbounded = PropertyUnbounded.ARRAY, description = "Colon separated URL rewrites to adjust the <loc> to match your dispatcher's apache rewrites")
    private static final String PROP_URL_REWRITES = "url.rewrites";

    @Property(boolValue = DEFAULT_INCLUDE_INHERITANCE_VALUE, label = "Include Inherit Value", description = "If true searches for the frequency and priority attribute in the current page if null looks in the parent.")
    private static final String PROP_INCLUDE_INHERITANCE_VALUE = "include.inherit";

    @Property(boolValue = DEFAULT_EXTENSIONLESS_URLS, label = "Extensionless URLs", description = "If true, page links included in sitemap are generated without .html extension and the path is included with a trailing slash, e.g. /content/geometrixx/en/.")
    private static final String PROP_EXTENSIONLESS_URLS = "extensionless.urls";

    @Property(boolValue = DEFAULT_REMOVE_TRAILING_SLASH, label = "Remove Trailing Slash from Extensionless URLs", description = "Only relevant if Extensionless URLs is selected.  If true, the trailing slash is removed from extensionless page links, e.g. /content/geometrixx/en.")
    private static final String PROP_REMOVE_TRAILING_SLASH = "remove.slash";

    @Property(label = "Character Encoding", description = "If not set, the container's default is used (ISO-8859-1 for Jetty)")
    private static final String PROP_CHARACTER_ENCODING_PROPERTY = "character.encoding";

    @Property(label = "Exclude Pages (by Template) from Sitemap", description = "Excludes pages that have a matching value at [cq:Page]/jcr:content@cq:Template")
    private static final String TEMPLATE_EXCLUDE_FROM_SITEMAP_PROPERTY = "exclude.templates";

    @Property(boolValue = DEFAULT_USE_VANITY_URL, label = "Use Vanity URLs", description = "Use the Vanity URL for generating the Page URL")
    private static final String USE_VANITY_URL = "use.vanity";

    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    @Reference
    private transient Externalizer externalizer;

    private String externalizerDomain;

    private boolean includeInheritValue;

    private boolean includeLastModified;

    private String[] changefreqProperties;

    private String[] priorityProperties;

    private String damAssetProperty;

    private List<String> damAssetTypes;

    private List<String> excludeFromSiteMapProperty;

    private String characterEncoding;

    private boolean extensionlessUrls;

    private Map<String, String> urlRewrites;

    private boolean removeTrailingSlash;

    private List<String> excludedPageTemplates;

    private boolean useVanityUrl;

    @Activate
    protected void activate(Map<String, Object> properties) {
        this.externalizerDomain = PropertiesUtil.toString(properties.get(PROP_EXTERNALIZER_DOMAIN),
                DEFAULT_EXTERNALIZER_DOMAIN);
        this.includeLastModified = PropertiesUtil.toBoolean(properties.get(PROP_INCLUDE_LAST_MODIFIED),
                DEFAULT_INCLUDE_LAST_MODIFIED);
        this.includeInheritValue = PropertiesUtil.toBoolean(properties.get(PROP_INCLUDE_INHERITANCE_VALUE),
                DEFAULT_INCLUDE_INHERITANCE_VALUE);
        this.changefreqProperties = PropertiesUtil.toStringArray(properties.get(PROP_CHANGE_FREQUENCY_PROPERTIES),
                new String[0]);
        this.priorityProperties = PropertiesUtil.toStringArray(properties.get(PROP_PRIORITY_PROPERTIES), new String[0]);
        this.damAssetProperty = PropertiesUtil.toString(properties.get(PROP_DAM_ASSETS_PROPERTY), "");
        this.damAssetTypes = Arrays
                .asList(PropertiesUtil.toStringArray(properties.get(PROP_DAM_ASSETS_TYPES), new String[0]));
        this.excludeFromSiteMapProperty = Arrays.asList(PropertiesUtil.toStringArray(properties.get(PROP_EXCLUDE_FROM_SITEMAP_PROPERTY),
                new String[0]));
        this.characterEncoding = PropertiesUtil.toString(properties.get(PROP_CHARACTER_ENCODING_PROPERTY), null);
        this.extensionlessUrls = PropertiesUtil.toBoolean(properties.get(PROP_EXTENSIONLESS_URLS),
                DEFAULT_EXTENSIONLESS_URLS);
        this.urlRewrites = ParameterUtil.toMap(PropertiesUtil.toStringArray(properties.get(PROP_URL_REWRITES), new String[0]), ":", true, "");
        this.removeTrailingSlash = PropertiesUtil.toBoolean(properties.get(PROP_REMOVE_TRAILING_SLASH),
                DEFAULT_REMOVE_TRAILING_SLASH);
        this.excludedPageTemplates = Arrays.asList(PropertiesUtil.toStringArray(properties.get(TEMPLATE_EXCLUDE_FROM_SITEMAP_PROPERTY),new String[0]));
        this.useVanityUrl =  PropertiesUtil.toBoolean(properties.get(USE_VANITY_URL), DEFAULT_USE_VANITY_URL);
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(request.getResponseContentType());
        if (StringUtils.isNotEmpty(this.characterEncoding)) {
            response.setCharacterEncoding(characterEncoding);
        }
        ResourceResolver resourceResolver = request.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(request.getResource());

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        XMLStreamWriter stream = null;
        try {
            stream = outputFactory.createXMLStreamWriter(response.getWriter());
            stream.writeStartDocument("1.0");

            stream.writeStartElement("", "urlset", NS);
            stream.writeNamespace("", NS);

            // first do the current page
            write(page, stream, request);

            for (Iterator<Page> children = page.listChildren(new PageFilter(false, true), true); children.hasNext();) {
                write(children.next(), stream, request);
            }

            if (damAssetTypes.size() > 0 && damAssetProperty.length() > 0) {
                for (Resource assetFolder : getAssetFolders(page, resourceResolver)) {
                    writeAssets(stream, assetFolder, request);
                }
            }

            stream.writeEndElement();

            stream.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } finally {
            if (stream != null) {
                try
                {
                    stream.close();
                }
                catch ( XMLStreamException e )
                {
                    log.warn("Can not close xml stream writer", e);
                }
            }
        }
    }

    private Collection<Resource> getAssetFolders(Page page, ResourceResolver resolver) {
        List<Resource> allAssetFolders = new ArrayList<Resource>();
        ValueMap properties = page.getProperties();
        String[] configuredAssetFolderPaths = properties.get(damAssetProperty, String[].class);
        if (configuredAssetFolderPaths != null) {
            // Sort to aid in removal of duplicate paths.
            Arrays.sort(configuredAssetFolderPaths);
            String prevPath = "#";
            for (String configuredAssetFolderPath : configuredAssetFolderPaths) {
                // Ensure that this folder is not a child folder of another
                // configured folder, since it will already be included when
                // the parent folder is traversed.
                if (StringUtils.isNotBlank(configuredAssetFolderPath) && !configuredAssetFolderPath.equals(prevPath)
                        && !StringUtils.startsWith(configuredAssetFolderPath, prevPath + "/")) {
                    Resource assetFolder = resolver.getResource(configuredAssetFolderPath);
                    if (assetFolder != null) {
                        prevPath = configuredAssetFolderPath;
                        allAssetFolders.add(assetFolder);
                    }
                }
            }
        }
        return allAssetFolders;
    }

    private String applyUrlRewrites(String url) {
        try {
            String path = URI.create(url).getPath();
            for (Map.Entry<String, String> rewrite : urlRewrites.entrySet()) {
                if (path.startsWith(rewrite.getKey())) {
                    return url.replaceFirst(rewrite.getKey(), rewrite.getValue());
                }
            }
            return url;
        } catch (IllegalArgumentException e) {
            return url;
        }
    }

    @SuppressWarnings("squid:S1192")
    private void write(Page page, XMLStreamWriter stream, SlingHttpServletRequest request) throws XMLStreamException {
        if (isHiddenByPageProperty(page) || isHiddenByPageTemplate(page)) {
            return;
        }
        stream.writeStartElement(NS, "url");
        String loc = "";

        if (useVanityUrl && !StringUtils.isEmpty(page.getVanityUrl())) {
            loc = externalizeUri(request, page.getVanityUrl());
        } else if (!extensionlessUrls) {
            loc = externalizeUri(request, String.format("%s.html", page.getPath()));
        } else {
            String urlFormat = removeTrailingSlash ? "%s" : "%s/";
            loc = externalizeUri(request, String.format(urlFormat, page.getPath()));
        }

        loc = applyUrlRewrites(loc);

        writeElement(stream, "loc", loc);

        if (includeLastModified) {
            Calendar cal = page.getLastModified();
            if (cal != null) {
                writeElement(stream, "lastmod", DATE_FORMAT.format(cal));
            }
        }

        if (includeInheritValue) {
            HierarchyNodeInheritanceValueMap hierarchyNodeInheritanceValueMap = new HierarchyNodeInheritanceValueMap(
                    page.getContentResource());
            writeFirstPropertyValue(stream, "changefreq", changefreqProperties, hierarchyNodeInheritanceValueMap);
            writeFirstPropertyValue(stream, "priority", priorityProperties, hierarchyNodeInheritanceValueMap);
        } else {
            ValueMap properties = page.getProperties();
            writeFirstPropertyValue(stream, "changefreq", changefreqProperties, properties);
            writeFirstPropertyValue(stream, "priority", priorityProperties, properties);
        }

        stream.writeEndElement();
    }

    private boolean isHiddenByPageProperty(Page page){
        boolean flag = false;
        if(this.excludeFromSiteMapProperty != null){
            for(String pageProperty : this.excludeFromSiteMapProperty){
                flag = flag || page.getProperties().get(pageProperty, Boolean.FALSE);
            }
        }
        return flag;
    }

    private boolean isHiddenByPageTemplate(Page page) {
        boolean flag = false;
        if(this.excludedPageTemplates != null){
            for(String pageTemplate : this.excludedPageTemplates){
                flag = flag || page.getProperties().get("cq:template", StringUtils.EMPTY).equalsIgnoreCase(pageTemplate);
            }
        }
        return flag;
    }

    private String externalizeUri(SlingHttpServletRequest request, String path) {
        if (StringUtils.isNotBlank(externalizerDomain)) {
            return externalizer.externalLink(request.getResourceResolver(), externalizerDomain, path);
        } else {
            log.debug("No externalizer domain configured, take into account current host header {} and current scheme {}", request.getServerName(), request.getScheme());
            return externalizer.absoluteLink(request, request.getScheme(), path);
        }
    }

    private void writeAsset(Asset asset, XMLStreamWriter stream, SlingHttpServletRequest request) throws XMLStreamException {
        stream.writeStartElement(NS, "url");

        String loc = externalizeUri(request, asset.getPath());
        writeElement(stream, "loc", loc);

        if (includeLastModified) {
            long lastModified = asset.getLastModified();
            if (lastModified > 0) {
                writeElement(stream, "lastmod", DATE_FORMAT.format(lastModified));
            }
        }

        Resource contentResource = asset.adaptTo(Resource.class).getChild(JcrConstants.JCR_CONTENT);
        if (contentResource != null) {
            if (includeInheritValue) {
                HierarchyNodeInheritanceValueMap hierarchyNodeInheritanceValueMap = new HierarchyNodeInheritanceValueMap(
                        contentResource);
                writeFirstPropertyValue(stream, "changefreq", changefreqProperties, hierarchyNodeInheritanceValueMap);
                writeFirstPropertyValue(stream, "priority", priorityProperties, hierarchyNodeInheritanceValueMap);
            } else {
                ValueMap properties = contentResource.getValueMap();
                writeFirstPropertyValue(stream, "changefreq", changefreqProperties, properties);
                writeFirstPropertyValue(stream, "priority", priorityProperties, properties);
            }
        }

        stream.writeEndElement();
    }

    private void writeAssets(final XMLStreamWriter stream, final Resource assetFolder, final SlingHttpServletRequest request)
            throws XMLStreamException {
        for (Iterator<Resource> children = assetFolder.listChildren(); children.hasNext();) {
            Resource assetFolderChild = children.next();
            if (assetFolderChild.isResourceType(DamConstants.NT_DAM_ASSET)) {
                Asset asset = assetFolderChild.adaptTo(Asset.class);

                if (damAssetTypes.contains(asset.getMimeType())) {
                    writeAsset(asset, stream, request);
                }
            } else {
                writeAssets(stream, assetFolderChild, request);
            }
        }
    }

    private void writeFirstPropertyValue(final XMLStreamWriter stream, final String elementName,
                                         final String[] propertyNames, final ValueMap properties) throws XMLStreamException {
        for (String prop : propertyNames) {
            String value = properties.get(prop, String.class);
            if (value != null) {
                writeElement(stream, elementName, value);
                break;
            }
        }
    }

    @SuppressWarnings("squid:S1144")
    private void writeFirstPropertyValue(final XMLStreamWriter stream, final String elementName,
                                         final String[] propertyNames, final InheritanceValueMap properties) throws XMLStreamException {
        for (String prop : propertyNames) {
            String value = properties.get(prop, String.class);
            if (value == null) {
                value = properties.getInherited(prop, String.class);
            }
            if (value != null) {
                writeElement(stream, elementName, value);
                break;
            }
        }
    }

    private void writeElement(final XMLStreamWriter stream, final String elementName, final String text)
            throws XMLStreamException {
        stream.writeStartElement(NS, elementName);
        stream.writeCharacters(text);
        stream.writeEndElement();
    }

}
