package com.poc.rest;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.RegisterDocumentSet;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class AbstractResource {
    protected AbstractResource()
    {
    }

    protected AbstractResource(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext ()
    {
        if (servletContext == null)
            throw new IllegalStateException("servletContext has not been set");

        return servletContext;
    }

    protected final synchronized ProducerTemplate getProducerTemplate ()
    {
        if (producerTemplate == null)
        {
            WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            producerTemplate = (ProducerTemplate) context.getBean("template");
        }
        return producerTemplate;
    }

    protected final QueryResponse routeThroughCamel (QueryRegistry body)
    {
        return (QueryResponse) routeThroughCamel("direct:queryRegistry", (Object) body);
    }

    protected final Response routeThroughCamel (RegisterDocumentSet body)
    {
        return (Response) routeThroughCamel("direct:registerDocumentSet", (Object) body);
    }

    protected final Object routeThroughCamel (String destination, Object body)
    {
        return getProducerTemplate().sendBody(destination, ExchangePattern.InOut, body);
    }

    private static String buildDocumentUri (UriInfo uriInfo, String rawUuid)
    {
        return buildDocumentUri(uriInfo, rawUuid, "", true);
    }

    private static String buildDocumentUri (UriInfo uriInfo, String rawUuid, String subResource)
    {
        return buildDocumentUri(uriInfo, rawUuid, subResource, true);
    }

    private static String buildDocumentUri (
            UriInfo uriInfo, String rawUuid, String subResource, boolean includeTypeExtension)
    {
        return buildUri(uriInfo, String.format("/documents/%s%s", rawUuid, subResource), includeTypeExtension, false);
    }

    private static String buildFolderUri (UriInfo uriInfo, String rawUuid)
    {
        return buildFolderUri(uriInfo, rawUuid, "");
    }

    private static String buildFolderUri (UriInfo uriInfo, String rawUuid, String subResource)
    {
        return buildUri(uriInfo, String.format("/folders/%s%s", rawUuid, subResource), true, false);
    }

    private static String buildSubmissionSetUri (UriInfo uriInfo, String rawUuid)
    {
        return buildSubmissionSetUri(uriInfo, rawUuid, "");
    }

    private static String buildSubmissionSetUri (UriInfo uriInfo, String rawUuid, String subResource)
    {
        return buildUri(uriInfo, String.format("/submissionSets/%s%s", rawUuid, subResource), true, false);
    }

    private static String buildUri (
            UriInfo uriInfo, String relativeUri, boolean includeTypeOverrideExtension, boolean absolute)
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append(absolute ? uriInfo.getBaseUri() : uriInfo.getBaseUri().getPath());
        builder.append(relativeUri);

        if (includeTypeOverrideExtension)
        {
            String extension = getTypeOverrideExtension(uriInfo);
            if (extension != null)
                builder.append('/').append(extension);
        }

        return builder.toString();
    }

    protected static String getTypeOverrideExtension (UriInfo uriInfo)
    {
        String extension = uriInfo.getPathParameters().getFirst("typeOverrideExtension");
        if (StringUtils.isEmpty(extension))
            return null;

        if (extension.startsWith("/"))
            return extension.substring(1);
        else
            return extension;
    }

    protected static String getTypeOverride (UriInfo uriInfo)
    {
        String extension = getTypeOverrideExtension(uriInfo);
        if (extension == null)
            return null; // Will be computed later by normal jax-rs algorithm

        return extensionToTypeMap.get(extension);
    }

    protected static String rawUuid (String uuid)
    {
        return uuid.startsWith("urn:uuid:") ? uuid.substring(9) : uuid;
    }

    protected static String uuidUrn (String uuid)
    {
        return uuid.startsWith("urn:uuid:") ? uuid : "urn:uuid:" + uuid;
    }

    @Context
    private ServletContext servletContext;
    private ProducerTemplate producerTemplate;

    private static final Map<String, String> extensionToTypeMap = new HashMap<String, String>();

    static
    {
        extensionToTypeMap.put(".xml", "application/xml");
        extensionToTypeMap.put(".json", "application/json");
        extensionToTypeMap.put(".atom", "application/atom+xml");
        extensionToTypeMap.put(".atomfeed", "application/atom+xml;type=feed");
        extensionToTypeMap.put(".atomentry", "application/atom+xml;type=entry");
    }
}
