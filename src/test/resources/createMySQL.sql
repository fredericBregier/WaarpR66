use test;

--
-- Name: configuration; Type: TABLE;
--

CREATE TABLE CONFIGURATION (
    readgloballimit bigint NOT NULL,
    writegloballimit bigint NOT NULL,
    readsessionlimit bigint NOT NULL,
    writesessionlimit bigint NOT NULL,
    delaylimit bigint NOT NULL,
    updatedinfo integer NOT NULL,
    hostid character varying(255) NOT NULL,
    PRIMARY KEY (hostid)
);

--
-- Name: hostconfig; Type: TABLE;
--

CREATE TABLE HOSTCONFIG (
    business text NOT NULL,
    roles text NOT NULL,
    aliases text NOT NULL,
    others text NOT NULL,
    updatedinfo integer NOT NULL,
    hostid character varying(255) NOT NULL,
    PRIMARY KEY (hostid)
);

--
-- Name: hosts; Type: TABLE;
--

CREATE TABLE HOSTS (
    address character varying(255) NOT NULL,
    port integer NOT NULL,
    isssl boolean NOT NULL,
    hostkey varbinary(255) NOT NULL,
    adminrole boolean NOT NULL,
    isclient boolean NOT NULL,
    isactive boolean NOT NULL,
    isproxified boolean NOT NULL,
    updatedinfo integer NOT NULL,
    hostid character varying(255) NOT NULL,
    PRIMARY KEY (hostid)
);

--
-- Name: multiplemonitor; Type: TABLE;
--

CREATE TABLE MULTIPLEMONITOR (
    countconfig integer NOT NULL,
    counthost integer NOT NULL,
    countrule integer NOT NULL,
    hostid character varying(255) NOT NULL,
    PRIMARY KEY (hostid)
);

--
-- Name: rules; Type: TABLE;
--

CREATE TABLE RULES (
    hostids text,
    modetrans integer,
    recvpath character varying(255),
    sendpath character varying(255),
    archivepath character varying(255),
    workpath character varying(255),
    rpretasks text,
    rposttasks text,
    rerrortasks text,
    spretasks text,
    sposttasks text,
    serrortasks text,
    updatedinfo integer,
    idrule character varying(255) NOT NULL,
    PRIMARY KEY (idrule)
);

--
-- Name: runner; Type: TABLE;
--

CREATE TABLE RUNNER (
    globalstep integer NOT NULL,
    globallaststep integer NOT NULL,
    step integer NOT NULL,
    rank integer NOT NULL,
    stepstatus character(3) NOT NULL,
    retrievemode boolean NOT NULL,
    filename character varying(255) NOT NULL,
    ismoved boolean NOT NULL,
    idrule character varying(255) NOT NULL,
    blocksz integer NOT NULL,
    originalname character varying(255) NOT NULL,
    fileinfo text NOT NULL,
    transferinfo text NOT NULL,
    modetrans integer NOT NULL,
    starttrans timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stoptrans timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    infostatus character(3) NOT NULL,
    updatedinfo integer NOT NULL,
    ownerreq character varying(255) NOT NULL,
    requester character varying(255) NOT NULL,
    requested character varying(255) NOT NULL,
    specialid bigint NOT NULL,
    PRIMARY KEY (ownerreq, requester, requested, specialid)
);
