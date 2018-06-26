package com.poc.rest;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.GetDocumentsQuery;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;

public class DocumentResource extends AbstractResource{
    DocumentResource (ServletContext servletContext)
    {
        super(servletContext);
    }

    @GET
    @Path("/{typeOverrideExtension:(\\.[^/]+)?}")
    @Produces({"application/json", "application/xml"})
    public Response getSelf (@Context UriInfo uriInfo, @PathParam("uuid") String uuid)
    {
        QueryResponse queryResponse = queryForSelf(uuid);
        if (queryResponse.getDocumentEntries().size() == 0)
            return Response.status(Response.Status.NOT_FOUND).build();

        DocumentEntry documentEntry = queryResponse.getDocumentEntries().get(0);
        //addLinks(uriInfo, documentEntry);
        return Response.ok(documentEntry).type(getTypeOverride(uriInfo)).build();
    }

    private QueryResponse queryForSelf (String rawUuid)
    {
        GetDocumentsQuery query = new GetDocumentsQuery();
        query.setUuids(new ArrayList<String>());
        query.getUuids().add(uuidUrn(rawUuid));
        QueryRegistry queryRequest = new QueryRegistry(query);
        queryRequest.setReturnType(QueryReturnType.LEAF_CLASS);

        return routeThroughCamel(queryRequest);
    }
}
