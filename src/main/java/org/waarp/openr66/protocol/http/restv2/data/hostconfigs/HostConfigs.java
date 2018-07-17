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

package org.waarp.openr66.protocol.http.restv2.data.hostconfigs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.waarp.openr66.protocol.http.restv2.exception.OpenR66RestBadRequestException;
import org.waarp.openr66.protocol.http.restv2.exception.OpenR66RestIdNotFoundException;
import org.waarp.openr66.protocol.http.restv2.exception.OpenR66RestInternalServerException;
import org.waarp.openr66.protocol.http.restv2.test.TestHostConfig;

import java.lang.reflect.Field;

/** This class consists exclusively of static methods that operate on or return host configurations. */
public final class HostConfigs {

    /**
     * Returns the configuration of the host passed as argument, if it has one.
     *
     * @param hostId The id of the queried host
     * @return The desired configuration.
     * @throws OpenR66RestIdNotFoundException Thrown if the queried host does not have a configuration.
     */
    public static HostConfig loadConfig(String hostId) throws OpenR66RestIdNotFoundException {
        //TODO: replace by a real database request
        for (HostConfig config : TestHostConfig.configDb) {
            if (config.hostId.equals(hostId)) {
                return config;
            }
        }
        throw new OpenR66RestIdNotFoundException(
                "{" +
                        "\"userMessage\":\"Not Found\"," +
                        "\"internalMessage\":\"This host does not have a configuration.\"" +
                        "}"
        );
    }

    /**
     * Removes the desired host configuration entry from the database if it exists.
     *
     * @param hostId The id of the host whose configuration should be removed
     * @throws OpenR66RestIdNotFoundException Thrown if the queried host does not have a configuration.
     */
    public static void deleteConfig(String hostId) throws OpenR66RestIdNotFoundException {
        //TODO: replace by a real database request
        HostConfig toDelete = loadConfig(hostId);
        TestHostConfig.configDb.remove(toDelete);
    }

    /**
     * Adds the host configuration to the database if the queried host does not already have a configuration.
     *
     * @param newConfig The new config to add to the database.
     * @throws OpenR66RestBadRequestException Thrown if the host already has a configuration.
     */
    public static void initConfig(HostConfig newConfig) throws OpenR66RestBadRequestException {
        //TODO: replace by a real database request
        if (TestHostConfig.configDb.contains(newConfig)) {
            throw OpenR66RestBadRequestException.alreadyExisting("host configuration");
        }
        TestHostConfig.configDb.add(newConfig);
    }

    /**
     * Replaces the host config entry with the one passed as parameter if it has one.
     *
     * @param newConfig The new host configuration instance.
     * @throws OpenR66RestBadRequestException Thrown if the host does not have a configuration to replace.
     */
    public static void replace(String id, HostConfig newConfig) {
        for (Field field : HostConfig.class.getFields()) {
            try {
                if (field.get(newConfig) == null) {
                    throw OpenR66RestBadRequestException.emptyField(field.getName());
                }
            } catch (IllegalAccessException e) {
                assert false;
            }
        }

        //TODO: delete the old config from the database and insert the new one
        HostConfig oldConfig = loadConfig(id);
        TestHostConfig.configDb.remove(oldConfig);
        TestHostConfig.configDb.add(newConfig);
    }

    /**
     * Replaces the host config entry with the one passed as parameter if it has one.
     *
     * @param newConfig The new host configuration instance.
     * @throws OpenR66RestBadRequestException Thrown if the host does not have a configuration to replace.
     */
    public static void update(String id, HostConfig newConfig) {
        HostConfig oldConfig = loadConfig(id);
        for (Field field : HostConfig.class.getFields()) {
            try {
                if (field.get(newConfig) == null) {
                    field.set(newConfig, field.get(oldConfig));
                }
            } catch (IllegalAccessException e) {
                assert false;
            } catch (IllegalArgumentException e) {
                assert false;
            }
        }
        //TODO: delete the old config from the database and insert the new one
        TestHostConfig.configDb.remove(oldConfig);
        TestHostConfig.configDb.add(newConfig);
    }


    /**
     * Returns the host configuration object as a String usable in a JSON file.
     *
     * @return The host as a String.
     * @throws OpenR66RestInternalServerException Thrown if the config object could not be converted to JSON format.
     */
    public static String toJsonString(HostConfig config) throws OpenR66RestInternalServerException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw OpenR66RestInternalServerException.jsonProcessing();
        }
    }
}