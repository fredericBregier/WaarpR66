/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package openr66.server;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.common.logging.GgSlf4JLoggerFactory;

import java.io.File;

import openr66.client.SubmitTransfer;
import openr66.configuration.AuthenticationFileBasedConfiguration;
import openr66.configuration.FileBasedConfiguration;
import openr66.configuration.RuleFileBasedConfiguration;
import openr66.database.DbConstant;
import openr66.database.exception.OpenR66DatabaseException;
import openr66.database.exception.OpenR66DatabaseNoConnectionError;
import openr66.database.model.DbModelFactory;
import openr66.protocol.exception.OpenR66ProtocolSystemException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jboss.netty.logging.InternalLoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Utility class to initiate the database for a server
 *
 * @author Frederic Bregier
 *
 */
public class ServerInitDatabase {
    /**
     * Internal Logger
     */
    static volatile GgInternalLogger logger;

    static String sxml = null;
    static boolean database = false;
    static String sdirconfig = null;
    static String shostauth = null;
    static String slimitconfig = null;

    protected static boolean getParams(String [] args) {
        if (args.length < 1) {
            logger.error("Need at least the configuration file as first argument");
            return false;
        }
        sxml = args[0];
        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-initdb")) {
                database = true;
            } else if (args[i].equalsIgnoreCase("-dir")) {
                i++;
                sdirconfig = args[i];
            } else if (args[i].equalsIgnoreCase("-limit")) {
                i++;
                slimitconfig = args[i];
            } else if (args[i].equalsIgnoreCase("-auth")) {
                i++;
                shostauth = args[i];
            }
        }
        return true;
    }

    /**
     * @param args
     *          as config_database file
     *          [rules_directory host_authent limit_configuration]
     */
    public static void main(String[] args) {
        InternalLoggerFactory.setDefaultFactory(new GgSlf4JLoggerFactory(
                Level.WARN));
        if (logger == null) {
            logger = GgInternalLoggerFactory.getLogger(SubmitTransfer.class);
        }
        if (! getParams(args)) {
            logger.error("Need at least config_database file " +
                    "and optionaly (in that order) rules_directory host_authent_file " +
                    "limit_configuration_file");
            if (DbConstant.admin != null && DbConstant.admin.isConnected) {
                DbConstant.admin.close();
            }
            System.exit(1);
        }

        try {
            Document document = null;
            // Open config file
            try {
                document = new SAXReader().read(sxml);
            } catch (DocumentException e) {
                logger.error("Unable to read the XML Config file: " + sxml, e);
                return;
            }
            if (document == null) {
                logger.error("Unable to read the XML Config file: " + sxml);
                return;
            }
            if (!FileBasedConfiguration.loadDatabase(document)) {
                logger.error("Cannot start database");
                return;
            }
            if (database) {
                // Init database
                try {
                    initdb();
                } catch (OpenR66DatabaseNoConnectionError e) {
                    logger.error("Cannot connect to database");
                    return;
                }
                System.out.println("End creation");
            }
            if (sdirconfig != null) {
                // load Rules
                File dirConfig = new File(sdirconfig);
                if (dirConfig.isDirectory()) {
                    loadRules(dirConfig);
                } else {
                    System.err.println("Dir is not a directory: " + sdirconfig);
                }
            }
            if (shostauth != null) {
                // Load Host Authentications
                if (args.length > 2) {
                    loadHostAuth(shostauth);
                }
            }
            if (slimitconfig != null) {
                // Load configuration
                if (args.length > 3) {
                    loadConfiguration(slimitconfig);
                }
            }
            System.out.println("Load done");
        } finally {
            if (DbConstant.admin != null) {
                DbConstant.admin.close();
            }
        }
    }

    public static void initdb() throws OpenR66DatabaseNoConnectionError {
        // Create tables: configuration, hosts, rules, runner, cptrunner
        DbModelFactory.dbModel.createTables();
    }

    public static void loadRules(File dirConfig) {
        try {
            RuleFileBasedConfiguration.importRules(dirConfig);
        } catch (OpenR66ProtocolSystemException e3) {
            e3.printStackTrace();
        } catch (OpenR66DatabaseException e) {
            e.printStackTrace();
        }
    }
    public static void loadHostAuth(String filename) {
        AuthenticationFileBasedConfiguration.loadAuthentication(filename);
    }
    public static void loadConfiguration(String filename) {
        Document document = null;
        // Open config file
        try {
            document = new SAXReader().read(filename);
        } catch (DocumentException e) {
            e.printStackTrace();
            return;
        }
        FileBasedConfiguration.loadLimit(document);
    }
}
