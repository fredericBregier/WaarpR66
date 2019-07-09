package org.waarp.openr66.dao.xml;

import org.waarp.openr66.dao.BusinessDAO;
import org.waarp.openr66.dao.DAOFactory;
import org.waarp.openr66.dao.HostDAO;
import org.waarp.openr66.dao.LimitDAO;
import org.waarp.openr66.dao.MultipleMonitorDAO;
import org.waarp.openr66.dao.RuleDAO;
import org.waarp.openr66.dao.TransferDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.protocol.configuration.Configuration;

public class XMLDAOFactory extends DAOFactory {

    private String confDir = Configuration.configuration.getConfigPath();

    private String businessFile = confDir + "/business.xml";
    private String hostFile = Configuration.configuration.getAUTH_FILE();
    private String limitFile = confDir + "/limit.xml";
    private String ruleFile = confDir;
    private String transferFile = Configuration.configuration.getArchivePath();

    public XMLDAOFactory() {
    }


    @Override
    public BusinessDAO getBusinessDAO() throws DAOException {
        return new XMLBusinessDAO(businessFile);
    }

    @Override
    public HostDAO getHostDAO() throws DAOException {
        return new XMLHostDAO(hostFile);
    }

    @Override
    public LimitDAO getLimitDAO() throws DAOException {
        return new XMLimitDAO(limitFile);
    }

    @Override
    public MultipleMonitorDAO getMultipleMonitorDAO() throws DAOException {
        throw new DAOException("MultipleMonitor is not supported on XML DAO");
    }

    @Override
    public RuleDAO getRuleDAO() throws DAOException {
        return new XMLRuleDAO(ruleFile);
    }

    @Override
    public TransferDAO getTransferDAO() throws DAOException {
        return new XMLTransferDAO(transferFile);
    }
}
