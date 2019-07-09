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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.openr66.dao.Filter;
import org.waarp.openr66.dao.LimitDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Limit;
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
import java.util.ArrayList;
import java.util.List;

//TODO
public class XMLimitDAO implements LimitDAO {

  public static final String HOSTID_FIELD = "hostid";
  public static final String SESSION_LIMIT_FILED = "sessionlimit";
  public static final String GOLBAL_LIMIT_FILED = "globallimit";
  public static final String DELAY_LIMIT_FILED = "delaylimit";
  public static final String RUN_LIMIT_FILED = "runlimit";
  public static final String DELAY_COMMAND_LIMIT_FILED = "delaycommand";
  public static final String DELAY_RETRY_LIMIT_FILED = "delayretry";
  private static final WaarpLogger logger =
      WaarpLoggerFactory.getLogger(XMLimitDAO.class);
  private static final String XML_SELECT = "/config/identity[hostid=$hostid]";

  private File file;

  public XMLimitDAO(String filePath) throws DAOException {
    this.file = new File(filePath);
  }

  public void close() {
  }

  public void delete(Limit limit) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public void deleteAll() throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public List<Limit> getAll() throws DAOException {
    if (!file.exists()) {
      throw new DAOException("File doesn't exist");
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().parse(file);
      // File contains only 1 entry
      List<Limit> res = new ArrayList<Limit>(1);
      res.add(getFromNode(document.getDocumentElement()));
      // Iterate through all found nodes
      return res;
    } catch (SAXException e) {
      throw new DAOException(e);
    } catch (ParserConfigurationException e) {
      throw new DAOException(e);
    } catch (IOException e) {
      throw new DAOException(e);
    }
  }

  private Limit getFromNode(Node parent) {
    Limit res = new Limit();

    NodeList children = parent.getChildNodes();
    for (int j = 0; j < children.getLength(); j++) {
      Node node = children.item(j);
      if (node.getNodeName().equals(HOSTID_FIELD)) {
        res.setHostid(node.getTextContent());
      }
    }
    return res;
  }

  public boolean exist(String hostid) throws DAOException {
    if (!file.exists()) {
      throw new DAOException("File doesn't exist");
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().parse(file);
      // Setup XPath variable
      SimpleVariableResolver resolver = new SimpleVariableResolver();
      resolver.addVariable(new QName(null, "hostid"), hostid);
      // Setup XPath query
      XPath xPath = XPathFactory.newInstance().newXPath();
      xPath.setXPathVariableResolver(resolver);
      XPathExpression xpe = xPath.compile(XML_SELECT);
      // Query will return "" if nothing is found
      return (!"".equals(xpe.evaluate(document)));
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

  public List<Limit> find(List<Filter> fitlers) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public void insert(Limit limit) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  public Limit select(String hostid) throws DAOException {
    if (!file.exists()) {
      throw new DAOException("File doesn't exist");
    }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().parse(file);
      // Setup XPath variable
      SimpleVariableResolver resolver = new SimpleVariableResolver();
      resolver.addVariable(new QName(null, "hostid"), hostid);
      // Setup XPath query
      XPath xPath = XPathFactory.newInstance().newXPath();
      xPath.setXPathVariableResolver(resolver);
      XPathExpression xpe = xPath.compile(XML_SELECT);
      // Retrieve node and instantiate object
      Node node = (Node) xpe.evaluate(document, XPathConstants.NODE);
      if (node != null) {
        return getFromNode(document.getDocumentElement());
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

  public void update(Limit limit) throws DAOException {
    throw new DAOException("Operation not supported on XML DAO");
  }

  private Node getNode(Document doc, Limit limit) {
    Node res = doc.createElement("config");
    Node tmp = res.appendChild(doc.createElement("identity"));
    tmp.appendChild(XMLUtils.createNode(doc, HOSTID_FIELD,
                                        limit.getHostid()));
    tmp = res.appendChild(doc.createElement("limit"));
    return res;
  }
}
