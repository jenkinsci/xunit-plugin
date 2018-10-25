package org.jenkinsci.plugins.xunit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.TransformerException;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.AUnitJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.CustomType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
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

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup() throws Exception {
        workspace = new FilePath(fileRule.newFolder());
    }

    @Test
    public void user_XSL_override_embedded() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());

        TestType tool = new AUnitJunitHudsonTestType("**/TEST-*.xml", false, false, true, true);

        FilePath userContent = new FilePath(jenkinsRule.jenkins.getRootDir()).child("userContent").child("xunit").child("AUnit").child("3.x");
        userContent.mkdirs();
        FilePath customXSL = userContent.child("customXSL.xsl");
        FileUtils.write(new File(customXSL.getRemote()), "test");

        XUnitProcessor processor = new XUnitProcessor(new TestType[] { tool }, null, 300, new ExtraConfiguration(3000));
        Field field = ReflectionUtils.findField(XUnitProcessor.class, "logger");
        field.setAccessible(true);
        ReflectionUtils.setField(field, processor, mock(XUnitLog.class));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tool, build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void custom_tools_as_URL() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());

        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test");

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.toURI().toURL().toExternalForm()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void custom_tools_resolve_against_master() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());
        
        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test");
        
        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getAbsolutePath()) };
        
        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);
        
        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Ignore
    @Test
    public void custom_tools_resolve_against_workspace() throws Throwable {
        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test");

        VirtualChannel channel = mock(VirtualChannel.class);
        when(channel.call(any(DelegatingCallable.class))).thenReturn(false, true);
        workspace = new FilePath(channel, workspace.getName());
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getName()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void slave_exception_unwraps_to_TransformerException_if_any_is_found_in_chain() {
        XUnitProcessor xup = new XUnitProcessor(new TestType[0], new XUnitThreshold[0], 0, new ExtraConfiguration(0));
        TransformerException transformerException = new TransformerException("test");
        IOException exception = new IOException(new Exception(transformerException));
        Throwable unwrappedException = xup.unwrapSlaveException(exception);
        Assert.assertEquals(transformerException, unwrappedException);
    }

    @Test
    public void slave_exception_unwraps_to_self_if_no_TransformerException_is_found_in_chain() {
        XUnitProcessor xup = new XUnitProcessor(new TestType[0], new XUnitThreshold[0], 0, new ExtraConfiguration(0));
        IOException exception = new IOException(new Exception(new Exception()));
        Throwable unwrappedException = xup.unwrapSlaveException(exception);
        Assert.assertEquals(exception, unwrappedException);
    }

    @Test
    public void slave_exception_with_no_cause_unwraps_to_self() {
        XUnitProcessor xup = new XUnitProcessor(new TestType[0], new XUnitThreshold[0], 0, new ExtraConfiguration(0));
        IOException exception = new IOException();
        Throwable unwrappedException = xup.unwrapSlaveException(exception);
        Assert.assertEquals(exception, unwrappedException);
    }
}