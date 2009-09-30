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
package openr66.configuration;

import goldengate.common.file.DirInterface;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import openr66.database.DbConstant;
import openr66.database.DbPreparedStatement;
import openr66.database.data.DbRule;
import openr66.database.exception.OpenR66DatabaseException;
import openr66.database.exception.OpenR66DatabaseNoConnectionError;
import openr66.database.exception.OpenR66DatabaseNoDataException;
import openr66.database.exception.OpenR66DatabaseSqlError;
import openr66.protocol.configuration.Configuration;
import openr66.protocol.exception.OpenR66ProtocolNoDataException;
import openr66.protocol.exception.OpenR66ProtocolSystemException;
import openr66.protocol.utils.FileUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

/**
 * Rule File Based Configuration
 *
 * @author Frederic Bregier
 *
 */
public class RuleFileBasedConfiguration {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(RuleFileBasedConfiguration.class);

    private static final String ROOT = "rule";
    private static final String XIDRULE = "idrule";
    private static final String XHOSTIDS = "hostids";
    private static final String XHOSTID = "hostid";
    private static final String XMODE = "mode";
    private static final String XRECVPATH = "recvpath";
    private static final String XSENDPATH = "sendpath";
    private static final String XARCHIVEPATH = "archivepath";
    private static final String XWORKPATH = "workpath";
    private static final String XRPRETASKS = "rpretasks";
    private static final String XRPOSTTASKS = "rposttasks";
    private static final String XRERRORTASKS = "rerrortasks";
    private static final String XSPRETASKS = "spretasks";
    private static final String XSPOSTTASKS = "sposttasks";
    private static final String XSERRORTASKS = "serrortasks";
    private static final String XTASKS = "tasks";
    private static final String XTASK = "task";

    private static final String IDRULE = "/"+ROOT+"/"+XIDRULE;

    private static final String HOSTIDS_HOSTID = "/"+ROOT+"/"+XHOSTIDS+"/"+XHOSTID;

    private static final String MODE = "/"+ROOT+"/"+XMODE;

    private static final String RECVPATH = "/"+ROOT+"/"+XRECVPATH;

    private static final String SENDPATH = "/"+ROOT+"/"+XSENDPATH;

    private static final String ARCHIVEPATH = "/"+ROOT+"/"+XARCHIVEPATH;

    private static final String WORKPATH = "/"+ROOT+"/"+XWORKPATH;

    private static final String RPRETASKS = "/"+ROOT+"/"+XRPRETASKS;

    private static final String RPOSTTASKS = "/"+ROOT+"/"+XRPOSTTASKS;

    private static final String RERRORTASKS = "/"+ROOT+"/"+XRERRORTASKS;

    private static final String SPRETASKS = "/"+ROOT+"/"+XSPRETASKS;

    private static final String SPOSTTASKS = "/"+ROOT+"/"+XSPOSTTASKS;

    private static final String SERRORTASKS = "/"+ROOT+"/"+XSERRORTASKS;

    private static final String TASK = "tasks/task";

    /**
     * Extension of rule files
     */
    public static final String EXT_RULE = ".rule.xml";

    /**
     *
     * @author Frederic Bregier
     *
     */
    static class ExtensionFilter implements FilenameFilter {
        /*
         * (non-Javadoc)
         *
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        @Override
        public boolean accept(File arg0, String arg1) {
            return arg1.endsWith(EXT_RULE);
        }

    }

    /**
     * Import all Rule files into the HashTable of Rules
     *
     * @param configDirectory
     * @throws OpenR66ProtocolSystemException
     * @throws OpenR66DatabaseException
     */
    public static void importRules(File configDirectory)
            throws OpenR66ProtocolSystemException, OpenR66DatabaseException {
        File[] files = FileUtils.getFiles(configDirectory,
                new ExtensionFilter());
        for (File file: files) {
            DbRule rule = getFromFile(file);
            logger.debug(rule.toString());
        }
    }

    /**
     *
     * @param document
     * @param path
     * @return The value associated with the path
     * @throws OpenR66ProtocolNoDataException
     */
    private static String getValue(Document document, String path)
            throws OpenR66ProtocolNoDataException {
        Node node = document.selectSingleNode(path);
        if (node == null) {
            logger.error("Unable to find in Rule file: " + path);
            throw new OpenR66ProtocolNoDataException(
                    "Unable to find in the XML Rule file: " + path);
        }
        String result = node.getText();
        if (result == null || result.length() == 0) {
            throw new OpenR66ProtocolNoDataException(
                    "Unable to find in the XML Rule file: " + path);
        }
        return result;
    }

