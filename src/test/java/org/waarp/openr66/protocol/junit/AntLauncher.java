package org.waarp.openr66.protocol.junit;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.common.utility.DetectionUtils;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.waarp.openr66.server.ServerInitDatabase;

import java.io.File;
import java.io.PrintStream;

public abstract class AntLauncher extends TestAbstractMinimal {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger =
            WaarpLoggerFactory.getLogger(AntLauncher.class);
    private static Project project;
    private static File homeDir;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
        String serverInit = "Linux/config/config-serverInitB.xml";
        setUpBeforeClassMinimal(serverInit);

        ClassLoader classLoader = AntLauncher.class.getClassLoader();
        DetectionUtils.setJunit(true);
        File file = new File(classLoader.getResource(serverInit).getFile());
        String newfile = file.getAbsolutePath().replace("target/test-classes",
                                                        "src/test/resources");
        file = new File(newfile);
        dir = file.getParentFile();
        logger.warn("File {} exists? {}", file, file.isFile());
        homeDir = dir.getParentFile().getParentFile().getParentFile()
                     .getParentFile().getParentFile();
        logger.warn("Dir {} exists? {}", homeDir, homeDir.isDirectory());

        // global ant project settings
        project = new Project();
        project.setBaseDir(homeDir);
        project.init();
        DefaultLogger listener = new DefaultLogger();
        project.addBuildListener(listener);
        listener.setOutputPrintStream(System.out);
        listener.setErrorPrintStream(System.err);
        listener.setMessageOutputLevel(Project.MSG_WARN);
        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
        project.fireBuildStarted();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        project.log("finished");
        project.fireBuildFinished(null);
    }

    @Test
    public void test0_InitiateDbB() {
        DetectionUtils.setJunit(true);
        String serverInit = "config-serverInitB.xml";
        File file = new File(dir, serverInit);
        logger.warn("File {} exists? {}", file, file.isFile());

        String[] args = new String[] {
                file.getAbsolutePath(), "-initdb", "-dir",
                dir.getAbsolutePath(), "-auth",
                new File(dir, "OpenR66-authent-A.xml").getAbsolutePath(),
                "-limit",
                new File(dir, "limitConfiga.xml").getAbsolutePath()
        };
        executeJvm(homeDir, ServerInitDatabase.class, args);
    }

    public static void executeJvm(File homeDir, Class<?> zclass,
                                  String[] args) {
        Throwable caught = null;
        try {
            /** initialize an java task **/
            Java javaTask = new Java();
            javaTask.setNewenvironment(false);
            javaTask.setTaskName(zclass.getSimpleName());
            javaTask.setProject(project);
            javaTask.setFork(true);
            javaTask.setFailonerror(false);
            javaTask.setClassname(zclass.getName());

            // add some vm args
            Argument jvmArgs = javaTask.createJvmarg();
            jvmArgs.setLine("-Xms512m -Xmx1024m");

            // added some args for to class to launch
            Argument taskArgs = javaTask.createArg();
            StringBuilder builder = new StringBuilder();
            for (String string : args) {
                builder.append(' ').append(string);
            }
            taskArgs.setLine(builder.toString());

            /** set the class path */
            String classpath = System.getProperty("java.class.path");
            Path classPath = javaTask.createClasspath();
            classPath.setPath(classpath);
            javaTask.setClasspath(classPath);

            javaTask.init();
            int ret = javaTask.executeJava();
            System.err.println("return code: " + ret);

        } catch (BuildException e) {
            caught = e;
        }
    }

    @Test
    public void test1_testInitiateDbA() {
        DetectionUtils.setJunit(true);
        String serverInit = "config-serverInitA.xml";
        File file = new File(dir, serverInit);
        logger.warn("File {} exists? {}", file, file.isFile());

        String[] args = new String[] {
                file.getAbsolutePath(), "-initdb", "-dir",
                dir.getAbsolutePath(), "-auth",
                new File(dir, "OpenR66-authent-A.xml").getAbsolutePath(),
                "-limit",
                new File(dir, "limitConfiga.xml").getAbsolutePath()
        };
        executeJvm(homeDir, ServerInitDatabase.class, args);
    }
}
