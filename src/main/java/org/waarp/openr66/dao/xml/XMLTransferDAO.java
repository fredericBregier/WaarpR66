/*******************************************************************************
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright (c) 2019, Waarp SAS, and individual contributors by the @author
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
 ******************************************************************************/

package org.waarp.openr66.dao.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.utility.LongUuid;
import org.waarp.openr66.context.ErrorCode;
import org.waarp.openr66.dao.Filter;
import org.waarp.openr66.dao.TransferDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Transfer;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class XMLTransferDAO implements TransferDAO {

  public static final String ROOT_LIST = "taskrunners";
  public static final String ROOT_ELEMENT = "runner";
  public static final String XMLEXTENSION = "_singlerunner.xml";
  private static final WaarpLogger logger =
      WaarpLoggerFactory.getLogger(XMLTransferDAO.class);
  private static final String XML_SELECT = "//runner[" +
                                           "specialid=$specialid and requester='$requester' and " +
                                           "requested='$requested' and ownerreq='ownerreq']";
  private static final String XML_GET_ALL = "runner";
  public static String GLOBAL_STEP_FIELD = "globalstep";
  public static String LAST_GLOBAL_STEP_FIELD = "globallaststep";
  public static String STEP_FIELD = "step";
  public static String RANK_FIELD = "rank";
  public static String STEP_STATUS_FIELD = "stepstatus";
  public static String RETRIEVE_MODE_FIELD = "retrievemode";
  public static String FILENAME_FIELD = "filename";
  public static String IS_MOVED_FIELD = "ismoved";
  public static String RULE_FIELD = "idrule";
  public static String BLOCK_SIZE_FIELD = "blocksz";
  public static String ORIGINAL_NAME_FIELD = "originalname";
  public static String FILE_INFO_FIELD = "fileinfo";
  public static String TRANSFER_MODE_FIELD = "modetrans";
  public static String START_FIELD = "starttrans";
  public static String STOP_FIELD = "stoptrans";
  public static String INFO_STATUS_FIELD = "infostatus";
  public static String OWNER_FIELD = "ownerreq";
  public static String REQUESTER_FIELD = "requester";
  public static String REQUESTED_FIELD = "requested";
  public static String ID_FIELD = "specialid";
  private File file;

  public XMLTransferDAO(String filePath) {
    this.file = new File(filePath);
  }

  public void close() {
  }

  public void delete(Transfer transfer) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public void deleteAll() throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public List<Transfer> getAll() throws DAOException {
    File arch = new File(Configuration.configuration.getArchivePath());
    File[] runnerFiles = arch.listFiles();
    List<Transfer> res = new ArrayList<Transfer>();
    for (File file : runnerFiles) {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = dbf.newDocumentBuilder().parse(file);
        // Setup XPath query
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xpe = xPath.compile(XML_GET_ALL);
        NodeList listNode = (NodeList) xpe.evaluate(document,
                                                    XPathConstants.NODESET);
        // Iterate through all found nodes
        for (int i = 0; i < listNode.getLength(); i++) {
          Node node = listNode.item(i);
          res.add(getFromNode(node));
        }
      } catch (SAXException e) {
        throw new DAOException(e);
      } catch (XPathExpressionException e) {
        throw new DAOException(e);
      } catch (ParserConfigurationException e) {
        throw new DAOException(e);
      } catch (IOException e) {
        throw new DAOException(e);
      }
    }
    return res;
  }

  private Transfer getFromNode(Node parent) {
    Transfer res = new Transfer();
    NodeList children = parent.getChildNodes();
    for (int j = 0; j < children.getLength(); j++) {
      Node node = children.item(j);
      if (node.getNodeName().equals(ID_FIELD)) {
        res.setId(Long.parseLong(node.getTextContent()));
      } else if (node.getNodeName().equals(OWNER_FIELD)) {
        res.setOwnerRequest(node.getTextContent());
      } else if (node.getNodeName().equals(REQUESTER_FIELD)) {
        res.setRequester(node.getTextContent());
      } else if (node.getNodeName().equals(REQUESTED_FIELD)) {
        res.setRequested(node.getTextContent());
      } else if (node.getNodeName().equals(RULE_FIELD)) {
        res.setRule(node.getTextContent());
      } else if (node.getNodeName().equals(RETRIEVE_MODE_FIELD)) {
        res.setRetrieveMode(Boolean.parseBoolean(node.getTextContent()));
      } else if (node.getNodeName().equals(TRANSFER_MODE_FIELD)) {
        res.setTransferMode(Integer.parseInt(node.getTextContent()));
      } else if (node.getNodeName().equals(FILENAME_FIELD)) {
        res.setFilename(node.getTextContent());
      } else if (node.getNodeName().equals(ORIGINAL_NAME_FIELD)) {
        res.setOriginalName(node.getTextContent());
      } else if (node.getNodeName().equals(FILE_INFO_FIELD)) {
        res.setFileInfo(node.getTextContent());
      } else if (node.getNodeName().equals(IS_MOVED_FIELD)) {
        res.setIsMoved(Boolean.parseBoolean(node.getTextContent()));
      } else if (node.getNodeName().equals(BLOCK_SIZE_FIELD)) {
        res.setBlockSize(Integer.parseInt(node.getTextContent()));
      } else if (node.getNodeName().equals(GLOBAL_STEP_FIELD)) {
        res.setGlobalStep(Transfer.TASKSTEP.valueOf(
            Integer.parseInt(node.getTextContent())));
      } else if (node.getNodeName().equals(LAST_GLOBAL_STEP_FIELD)) {
        res.setLastGlobalStep(Transfer.TASKSTEP.valueOf(
            Integer.parseInt(node.getTextContent())));
      } else if (node.getNodeName().equals(STEP_FIELD)) {
        res.setStep(Integer.parseInt(node.getTextContent()));
      } else if (node.getNodeName().equals(RANK_FIELD)) {
        res.setRank(Integer.parseInt(node.getTextContent()));
      } else if (node.getNodeName().equals(STEP_STATUS_FIELD)) {
        res.setStepStatus(ErrorCode.getFromCode(node.getTextContent()));
      } else if (node.getNodeName().equals(INFO_STATUS_FIELD)) {
        res.setInfoStatus(ErrorCode.getFromCode(node.getTextContent()));
      } else if (node.getNodeName().equals(START_FIELD)) {
        res.setStart(Timestamp.valueOf(node.getTextContent()));
      } else if (node.getNodeName().equals(STOP_FIELD)) {
        res.setStop(Timestamp.valueOf(node.getTextContent()));
      }
    }
    return res;
  }

  public boolean exist(long id, String requester, String requested,
                       String owner) throws DAOException {
    file = getFile(requester, requested, id);
    return file.exists();
  }

  private File getFile(String requester, String requested, long id) {
    return new File(Configuration.configuration.getBaseDirectory() +
                    Configuration.configuration.getArchivePath() + "/"
                    + requester + "_" + requested + "_"
                    + id + XMLEXTENSION);
  }

  public List<Transfer> find(List<Filter> fitlers) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  @Override
  public List<Transfer> find(List<Filter> filters, int limit)
      throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  @Override
  public List<Transfer> find(List<Filter> filters, int limit, int offset)
      throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  @Override
  public List<Transfer> find(List<Filter> filters, String column,
                             boolean ascend) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  @Override
  public List<Transfer> find(List<Filter> filters, String column,
                             boolean ascend, int limit) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  @Override
  public List<Transfer> find(List<Filter> filters, String column,
                             boolean ascend, int limit, int offset)
      throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public void insert(Transfer transfer) throws DAOException {
    //Set unique Id
    transfer.setId(new LongUuid().getLong());

    file = getFile(transfer.getRequester(), transfer.getRequested(),
                   transfer.getId());
    if (file.exists()) {
      throw new DAOException("File already exist");
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().newDocument();
      Element root = document.createElement(ROOT_LIST);
      document.appendChild(root);
      root.appendChild(getNode(document, transfer));
      // Write document in file
      XMLUtils.writeToFile(file, document);
    } catch (ParserConfigurationException e) {
      throw new DAOException(e);
    }
  }

  private Node getNode(Document doc, Transfer transfer) {
    Node res = doc.createElement(ROOT_ELEMENT);
    res.appendChild(XMLUtils.createNode(doc, ID_FIELD,
                                        Long.toString(transfer.getId())));
    res.appendChild(XMLUtils.createNode(doc, OWNER_FIELD,
                                        transfer.getOwnerRequest()));
    res.appendChild(XMLUtils.createNode(doc, REQUESTER_FIELD,
                                        transfer.getRequester()));
    res.appendChild(XMLUtils.createNode(doc, REQUESTED_FIELD,
                                        transfer.getRequested()));
    res.appendChild(XMLUtils.createNode(doc, RULE_FIELD,
                                        transfer.getRule()));
    res.appendChild(XMLUtils.createNode(doc, RETRIEVE_MODE_FIELD,
                                        Boolean.toString(
                                            transfer.getRetrieveMode())));
    res.appendChild(XMLUtils.createNode(doc, TRANSFER_MODE_FIELD,
                                        Integer.toString(
                                            transfer.getTransferMode())));
    res.appendChild(XMLUtils.createNode(doc, FILENAME_FIELD,
                                        transfer.getRequested()));
    res.appendChild(XMLUtils.createNode(doc, ORIGINAL_NAME_FIELD,
                                        transfer.getFilename()));
    res.appendChild(XMLUtils.createNode(doc, REQUESTED_FIELD,
                                        transfer.getOriginalName()));
    res.appendChild(XMLUtils.createNode(doc, FILE_INFO_FIELD,
                                        transfer.getFileInfo()));
    res.appendChild(XMLUtils.createNode(doc, IS_MOVED_FIELD,
                                        Boolean
                                            .toString(transfer.getIsMoved())));
    res.appendChild(XMLUtils.createNode(doc, BLOCK_SIZE_FIELD,
                                        Integer.toString(
                                            transfer.getBlockSize())));
    res.appendChild(XMLUtils.createNode(doc, GLOBAL_STEP_FIELD,
                                        Integer.toString(
                                            transfer.getGlobalStep()
                                                    .ordinal())));
    res.appendChild(XMLUtils.createNode(doc, LAST_GLOBAL_STEP_FIELD,
                                        Integer.toString(
                                            transfer.getLastGlobalStep()
                                                    .ordinal())));
    res.appendChild(XMLUtils.createNode(doc, STEP_FIELD,
                                        Integer.toString(transfer.getStep())));
    res.appendChild(XMLUtils.createNode(doc, RANK_FIELD,
                                        Integer.toString(transfer.getRank())));
    res.appendChild(XMLUtils.createNode(doc, STEP_STATUS_FIELD,
                                        transfer.getStepStatus().getCode()));
    res.appendChild(XMLUtils.createNode(doc, INFO_STATUS_FIELD,
                                        transfer.getInfoStatus().getCode()));
    res.appendChild(XMLUtils.createNode(doc, START_FIELD,
                                        transfer.getStart().toString()));
    res.appendChild(XMLUtils.createNode(doc, STOP_FIELD,
                                        transfer.getStop().toString()));
    return res;
  }

  public Transfer select(long id, String requester, String requested,
                         String owner) throws DAOException {
    file = getFile(requester, requested, id);
    if (!file.exists()) {
      return null;
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().parse(file);
      // Setup XPath variable
      SimpleVariableResolver resolver = new SimpleVariableResolver();
      resolver.addVariable(new QName(null, "specialid"), id);
      resolver.addVariable(new QName(null, "requester"), requester);
      resolver.addVariable(new QName(null, "requested"), requested);
      resolver.addVariable(new QName(null, "owner"), owner);
      // Setup XPath query
      XPath xPath = XPathFactory.newInstance().newXPath();
      xPath.setXPathVariableResolver(resolver);
      XPathExpression xpe = xPath.compile(XML_SELECT);
      // Retrieve node and instantiate object
      Node node = (Node) xpe.evaluate(document, XPathConstants.NODE);
      if (node != null) {
        return getFromNode(node);
      }
      return null;
    } catch (SAXException e) {
      throw new DAOException(e);
    } catch (XPathExpressionException e) {
      throw new DAOException(e);
    } catch (ParserConfigurationException e) {
      throw new DAOException(e);
    } catch (IOException e) {
      throw new DAOException(e);
    }
  }

  public void update(Transfer transfer) throws DAOException {
    file = getFile(transfer.getRequester(), transfer.getRequested(),
                   transfer.getId());
    if (!file.exists()) {
      throw new DAOException("File doesn't exist");
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().parse(file);
      // Setup XPath variable
      SimpleVariableResolver resolver = new SimpleVariableResolver();
      resolver.addVariable(new QName(null, "specialid"), transfer.getId());
      resolver
          .addVariable(new QName(null, "requester"), transfer.getRequester());
      resolver
          .addVariable(new QName(null, "requested"), transfer.getRequested());
      resolver
          .addVariable(new QName(null, "ownerreq"), transfer.getOwnerRequest());
      // Setup XPath query
      XPath xPath = XPathFactory.newInstance().newXPath();
      xPath.setXPathVariableResolver(resolver);
      XPathExpression xpe = xPath.compile(XML_SELECT);
      // Retrieve node and remove it
      Node node = (Node) xpe.evaluate(document, XPathConstants.NODE);
      if (node == null) {
        logger.warn("Entry not found cannot update");
        return;
      }
      node.getParentNode().removeChild(node);
      // Insert updated node
      Element root = document.getDocumentElement();
      root.appendChild(getNode(document, transfer));
      // Write document in file
      XMLUtils.writeToFile(file, document);
    } catch (SAXException e) {
      throw new DAOException(e);
    } catch (XPathExpressionException e) {
      throw new DAOException(e);
    } catch (ParserConfigurationException e) {
      throw new DAOException(e);
    } catch (IOException e) {
      throw new DAOException(e);
    }
  }
}
