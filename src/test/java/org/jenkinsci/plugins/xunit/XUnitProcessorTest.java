package org.jenkinsci.plugins.xunit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.types.CustomType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.VirtualChannel;
import hudson.util.ReflectionUtils;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class XUnitProcessorTest {

    @SuppressWarnings("serial")
    private static class MyCustomType extends CustomType {
        public MyCustomType(String xslPath) {
            super("**/TEST-*.xml", xslPath, false, false, true, true);
        }

        @Override
        public TestTypeDescriptor<? extends TestType> getDescriptor() {
            return new CustomInputMetricDescriptor();
        }
    }

    @Mock
    private TaskListener listener;
    @Mock
    private Run<?, ?> build;
    private FilePath workspace;
    private Class<?>[] paramTypes = new Class[] { TestType.class, Run.class, FilePath.class, TaskListener.class };

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        workspace = new FilePath(fileRule.newFolder());
    }

    @Test
    public void custom_tools_read_from_master() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());

        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test");

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getAbsolutePath()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000));

        Method buildXUnitToolInfo = ReflectionUtils.findMethod(XUnitProcessor.class, "buildXUnitToolInfo", paramTypes);
        buildXUnitToolInfo.setAccessible(true);
        Object[] args = new Object[] { tools[0], build, workspace, listener };
        XUnitToolInfo toolInfo = (XUnitToolInfo) ReflectionUtils.invokeMethod(buildXUnitToolInfo, processor, args);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Ignore
    @Test
    public void read_custom_xsl_from_workspace() throws Throwable {
        // TODO use powermockito to mock FilePath!
        VirtualChannel channel = mock(VirtualChannel.class);
        when(channel.call(any(DelegatingCallable.class))).thenReturn(true);
        workspace = new FilePath(channel, workspace.getName());
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());

        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test");

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getName()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000));

        Method buildXUnitToolInfo = ReflectionUtils.findMethod(XUnitProcessor.class, "buildXUnitToolInfo", paramTypes);
        buildXUnitToolInfo.setAccessible(true);
        Object[] args = new Object[] { tools[0], build, workspace, listener };
        XUnitToolInfo toolInfo = (XUnitToolInfo) ReflectionUtils.invokeMethod(buildXUnitToolInfo, processor, args);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

}