    /**
     * Load and update a Rule from a file
     * @param file
     * @return the newly created R66Rule from XML File
     * @throws OpenR66ProtocolSystemException
     * @throws OpenR66DatabaseException
     * @throws OpenR66DatabaseNoDataException
     * @throws OpenR66DatabaseSqlError
     * @throws OpenR66DatabaseNoConnectionError
     * @throws OpenR66ProtocolNoDataException
     */
    @SuppressWarnings("unchecked")
    public static DbRule getFromFile(File file)
            throws OpenR66ProtocolSystemException, OpenR66DatabaseNoConnectionError, OpenR66DatabaseSqlError, OpenR66DatabaseNoDataException, OpenR66DatabaseException {
        DbRule newRule = null;
        Document document = null;
        // Open config file
        try {
            document = new SAXReader().read(file);
        } catch (DocumentException e) {
            logger.error("Unable to read the XML Rule file: " + file.getName(),
                    e);
            throw new OpenR66ProtocolSystemException(
                    "Unable to read the XML Rule file", e);
        }
        if (document == null) {
            logger.error("Unable to read the XML Rule file: " + file.getName());
            throw new OpenR66ProtocolSystemException(
                    "Unable to read the XML Rule file");
        }
        Node nodebase = null;
        String idrule;
        try {
            idrule = getValue(document, IDRULE);
        } catch (OpenR66ProtocolNoDataException e1) {
            throw new OpenR66ProtocolSystemException(e1);
        }
        String smode;
        try {
            smode = getValue(document, MODE);
        } catch (OpenR66ProtocolNoDataException e1) {
            throw new OpenR66ProtocolSystemException(e1);
        }
        int mode = Integer.parseInt(smode);
        String recvpath;
        try {
            recvpath = DirInterface.SEPARATOR + getValue(document, RECVPATH);
        } catch (OpenR66ProtocolNoDataException e) {
            recvpath = Configuration.configuration.inPath;
        }
        String sendpath;
        try {
            sendpath = DirInterface.SEPARATOR + getValue(document, SENDPATH);
        } catch (OpenR66ProtocolNoDataException e) {
            sendpath = Configuration.configuration.outPath;
        }
        String archivepath;
        try {
            archivepath = DirInterface.SEPARATOR +
                    getValue(document, ARCHIVEPATH);
        } catch (OpenR66ProtocolNoDataException e) {
            archivepath = Configuration.configuration.archivePath;
        }
        String workpath;
        try {
            workpath = DirInterface.SEPARATOR + getValue(document, WORKPATH);
        } catch (OpenR66ProtocolNoDataException e) {
            workpath = Configuration.configuration.workingPath;
        }

        String[] idsArray = null;
        List<Node> listNode = document.selectNodes(HOSTIDS_HOSTID);
        if (listNode == null) {
            logger
                    .info("Unable to find the id for Rule, setting to the default");
        } else {
            idsArray = new String[listNode.size()];
            int i = 0;
            for (Node nodeid: listNode) {
                idsArray[i] = nodeid.getText();
                i ++;
            }
            listNode.clear();
            listNode = null;
        }

        nodebase = document.selectSingleNode(RPRETASKS);
        String[][] rpretasks = new String[0][0];
        if (nodebase != null) {
            rpretasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }
        nodebase = document.selectSingleNode(RPOSTTASKS);
        String[][] rposttasks = new String[0][0];
        if (nodebase != null) {
            rposttasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }
        nodebase = document.selectSingleNode(RERRORTASKS);
        String[][] rerrortasks = new String[0][0];
        if (nodebase != null) {
            rerrortasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }
        nodebase = document.selectSingleNode(SPRETASKS);
        String[][] spretasks = new String[0][0];
        if (nodebase != null) {
            spretasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }
        nodebase = document.selectSingleNode(SPOSTTASKS);
        String[][] sposttasks = new String[0][0];
        if (nodebase != null) {
            sposttasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }
        nodebase = document.selectSingleNode(SERRORTASKS);
        String[][] serrortasks = new String[0][0];
        if (nodebase != null) {
            serrortasks = DbRule.getTasksRule(nodebase, TASK);
            nodebase = null;
        }

        newRule = new DbRule(DbConstant.admin.session, idrule, idsArray, mode, recvpath, sendpath,
                archivepath, workpath, rpretasks, rposttasks, rerrortasks,
                spretasks, sposttasks, serrortasks);
        if (DbConstant.admin != null && DbConstant.admin.session != null) {
            if (newRule.exist()) {
                newRule.update();
            } else {
                newRule.insert();
            }
        } else {
            // put in hashtable
            newRule.insert();
        }
        return newRule;
    }
    /**
     * Construct a new Element with value
     * @param name
     * @param value
     * @return the new Element
     */
    private static Element newElement(String name, String value) {
        Element node = new DefaultElement(name);
        node.addText(value);
        return node;
    }
    /**
     * Write the rule to a file from filename
     * @param filename
     * @param rule
     * @throws OpenR66ProtocolSystemException
     */
    private static void writeXML(String filename, DbRule rule) throws OpenR66ProtocolSystemException {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ROOT);
        root.add(newElement(XIDRULE, rule.idRule));
        Element hosts = new DefaultElement(XHOSTIDS);
        for (String host: rule.idsArray) {
            hosts.add(newElement(XHOSTID, host));
        }
        root.add(hosts);
        root.add(newElement(XMODE, Integer.toString(rule.mode)));
        if (rule.recvPath != null) {
            root.add(newElement(XRECVPATH, rule.recvPath.substring(1)));
        }
        if (rule.sendPath != null) {
            root.add(newElement(XSENDPATH, rule.sendPath.substring(1)));
        }
        if (rule.archivePath != null) {
            root.add(newElement(XARCHIVEPATH, rule.archivePath.substring(1)));
        }
        if (rule.workPath != null) {
            root.add(newElement(XWORKPATH, rule.workPath.substring(1)));
        }
        Element tasks = new DefaultElement(XRPRETASKS);
        Element roottasks = new DefaultElement(XTASKS);
        int rank = 0;
        String [][] array = rule.rpreTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);
        tasks = new DefaultElement(XRPOSTTASKS);
        roottasks = new DefaultElement(XTASKS);
        array = rule.rpostTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);
        tasks = new DefaultElement(XRERRORTASKS);
        roottasks = new DefaultElement(XTASKS);
        array = rule.rerrorTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);
        tasks = new DefaultElement(XSPRETASKS);
        roottasks = new DefaultElement(XTASKS);
        array = rule.spreTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);
        tasks = new DefaultElement(XSPOSTTASKS);
        roottasks = new DefaultElement(XTASKS);
        array = rule.spostTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);
        tasks = new DefaultElement(XSERRORTASKS);
        roottasks = new DefaultElement(XTASKS);
        array = rule.serrorTasksArray;
        for (rank = 0; rank < array.length; rank++) {
            Element task = new DefaultElement(XTASK);
            task.add(newElement(DbRule.TASK_TYPE, array[rank][0]));
            task.add(newElement(DbRule.TASK_PATH, array[rank][1]));
            task.add(newElement(DbRule.TASK_DELAY, array[rank][2]));
            roottasks.add(task);
        }
        tasks.add(roottasks);
        root.add(tasks);

        FileUtils.writeXML(filename, null, document);
    }
    /**
     * Write to directory files prefixed by hostname all Rules from database
     * @param directory
     * @param hostname
     * @throws OpenR66DatabaseNoConnectionError
     * @throws OpenR66DatabaseSqlError
     * @throws OpenR66ProtocolSystemException
     */
    public static void writeXml(String directory, String hostname) throws OpenR66DatabaseNoConnectionError, OpenR66DatabaseSqlError, OpenR66ProtocolSystemException {
        File dir = new File(directory);
        if (! dir.isDirectory()) {
            dir.mkdirs();
        }
        String request = "SELECT " +DbRule.selectAllFields+" FROM "+DbRule.table;
        DbPreparedStatement preparedStatement = null;
        try {
            preparedStatement = new DbPreparedStatement(DbConstant.admin.session);
            preparedStatement.createPrepareStatement(request);
            preparedStatement.executeQuery();
            while (preparedStatement.getNext()) {
                DbRule rule = DbRule.getFromStatement(preparedStatement);
                String filename = dir.getAbsolutePath()+File.separator+hostname+"_"+rule.idRule+
                    RuleFileBasedConfiguration.EXT_RULE;
                RuleFileBasedConfiguration.writeXML(filename, rule);
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.realClose();
            }
        }
    }
}
