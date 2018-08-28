/*
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 * Copyright 2009, Waarp SAS, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * All Waarp Project is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Waarp . If not, see <http://www.gnu.org/licenses/>.
 */

package org.waarp.openr66.protocol.http.restv2.handler;

import co.cask.http.HttpResponder;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.waarp.openr66.dao.TransferDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Transfer;
import org.waarp.openr66.protocol.http.restv2.RestUtils;
import org.waarp.openr66.protocol.http.restv2.data.RestHostConfig;
import org.waarp.openr66.protocol.http.restv2.data.RestTransfer;
import org.waarp.openr66.protocol.http.restv2.errors.InternalErrorResponse;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Arrays;
import java.util.List;

/**
 * This is the handler for all requests made on a single 'transfer' entry, accessible with the URI "/transfer/{id}",
 * with {id} being the actual id of the desired transfer.
 */
@Path("/v2/transfers/{id}")
public class TransferIdHandler extends AbstractRestHttpHandler {

    private final static List<RestHostConfig.RoleType> writeRoles = Arrays.asList(
            RestHostConfig.RoleType.fullAdmin,
            RestHostConfig.RoleType.configAdmin,
            RestHostConfig.RoleType.partner,
            RestHostConfig.RoleType.transfer
    );
    private final static List<RestHostConfig.RoleType> readRoles = Arrays.asList(
            RestHostConfig.RoleType.readOnly
    );

    public TransferIdHandler() {
        super(writeRoles, readRoles);
    }

    /**
     * The method called when a GET request is made on /v2/transfers/{id}. If the request is valid and the id exists
     * in the database, the Http response will contain the corresponding transfer entry. If the id does not exist,
     * the response will contain a '404 - Not found' error message. If the id exists but the request is invalid, a
     * '400 - Bad request' error will be sent instead.
     *
     * @param request   The Http request made on the resource.
     * @param responder The Http responder, Http response are given to it in order to be sent back.
     * @param id        The requested transfer's id, this id is identical to the {id} in the URI of the request.
     */
    @GET
    public void getTransfer(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
        try {
            long transID = Long.parseLong(id);
            TransferDAO transferDAO = RestUtils.factory.getTransferDAO();
            if (!transferDAO.exist(transID)) {
                responder.sendStatus(HttpResponseStatus.NOT_FOUND);
            } else {
                RestTransfer trans = new RestTransfer(transferDAO.select(transID));
                String responseBody = RestUtils.toJsonString(trans);
                responder.sendJson(HttpResponseStatus.OK, responseBody);
            }
        } catch (JsonProcessingException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.jsonProcessingError().toJson());
        } catch (NumberFormatException e) {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        } catch (DAOException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.databaseError().toJson());
        }
    }

    /**
     * The method called when an OPTIONS request is made on /v2/transfers/{id}. If the request is valid, a response
     * will be sent with a list of allowed http methods in the header. If the request isn't valid the response will
     * contain a '400 - Bad request' error message.
     *
     * @param request   The Http request made on the resource.
     * @param responder The Http responder, Http response are given to it in order to be sent back.
     * @param id        The requested transfer's id, this id is identical to the {id} in the URI of the request.
     */
    @OPTIONS
    public void options(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
        HttpHeaders headers = new DefaultHttpHeaders();
        String allow = RestUtils.options(this.getClass());
        headers.add("allow", allow);
        responder.sendStatus(HttpResponseStatus.OK, headers);
    }

    /**
     * The method called when a PUT request is made on /v2/transfers/{id}/restart. If the request is valid and the
     * id exists in the database, the requested transfer will be restarted, and the Http response will contain the
     * corresponding updated transfer entry. If the id does not exist, the response will contain a '404 - Not found'
     * error message. If the id exists but the request is invalid, a '400 - Bad request' error will be sent
     * instead.
     *
     * @param request   The Http request made on the resource.
     * @param responder The Http responder, Http response are given to it in order to be sent back.
     * @param id        The requested transfer's id, this id is identical to the {id} in the URI of the request.
     */
    @Path("restart")
    @PUT
    public void restartTransfer(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
        try {
            long transID = Long.parseLong(id);
            TransferDAO transferDAO = RestUtils.factory.getTransferDAO();
            if (!transferDAO.exist(transID)) {
                responder.sendStatus(HttpResponseStatus.NOT_FOUND);
            } else {
                Transfer trans = transferDAO.select(transID);
                //TODO: update the transfer status to restart it
                transferDAO.update(trans);
                String responseBody = RestUtils.toJsonString(new RestTransfer(trans));
                responder.sendJson(HttpResponseStatus.OK, responseBody);
            }
        } catch (JsonProcessingException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.jsonProcessingError().toJson());
        } catch (NumberFormatException e) {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        } catch (DAOException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.databaseError().toJson());
        }
    }

    /**
     * The method called when a PUT request is made on /v2/transfers/{id}/stop. If the request is valid and the id
     * exists in the database, the requested transfer will be paused, and the Http response will contain the
     * corresponding updated transfer entry. If the id does not exist, the response will contain a '404 - Not found'
     * error message. If the id exists but the request is invalid, a '400 - Bad request' error will be sent
     * instead.
     *
     * @param request   The Http request made on the resource.
     * @param responder The Http responder, Http response are given to it in order to be sent back.
     * @param id        The requested transfer's id, this id is identical to the {id} in the URI of the request.
     */
    @Path("stop")
    @PUT
    public void stopTransfer(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
        try {
            long transID = Long.parseLong(id);
            TransferDAO transferDAO = RestUtils.factory.getTransferDAO();
            if (!transferDAO.exist(transID)) {
                responder.sendStatus(HttpResponseStatus.NOT_FOUND);
            } else {
                Transfer trans = transferDAO.select(transID);
                //TODO: update the transfer status to pause it
                transferDAO.update(trans);
                String responseBody = RestUtils.toJsonString(new RestTransfer(trans));
                responder.sendJson(HttpResponseStatus.OK, responseBody);
            }
        } catch (JsonProcessingException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.jsonProcessingError().toJson());
        } catch (NumberFormatException e) {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        } catch (DAOException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.databaseError().toJson());
        }
    }

    /**
     * The method called when a PUT request is made on /v2/transfers/{id}/cancel. If the request is valid and the id
     * exists in the database, the requested transfer will be restarted, and the Http response will contain the
     * corresponding updated transfer entry. If the id does not exist, the response will contain a '404 - Not found'
     * error message. If the id exists but the request is invalid, a '400 - Bad request' error will be sent
     * instead.
     *
     * @param request   The Http request made on the resource.
     * @param responder The Http responder, Http response are given to it in order to be sent back.
     * @param id        The requested transfer's id, this id is identical to the {id} in the URI of the request.
     */
    @Path("cancel")
    @PUT
    public void cancelTransfer(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
        try {
            long transID = Long.parseLong(id);
            TransferDAO transferDAO = RestUtils.factory.getTransferDAO();
            if (!transferDAO.exist(transID)) {
                responder.sendStatus(HttpResponseStatus.NOT_FOUND);
            } else {
                Transfer trans = transferDAO.select(transID);
                //TODO: update the transfer status to cancel it
                transferDAO.update(trans);
                String responseBody = RestUtils.toJsonString(new RestTransfer(trans));
                responder.sendJson(HttpResponseStatus.OK, responseBody);
            }
        } catch (JsonProcessingException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.jsonProcessingError().toJson());
        } catch (NumberFormatException e) {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        } catch (DAOException e) {
            responder.sendJson(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    InternalErrorResponse.databaseError().toJson());
        }
    }
}
