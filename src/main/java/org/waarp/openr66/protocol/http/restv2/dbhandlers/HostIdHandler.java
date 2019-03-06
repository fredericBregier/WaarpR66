/*
 *  This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright 2009, Waarp SAS, and individual contributors by the @author
 *  tags. See the COPYRIGHT.txt in the distribution for a full listing of
 *  individual contributors.
 *
 *  All Waarp Project is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  Waarp . If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.waarp.openr66.protocol.http.restv2.dbhandlers;

import io.cdap.http.HttpResponder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.waarp.openr66.dao.HostDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Host;
import org.waarp.openr66.protocol.http.restv2.data.RequiredRole;
import org.waarp.openr66.protocol.http.restv2.data.RestHost;
import org.waarp.openr66.protocol.http.restv2.utils.RestUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static javax.ws.rs.core.HttpHeaders.ALLOW;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.waarp.common.role.RoleDefault.ROLE.HOST;
import static org.waarp.common.role.RoleDefault.ROLE.NOACCESS;
import static org.waarp.common.role.RoleDefault.ROLE.READONLY;
import static org.waarp.openr66.protocol.http.restv2.RestConstants.DAO_FACTORY;
import static org.waarp.openr66.protocol.http.restv2.RestConstants.HOST_ID_HANDLER_URI;
import static org.waarp.openr66.protocol.http.restv2.RestConstants.URI_ID;
import static org.waarp.openr66.protocol.http.restv2.errors.ErrorFactory.FIELD_NOT_ALLOWED;
import static org.waarp.openr66.protocol.http.restv2.utils.JsonUtils.objectToJson;
import static org.waarp.openr66.protocol.http.restv2.utils.JsonUtils.requestToObject;
import static org.waarp.openr66.protocol.http.restv2.utils.RestUtils.getRequestLocale;

/**
 * This is the {@link AbstractRestDbHandler} handling all operations on a
 * single entry of the host's partner database.
 */
@Path(HOST_ID_HANDLER_URI)
public class HostIdHandler extends AbstractRestDbHandler {

    public HostIdHandler(byte crud) {
        super(crud);
    }

    /**
     * Method called to retrieve a host entry from the database with the id
     * in the request URI.
     *
     * @param request   The {@link HttpRequest} made on the resource.
     * @param responder The {@link HttpResponder} which sends the reply to
     *                  the request.
     * @param id  The requested transfer's id, this refers to the {id} parameter
     *            in the URI of the request.
     */
    @GET
    @Consumes(WILDCARD)
    @RequiredRole(READONLY)
    public void getHost(HttpRequest request, HttpResponder responder,
                        @PathParam(URI_ID) String id) {

        HostDAO hostDAO = null;
        try {
            hostDAO = DAO_FACTORY.getHostDAO();
            Host host = hostDAO.select(id);
            if (host == null) {
                responder.sendStatus(NOT_FOUND);
            } else {
                RestHost restHost = new RestHost(host);
                String responseBody = objectToJson(restHost);
                responder.sendJson(OK, responseBody);
            }
        } catch (DAOException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (hostDAO != null) {
                hostDAO.close();
            }
        }
    }

    /**
     * Method called to update the host entry with the given id. The entry is
     * replaced by the one in the request's body.
     *
     * @param request   The {@link HttpRequest} made on the resource.
     * @param responder The {@link HttpResponder} which sends the reply to
     *                  the request.
     * @param id The requested transfer's id, this refers to the {id} parameter
     *           in the URI of the request.
     */
    @PUT
    @Consumes(APPLICATION_JSON)
    @RequiredRole(HOST)
    public void updateHost(HttpRequest request, HttpResponder responder,
                           @PathParam(URI_ID) String id) {

        HostDAO hostDAO = null;
        try {
            hostDAO = DAO_FACTORY.getHostDAO();

            if (!hostDAO.exist(id)) {
                responder.sendStatus(NOT_FOUND);
                return;
            }

            RestHost partialHost =
                    requestToObject(request, RestHost.class, false);
            Locale lang = getRequestLocale(request);

            // check that the user didn't change the hostID, hostIDs cannot be changed
            if (partialHost.hostID != null && !id.equals(partialHost.hostID)) {
                responder.sendJson(BAD_REQUEST,
                        FIELD_NOT_ALLOWED("hostID").serialize(lang));
                return;
            }
            if (partialHost.hostKey != null && !partialHost.hostKey.isEmpty()) {
                partialHost.encryptPassword();
            }

            RestHost oldHost = new RestHost(hostDAO.select(id));
            RestHost newHost = RestUtils.updateRestObject(oldHost, partialHost);

            hostDAO.update(newHost.toHost());
            String responseBody = objectToJson(newHost);
            responder.sendJson(CREATED, responseBody);

        } catch (DAOException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (hostDAO != null) {
                hostDAO.close();
            }
        }
    }

    /**
     * Method called to delete a host entry from the database.
     *
     * @param request   The {@link HttpRequest} made on the resource.
     * @param responder The {@link HttpResponder} which sends the reply to
     *                  the request.
     * @param id The requested transfer's id, this refers to the {id} parameter
     *           in the URI of the request.
     */
    @DELETE
    @Consumes(WILDCARD)
    @RequiredRole(HOST)
    public void deleteHost(HttpRequest request, HttpResponder responder,
                           @PathParam(URI_ID) String id) {

        HostDAO hostDAO = null;
        try {
            hostDAO = DAO_FACTORY.getHostDAO();
            if(hostDAO.exist(id)) {
                hostDAO.delete(hostDAO.select(id));
                responder.sendStatus(NO_CONTENT);
            } else {
                responder.sendStatus(NOT_FOUND);
            }
        } catch (DAOException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (hostDAO != null) {
                hostDAO.close();
            }
        }
    }

    /**
     * Method called to get a list of all allowed HTTP methods on this entry point.
     * The HTTP methods are sent as an array in the reply's headers.
     *
     * @param request   The {@link HttpRequest} made on the resource.
     * @param responder The {@link HttpResponder} which sends the reply to
     *                  the request.
     * @param id The requested transfer's id, this refers to the {id} parameter
     *           in the URI of the request.
     */
    @OPTIONS
    @Consumes(WILDCARD)
    @RequiredRole(NOACCESS)
    public void options(HttpRequest request, HttpResponder responder,
                        @PathParam(URI_ID) String id) {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        String allow = RestUtils.getMethodList(this.getClass(), this.crud);
        headers.add(ALLOW, allow);
        responder.sendStatus(OK, headers);
    }
}