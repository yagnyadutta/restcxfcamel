package com.poc.rest;

import javax.ws.rs.Path;

public class RegistryResource extends AbstractResource {

    private DocumentsResource documentResource;

    @Path("/documents")
    public synchronized DocumentsResource getDocumentsResource ()
    {
        if (documentResource == null)
            documentResource = new DocumentsResource(getServletContext());

        return documentResource;
    }
}
