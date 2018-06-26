/*
 * Copyright (c) 2010-2011. EMC Corporation. All Rights Reserved.
 */
package com.poc.rest;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.GetDocumentsQuery;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.net.URI;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DocumentResourceTest
{
    private static ProducerTemplate producerTemplate;
    private static CamelContext camelContext;
    private static WebApplicationContext springContext;

    public static Server server;
    @BeforeClass
    public static void setUpClass () throws Exception
    {
        mockSupport = new EasyMockSupport();
        //jaxbUtil = new JAXBUtil(JAXBContext.newInstance(ElementRefs.class));
        //startServer(new CXFServlet(), "META-INF/spring/test-rest-context.xml");

        // Create Server
        server = new Server(8080);
        URI uri = new ClassPathResource("META-INF/spring/test-rest-context.xml").getURI();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setResourceBase("/");
        ContextLoaderListener listener = new ContextLoaderListener();
        context.getInitParams().put(ContextLoader.CONFIG_LOCATION_PARAM, uri.toString());
        context.setContextPath("");
        context.addEventListener(listener);
        ServletHolder holder = new ServletHolder("cxf", CXFServlet.class);
        context.addServlet(holder,"/*");
        server.setHandler(context);

        // Start Server
        server.start();
        ServletContext servletContext = holder.getServlet().getServletConfig().getServletContext();
        hostAndPort = String.format("http://localhost:%s", "8080");
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        producerTemplate = (ProducerTemplate) springContext.getBean("template");
        camelContext = (CamelContext) springContext.getBean("camelContext");
    }

    @Before
    public void setUp ()
    {
        List<Object> providers = new ArrayList<Object>();
        providers.add(springContext.getBean("jsonProvider"));
        client = WebClient.create(hostAndPort, providers);

    }



    @Test
    public void testGetSelf () throws Exception
    {
        String rawUuid = "Document01";
        WebClient resourceClient = client.path("registry").path("documents").path(rawUuid);

        GetDocumentsQuery query = new GetDocumentsQuery();
        query.setUuids(new ArrayList<String>());
        query.getUuids().add("urn:uuid:" + rawUuid);
        QueryRegistry request = new QueryRegistry(query);
        request.setReturnType(QueryReturnType.LEAF_CLASS);

        DocumentEntry documentEntry = EasyMock.createMock(DocumentEntry.class);

        QueryResponse response = new QueryResponse(Status.SUCCESS);
        response.setDocumentEntries(new ArrayList<DocumentEntry>());
        response.getDocumentEntries().add(documentEntry);
      // mockProcessor.process(bodyMatchesAndReturns(request, response));

        mockSupport.replayAll();

        DocumentEntry actualDocumentEntry = resourceClient.get(DocumentEntry.class);
        assertEquals(documentEntry, actualDocumentEntry);

        mockSupport.verifyAll();
    }





    private WebClient client;
    private static EasyMockSupport mockSupport;
    private Processor mockProcessor;

    private static String hostAndPort;
    //private static JAXBUtil jaxbUtil;
}
