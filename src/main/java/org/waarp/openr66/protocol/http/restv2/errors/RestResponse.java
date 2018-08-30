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

package org.waarp.openr66.protocol.http.restv2.errors;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class RestResponse {

    public static ResourceBundle restMessages = ResourceBundle.getBundle("restmessages", Locale.ENGLISH);

    protected static String getClassName(Class c) {
        try {
            return RestResponse.restMessages.getString("Data."+c.getSimpleName()).toLowerCase();
        } catch(Throwable t) {
            return c.getSimpleName();
        }
    }

    protected static String getFormat(String formatName, String fallbackFormat) {
        try {
            return restMessages.getString(formatName);
        } catch(Throwable t) {
            return fallbackFormat;
        }
    }

    protected static String formatMessage(String messageName, String[] args, String fallbackFormat) {
        try {
            String format = getFormat(messageName, fallbackFormat);
            return String.format(format, args);
        } catch(Throwable t) {
            return (args == null) ? "" : Arrays.toString(args);
        }
    }

    public abstract String toJson();
}