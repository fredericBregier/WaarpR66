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

package org.waarp.openr66.dao;

import org.waarp.common.database.ConnectionFactory;
import org.waarp.openr66.dao.database.DBDAOFactory;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.dao.xml.XMLDAOFactory;


/**
 * Abstract class to create DAOFactory
 */
public abstract class DAOFactory {

  private static DAOFactory instance;

  public static void initialize() {
    if (instance == null) {
      instance = new XMLDAOFactory();
    }
  }

  public static void initialize(ConnectionFactory factory) {
    if (instance == null) {
      instance = new DBDAOFactory(factory);
    }
  }

  public static DAOFactory getInstance() {
    return instance;
  }

  /**
   * Return a BusinessDAO
   *
   * @return a ready to use BusinessDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract BusinessDAO getBusinessDAO() throws DAOException;

  /**
   * Return a HostDAO
   *
   * @return a ready to use HostDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract HostDAO getHostDAO() throws DAOException;

  /**
   * Return a LimitDAO
   *
   * @return a ready to use LimitDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract LimitDAO getLimitDAO() throws DAOException;

  /**
   * Return a MultipleMonitorDAO
   *
   * @return a ready to use MultipleMonitorDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract MultipleMonitorDAO getMultipleMonitorDAO()
      throws DAOException;

  /**
   * Return a RuleDAO
   *
   * @return a ready to use RuleDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract RuleDAO getRuleDAO() throws DAOException;

  /**
   * Return a TransferDAO
   *
   * @return a ready to use TramsferDAO
   *
   * @throws DAOException if cannot create the DAO
   */
  public abstract TransferDAO getTransferDAO() throws DAOException;
}
