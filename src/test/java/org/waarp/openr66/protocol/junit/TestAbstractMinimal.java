/**
 *
 */
package org.waarp.openr66.protocol.junit;

import org.waarp.common.file.FileUtils;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.common.utility.DetectionUtils;
import org.waarp.openr66.protocol.configuration.Configuration;

import java.io.File;
import java.io.FileFilter;

/**
 * @author frederic
 *
 */
public abstract class TestAbstractMinimal {
    /**
     * Internal Logger
     */
    protected static WaarpLogger logger;
    protected static File dir;

    /**
     * @throws java.lang.Exception
     */
    public static void setUpBeforeClassMinimal(String serverInit)
            throws Exception {
        WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
        if (logger == null) {
            logger = WaarpLoggerFactory.getLogger(TestAbstractMinimal.class);
        }
        ClassLoader classLoader = TestAbstractMinimal.class.getClassLoader();
        DetectionUtils.setJunit(true);
        File file = new File(classLoader.getResource(serverInit).getFile());
        if (file.exists()) {
            dir = file.getParentFile();
            File tmp = new File("/tmp/R66");
            tmp.mkdirs();
            FileUtils.forceDeleteRecursiveDir(tmp);
            new File(tmp, "in").mkdir();
            new File(tmp, "out").mkdir();
            new File(tmp, "arch").mkdir();
            new File(tmp, "work").mkdir();
            File conf = new File(tmp, "conf");
            conf.mkdir();
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }
            });
            File [] copied = FileUtils.copyRecursive(dir, conf, false);
            for (File fileCopied : copied) {
                System.out.print(fileCopied.getAbsolutePath() + " ");
            }
            System.out.println(" Done");
        } else {
            System.err.println(
                    "Cannot find serverInit file: " + file.getAbsolutePath());
        }
    }

    /**
     *
     */
    public static void tearDownAfterClassMinimal() {
        File tmp = new File("/tmp/R66");
        tmp.mkdirs();
        FileUtils.forceDeleteRecursiveDir(tmp);
    }

}
