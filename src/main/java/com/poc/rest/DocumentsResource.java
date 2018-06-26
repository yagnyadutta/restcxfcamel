package com.poc.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

public class DocumentsResource extends AbstractResource {
    DocumentsResource (ServletContext servletContext)
    {
        super(servletContext);
    }

    @Path("/{uuid}")
    public synchronized DocumentResource getDocumentResource ()
    {
        if (documentResource == null)
            documentResource = new DocumentResource(getServletContext());

        return documentResource;
    }

    private DocumentResource documentResource;
}
