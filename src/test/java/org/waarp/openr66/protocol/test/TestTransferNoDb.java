/**
 * This file is part of Waarp Project.
 * <p>
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the COPYRIGHT.txt in the
 * distribution for a full listing of individual contributors.
 * <p>
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.protocol.test;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.openr66.client.DirectTransfer;
import org.waarp.openr66.context.ErrorCode;
import org.waarp.openr66.context.R66Result;
import org.waarp.openr66.database.DbConstant;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.waarp.openr66.protocol.networkhandler.NetworkTransaction;
import org.waarp.openr66.protocol.utils.R66Future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test class for multiple DirectTransfer
 *
 * @author Frederic Bregier
 *
 */
public class TestTransferNoDb extends DirectTransfer {
    static int nb = 100;

    public TestTransferNoDb(R66Future future, String remoteHost,
                            String filename, String rulename, String fileinfo, boolean isMD5, int blocksize,
                            long id,
                            NetworkTransaction networkTransaction) {
        super(future, remoteHost, filename, rulename, fileinfo, isMD5, blocksize, id,
              networkTransaction);
    }

    /**
     * @param args
     * @param rank
     * @return True if OK
     */
    protected static boolean getSpecialParams(String[] args, int rank) {
        for (int i = rank; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-nb")) {
                i++;
                nb = Integer.parseInt(args[i]);
            } else if (args[i].equalsIgnoreCase("-md5")) {
            } else if (args[i].charAt(0) == '-') {
                i++;// jump one
            }
        }
        return true;
    }

    public static void main(String[] args) {
        WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(
                null));
        if (logger == null) {
            logger = WaarpLoggerFactory.getLogger(DirectTransfer.class);
        }
        if (!getParams(args, false)) {
            logger.error("Wrong initialization");
            if (DbConstant.admin != null && DbConstant.admin.isActive()) {
                DbConstant.admin.close();
            }
            System.exit(1);
        }
        getSpecialParams(args, 1);
        Configuration.configuration.setCLIENT_THREAD(nb);
        Configuration.configuration.pipelineInit();
        NetworkTransaction networkTransaction = new NetworkTransaction();
        try {
            ExecutorService executorService = Executors.newCachedThreadPool();

            R66Future[] arrayFuture = new R66Future[nb];
            logger.info("Start of Test Transfer");
            long time1 = System.currentTimeMillis();
            for (int i = 0; i < nb; i++) {
                arrayFuture[i] = new R66Future(true);
                TestTransferNoDb transaction = new TestTransferNoDb(arrayFuture[i],
                                                                    rhost, localFilename, rule, fileInfo, ismd5, block,
                                                                    DbConstant.ILLEGALVALUE,
                                                                    networkTransaction);
                transaction.normalInfoAsWarn = snormalInfoAsWarn;
                executorService.execute(transaction);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            int success = 0;
            int error = 0;
            int warn = 0;
            for (int i = 0; i < nb; i++) {
                arrayFuture[i].awaitUninterruptibly();
                R66Result result = arrayFuture[i].getResult();
                if (arrayFuture[i].isSuccess()) {
                    if (result.getRunner().getErrorInfo() == ErrorCode.Warning) {
                        warn++;
                    } else {
                        success++;
                    }
                } else {
                    if (result.getRunner() != null &&
                        result.getRunner().getErrorInfo() == ErrorCode.Warning) {
                        warn++;
                    } else {
                        error++;
                    }
                }
            }
            long time2 = System.currentTimeMillis();
            long length = 0;
            // Get the first result as testing only
            R66Result result = arrayFuture[0].getResult();
            logger.warn("Final file: " +
                        (result.getFile() != null? result.getFile().toString() : "no file"));
            try {
                length = result.getFile() != null? result.getFile().length() : 0L;
            } catch (CommandAbstractException e) {
            }
            long delay = time2 - time1;
            float nbs = success * 1000;
            nbs /= delay;
            float mbs = nbs * length / 1024;
            logger.warn("Success: " + success + " Warning: " + warn + " Error: " +
                        error + " delay: " + delay + " NB/s: " + nbs + " KB/s: " + mbs);
            executorService.shutdown();
        } finally {
            networkTransaction.closeAll();
        }
    }

}
