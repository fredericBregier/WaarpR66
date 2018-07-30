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

package org.waarp.openr66.protocol.http.restv2.exception;


public class OpenR66RestInternalServerException extends Exception {

    //ERROR CODES
    //TODO:replace the placeholders by the real error codes
    private final static int JSON_PROCESSING = 100;

    private final static int UNKNOWN_FILTER_TYPE = 101;

    private final static int OBJECT_INSTANTIATION = 102;


    /** The description of the error in JSON format. */
    public final String message;

    /**
     * Creates a new `OpenR66RestInternalServerError` exception with the message passed as argument.
     *
     * @param message The error description.
     */
    public OpenR66RestInternalServerException(String message) {
        this.message = message;
    }

    /**
     * Returns a new OpenR66RestInternalServerException stating that there was an error when converting the response
     * java object into a JSON String.
     *
     * @return The new exception.
     */
    public static OpenR66RestInternalServerException objectInstantiation(Class c) {
        return new OpenR66RestInternalServerException(
                "{" +
                        "\"userMessage\":\"Object Instantiation Error\"," +
                        "\"internalMessage\":\"Could not instantiate object " + c.getSimpleName() + ".\"," +
                        "\"code\":" + OBJECT_INSTANTIATION +
                        "}"
        );
    }

    /**
     * Returns a new OpenR66RestInternalServerException stating that there was an error when converting the response
     * java object into a JSON String.
     *
     * @return The new exception.
     */
    public static OpenR66RestInternalServerException jsonProcessing() {
        return new OpenR66RestInternalServerException(
                "{" +
                        "\"userMessage\":\"JSON Processing Error\"," +
                        "\"internalMessage\":\"Could not transform the response into JSON format.\"," +
                        "\"code\":" + JSON_PROCESSING +
                        "}"
        );
    }

    /**
     * Returns a new OpenR66RestInternalServerException stating that there was an error when trying to convert one of
     * the query parameters to a Filter class field.
     *
     * @return The new exception.
     */
    public static OpenR66RestInternalServerException unknownFilterType(Class type) {
        return new OpenR66RestInternalServerException(
                "{" +
                        "\"userMessage\":\"Unknown Filter Type\"," +
                        "\"internalMessage\":\"The type of filter '" + type.getSimpleName() + "' is not supported.\"," +
                        "\"code\":" + UNKNOWN_FILTER_TYPE +
                        "}"
        );
    }

    /**
     * Returns a new OpenR66RestInternalServerException stating that there was an error when converting the response
     * java object into a JSON String.
     *
     * @return The new exception.
     */
    public static OpenR66RestInternalServerException base64Decoding() {
        return new OpenR66RestInternalServerException(
                "{" +
                        "\"userMessage\":\"Base64 Decoding Error\"," +
                        "\"internalMessage\":\"An error occurred during the decoding of the base64 authentication key."+
                        "\",\"code\":" + JSON_PROCESSING +
                        "}"
        );
    }
}
