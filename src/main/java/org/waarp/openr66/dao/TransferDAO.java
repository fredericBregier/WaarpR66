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
import org.waarp.openr66.pojo.Transfer;

import java.util.List;

/**
 * Interface to interact with Transfer objects in the persistance layer
 */
public interface TransferDAO {

  /**
   * Retrieve all Transfer objects in a List from the persistance layer
   *
   * @throws DAOException If data access error occurs
   */
  List<Transfer> getAll() throws DAOException;

  /**
   * Retrieve all Transfer objects to the given filters in a List from the
   * persistance layer
   *
   * @param filters List of filter
   *
   * @throws DAOException If data access error occurs
   */
  List<Transfer> find(List<Filter> filters) throws DAOException;

  List<Transfer> find(List<Filter> filters, int limit) throws DAOException;

  List<Transfer> find(List<Filter> filters, int limit, int offset)
      throws DAOException;


  List<Transfer> find(List<Filter> filters, String column, boolean ascend)
      throws DAOException;

  List<Transfer> find(List<Filter> filters, String column, boolean ascend,
                      int limit) throws DAOException;

  List<Transfer> find(List<Filter> filters, String column, boolean ascend,
                      int limit, int offset) throws DAOException;


  /**
   * Retrieve the Transfer object with the specified Special ID from the
   * persistance layer
   *
   * @param id special ID of the Transfer object requested
   *
   * @throws DAOException If a data access error occurs
   */
  Transfer select(long id, String requester, String requested, String owner)
      throws DAOException;

  /**
   * Verify if a Transfer object with the specified Special ID exists in the
   * persistance layer
   *
   * @param id special ID of the Transfer object verified
   *
   * @return true if a Transfer object with the specified Special ID exist;
   *     false if no Transfer object correspond to
   *     the specified Special ID.
   *
   * @throws DAOException If a data access error occurs
   */
  boolean exist(long id, String requester, String requested, String owner)
      throws DAOException;

  /**
   * Insert the specified Transfer object in the persistance layer
   *
   * @param transfer Transfer object to insert
   *
   * @throws DAOException If a data access error occurs
   */
  void insert(Transfer transfer) throws DAOException;

  /**
   * Update the specified Transfer object in the persistance layer
   *
   * @param transfer Transfer object to update
   *
   * @throws DAOException If a data access error occurs
   */
  void update(Transfer transfer) throws DAOException;

  /**
   * Remove the specified Transfer object from the persistance layer
   *
   * @param transfer Transfer object to insert
   *
   * @throws DAOException If a data access error occurs
   */
  void delete(Transfer transfer) throws DAOException;

  /**
   * Remove all Transfer objects from the persistance layer
   *
   * @throws DAOException If a data access error occurs
   */
  void deleteAll() throws DAOException;

  void close();
}
