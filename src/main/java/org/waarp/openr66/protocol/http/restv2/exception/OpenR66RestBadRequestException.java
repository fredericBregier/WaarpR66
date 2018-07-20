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

import javax.ws.rs.BadRequestException;
import java.util.Arrays;

public class OpenR66RestBadRequestException extends BadRequestException {

    /** The description of the error in JSON format. */
    public final String message;

    /**
     * Creates a new `OpenR66RestBadRequestException` exception.
     *
     * @param message The error description.
     */
    public OpenR66RestBadRequestException(String message) {
        this.message = message;
    }


    /**
     * Creates an OpenR66RestBadRequestException stating that the field passes as argument is missing from the request.
     * @param field The name of the missing field.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException emptyField(String field) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Empty field\"," +
                        "\"internalMessage\":\"The field '" + field + "' is missing or empty.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the parameter passed as argument is empty.
     * @param parameter The name of the empty parameter.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException emptyParameter(String parameter) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Empty parameter\"," +
                        "\"internalMessage\":\"The parameter '" + parameter + "' is empty.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the parameter passed as argument is empty.
     * @param parameter The name of the empty parameter.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException tooManyValues(String parameter) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Too many values\"," +
                        "\"internalMessage\":\"The parameter '" + parameter + "' has too many values.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the id passed as argument already exist in the database of
     * the entered type.
     * @param type The type of entry.
     * @param id The id of the existing entry.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException alreadyExisting(String type, String id) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Already existing\"," +
                        "\"internalMessage\":\"The " + type + " of id '" + id + "' already exists in the database.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the enum parameter of type 'type' and of name 'id' got
     * an invalid value 'value'.
     * @param type The type of entry.
     * @param id The id of the existing entry.
     * @param value The invalid value.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException invalidEnum(Class type, String id, String value) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Invalid enum value\"," +
                        "\"internalMessage\":\"'" + value + "' is not a valid enum value for parameter '" +
                        id + "'. Valid values : " + Arrays.toString(type.getEnumConstants()) + "\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the parameter of name 'id' was expecting a numerical
     * value but got 'value' instead which is not a number.
     * @param id The id of the existing entry.
     * @param value The invalid value.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException notANumber(String id, String value) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Not a number\"," +
                        "\"internalMessage\":\"The parameter '" + id + "' was expecting a numerical value but got '" +
                        value + "' instead.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the parameter of name 'id' cannot be negative but got a
     * negative value.
     * @param id The id of the existing entry.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException negative(String id) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Illegal value\"," +
                        "\"internalMessage\":\"The parameter '" + id + "' cannot be negative.\"" +
                        "}"
        );
    }

    /**
     * Creates an OpenR66RestBadRequestException stating that the host already has one of the requested type of entry
     * in the database.
     * @param type The type of entry.
     * @return The new exception.
     */
    public static OpenR66RestBadRequestException alreadyExisting(String type) {
        return new OpenR66RestBadRequestException(
                "{" +
                        "\"userMessage\":\"Empty field\"," +
                        "\"internalMessage\":\"This host already has a " + type + " in the database.\"" +
                        "}"
        );
    }
}
