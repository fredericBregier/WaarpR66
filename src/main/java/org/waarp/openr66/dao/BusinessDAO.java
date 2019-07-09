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

import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Business;

import java.util.List;

/**
 * Interface to interact with Business objects in the persistance layer
 */
public interface BusinessDAO {

  /**
   * Retrieve all Business objects in a List from the persistance layer
   *
   * @throws DAOException If data access error occurs
   */
  List<Business> getAll() throws DAOException;

  /**
   * Retrieve all Business objects corresponding to the given filters in a List
   * from the persistance layer
   *
   * @param filters List of filter
   *
   * @throws DAOException If data access error occurs
   */
  List<Business> find(List<Filter> filters) throws DAOException;

  /**
   * Retrieve the Business object with the specified hostid from the persistance
   * layer
   *
   * @param hostid Hostid of the Business object requested
   *
   * @throws DAOException If a data access error occurs
   */
  Business select(String hostid) throws DAOException;

  /**
   * Verify if a Business object with the specified hostid exists in the
   * persistance layer
   *
   * @param hostid Hostid of the Business object verified
   *
   * @return true if a Business object with the specified hostid exist; false if
   *     no Business object correspond to the
   *     specified hostid.
   *
   * @throws DAOException If a data access error occurs
   */
  boolean exist(String hostid) throws DAOException;

  /**
   * Insert the specified Business object in the persistance layer
   *
   * @param business Business object to insert
   *
   * @throws DAOException If a data access error occurs
   */
  void insert(Business business) throws DAOException;

  /**
   * Update the specified Business object in the persistance layer
   *
   * @param business Business object to update
   *
   * @throws DAOException If a data access error occurs
   */
  void update(Business business) throws DAOException;

  /**
   * Remove the specified Business object from the persistance layer
   *
   * @param business Business object to insert
   *
   * @throws DAOException If a data access error occurs
   */
  void delete(Business business) throws DAOException;

  /**
   * Remove all Business objects from the persistance layer
   *
   * @throws DAOException If a data access error occurs
   */
  void deleteAll() throws DAOException;

  void close();
}
