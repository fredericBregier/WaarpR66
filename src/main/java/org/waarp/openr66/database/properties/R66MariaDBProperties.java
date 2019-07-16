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

package org.waarp.openr66.database.properties;

public class R66MariaDBProperties extends R66DbProperties {

  @Override
  public String getCreateQuery() {
    StringBuilder builder = new StringBuilder();

    builder.append("CREATE TABLE MULTIPLEMONITOR (" +
                   "COUNTCONFIG INTEGER NOT NULL, " +
                   "COUNTHOST INTEGER NOT NULL, " +
                   "COUNTRULE INTEGER NOT NULL, " +
                   "HOSTID VARCHAR(8096) PRIMARY KEY);\n");
    builder.append("CREATE TABLE CONFIGURATION (" +
                   "READGLOBALLIMIT BIGINT NOT NULL, " +
                   "WRITEGLOBALLIMIT BIGINT NOT NULL," +
                   "READSESSIONLIMIT BIGINT NOT NULL," +
                   "WRITESESSIONLIMIT BIGINT NOT NULL," +
                   "DELAYLIMIT BIGINT NOT NULL," +
                   "UPDATEDINFO INTEGER NOT NULL," +
                   "HOSTID VARCHAR(8096) PRIMARY KEY);");
    builder.append("CREATE TABLE HOSTCONFIG (" +
                   "BUSINESS TEXT NOT NULL ," +
                   "ROLES TEXT NOT NULL ," +
                   "ALIASES TEXT NOT NULL ," +
                   "OTHERS TEXT NOT NULL ," +
                   "UPDATEDINFO INTEGER NOT NULL ," +
                   "HOSTID VARCHAR(8096) PRIMARY KEY);\n");
    builder.append("CREATE TABLE HOSTS (" +
                   "ADDRESS VARCHAR(8096) NOT NULL ," +
                   "PORT INTEGER NOT NULL ," +
                   "ISSSL BOOLEAN NOT NULL ," +
                   "HOSTKEY BYTEA NOT NULL ," +
                   "ADMINROLE BOOLEAN NOT NULL ," +
                   "ISCLIENT BOOLEAN NOT NULL ," +
                   "ISACTIVE BOOLEAN NOT NULL ," +
                   "ISPROXIFIED BOOLEAN NOT NULL ," +
                   "UPDATEDINFO INTEGER NOT NULL ," +
                   "HOSTID VARCHAR(8096) PRIMARY KEY);\n");
    builder.append("CREATE TABLE RULES (" +
                   "HOSTIDS TEXT ," +
                   "MODETRANS INTEGER ," +
                   "RECVPATH VARCHAR(8096)," +
                   "SENDPATH VARCHAR(8096) ," +
                   "ARCHIVEPATH VARCHAR(8096) ," +
                   "WORKPATH VARCHAR(8096) ," +
                   "RPRETASKS TEXT ," +
                   "RPOSTTASKS TEXT ," +
                   "RERRORTASKS TEXT ," +
                   "SPRETASKS TEXT ," +
                   "SPOSTTASKS TEXT ," +
                   "SERRORTASKS TEXT ," +
                   "UPDATEDINFO INTEGER ," +
                   "IDRULE VARCHAR(8096) PRIMARY KEY);\n");
    builder.append("CREATE TABLE RUNNER (" +
                   "GLOBALSTEP INTEGER NOT NULL ," +
                   "GLOBALLASTSTEP INTEGER NOT NULL ," +
                   "STEP INTEGER NOT NULL ," +
                   "RANK INTEGER NOT NULL ," +
                   "STEPSTATUS CHAR(3) NOT NULL ," +
                   "RETRIEVEMODE BOOLEAN NOT NULL ," +
                   "FILENAME VARCHAR(8096) NOT NULL ," +
                   "ISMOVED BOOLEAN NOT NULL ," +
                   "IDRULE VARCHAR(8096) NOT NULL ," +
                   "BLOCKSZ INTEGER NOT NULL ," +
                   "ORIGINALNAME VARCHAR(8096) NOT NULL ," +
                   "FILEINFO TEXT NOT NULL ," +
                   "TRANSFERINFO TEXT NOT NULL ," +
                   "MODETRANS INTEGER NOT NULL ," +
                   "STARTTRANS TIMESTAMP(3) NOT NULL ," +
                   "STOPTRANS TIMESTAMP(3) NOT NULL ," +
                   "INFOSTATUS CHAR(3) NOT NULL ," +
                   "UPDATEDINFO INTEGER NOT NULL ," +
                   "OWNERREQ VARCHAR(8096) NOT NULL ," +
                   "REQUESTER VARCHAR(8096) NOT NULL ," +
                   "REQUESTED VARCHAR(8096) NOT NULL ," +
                   "SPECIALID BIGINT NOT NULL ," +
                   "CONSTRAINT runner_pk PRIMARY KEY (OWNERREQ, REQUESTER, REQUESTED, SPECIALID));\n");
    builder.append("CREATE INDEX IDX_RUNNER ON RUNNER (" +
                   "STARTTRANS, OWNERREQ, STEPSTATUS, UPDATEDINFO, GLOBALSTEP, " +
                   "INFOSTATUS, SPECIALID);\n");
    builder.append("CREATE SEQUENCE RUNSEQ " +
                   "MINVALUE -9223372036854775807 " +
                   "START WITH -9223372036854775807;\n");
    return builder.toString();
  }
}
