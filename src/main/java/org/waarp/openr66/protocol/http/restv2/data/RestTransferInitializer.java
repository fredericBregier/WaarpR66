package org.waarp.openr66.protocol.http.restv2.data;

import org.waarp.openr66.dao.RuleDAO;
import org.waarp.openr66.dao.exception.DAOException;
import org.waarp.openr66.pojo.Transfer;
import org.waarp.openr66.protocol.http.restv2.RestUtils;
import org.waarp.openr66.protocol.http.restv2.errors.BadRequestResponse;
import org.waarp.openr66.protocol.http.restv2.exception.OpenR66RestBadRequestException;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class RestTransferInitializer {

    @NotEmpty
    public String ruleID;

    @NotEmpty
    public String fileName;

    @NotEmpty
    public String requested;

    @Or(@Bounds(min = 0, max = Integer.MAX_VALUE))
    public Integer blockSize = 4096;

    public String fileInfo = "";

    /* Since the ObjectMapper cannot directly extract a Calendar from a String in ISO-8601 format, the value of
     * the field `start` is assigned by the `setStart' method which takes the String as an argument. */
    private Calendar start = null;

    /**
     * Extracts a date from the String argument and assigns it to the `start` field. The String must represent a date
     * in ISO-8601 format and cannot be in the past.
     * @param date  The String from which the date is extracted.
     * @throws IllegalArgumentException Thrown if the date entered not in the right format or
     */
    public void setStart(String date) throws IllegalArgumentException {
        if(date != null && !date.isEmpty()) {
            Calendar start = RestUtils.toCalendar(date);
            if (start.before(new GregorianCalendar())) {
                throw new IllegalArgumentException();
            } else {
                this.start = start;
            }
        }
    }

    /**
     * Transforms this RestTransferInitializer instance into a database Transfer instance.
     * @return The new Transfer created from this initializer instance.
     * @throws DAOException Thrown if the database could not be reached.
     * @throws OpenR66RestBadRequestException Thrown if the rule entered is not a valid ruleID.
     */
    public Transfer toTransfer() throws DAOException, OpenR66RestBadRequestException {
        RuleDAO ruleDAO = RestUtils.factory.getRuleDAO();
        if(ruleDAO.exist(this.ruleID)) {
            int mode = ruleDAO.select(this.ruleID).getMode();
            boolean retrieve = (mode == 2 || mode == 4);
            Transfer transfer = new Transfer(this.ruleID, mode, retrieve, this.fileName, this.fileInfo, this.blockSize);
            transfer.setStart(new Timestamp(this.start.getTimeInMillis()));

            return transfer;
        } else {
            throw new OpenR66RestBadRequestException(
                    new BadRequestResponse().illegalFieldValue(this.getClass(), "ruleID"));
        }
    }
}