/**
 *
 */
package org.waarp.openr66.protocol.junit;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.openr66.context.task.exception.OpenR66RunnerErrorException;
import org.waarp.openr66.protocol.test.TestTasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author frederic
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllTasksTest extends TestAbstract {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setUpBeforeClassMinimal("Linux/config/config-serverInitB.xml");
        setUpDbBeforeClass();
    }

    @Test
    public void test2_Tasks() throws IOException, OpenR66RunnerErrorException,
                                     CommandAbstractException {
        System.err.println("Start Tasks");
        File totest = new File("/tmp/R66/in/testTask.txt");
        FileWriter fileWriter = new FileWriter(totest);
        fileWriter.write("Test content");
        fileWriter.flush();
        fileWriter.close();
        TestTasks.main(new String[] {
                new File(dir, "config-serverA-minimal.xml").getAbsolutePath(),
                "/tmp/R66/in",
                "/tmp/R66/out", totest.getName()
        });
        System.err.println("End Tasks");
    }

}
