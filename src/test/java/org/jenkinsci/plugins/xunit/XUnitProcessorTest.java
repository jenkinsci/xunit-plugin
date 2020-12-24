/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Nikolas Falco, Arnaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.xunit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.NoTestFoundException;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.service.XUnitTransformerCallable;
import org.jenkinsci.plugins.xunit.types.AUnitJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.CppUnitJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.CustomType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import hudson.tasks.junit.TestDataPublisher;
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

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup() throws Exception {
        workspace = new FilePath(fileRule.newFolder());
        when(listener.getLogger()).thenReturn(System.out);
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());
    }

    @Test
    public void user_XSL_override_embedded() throws Exception {
        TestType tool = new AUnitJunitHudsonTestType("**/TEST-*.xml", false, false, true, true);

        FilePath userContent = new FilePath(jenkinsRule.jenkins.getRootDir()).child("userContent").child("xunit").child("AUnit").child("3.x");
        userContent.mkdirs();
        FilePath customXSL = userContent.child("customXSL.xsl");
        FileUtils.write(new File(customXSL.getRemote()), "test", StandardCharsets.UTF_8);

        XUnitProcessor processor = new XUnitProcessor(new TestType[] { tool }, null, 300, new ExtraConfiguration(3000, true, 10, true));
        Field field = ReflectionUtils.findField(XUnitProcessor.class, "logger");
        field.setAccessible(true);
        ReflectionUtils.setField(field, processor, mock(XUnitLog.class));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tool, build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void custom_tools_as_URL() throws Exception {
        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.toURI().toURL().toExternalForm()) };

        XUnitProcessor processor = buildProcessor(tools);
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void custom_tools_resolve_against_master() throws Exception {
        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getAbsolutePath()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000, true, 10, true));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Ignore
    @Test
    public void custom_tools_resolve_against_workspace() throws Throwable {
        final File customXSL = fileRule.newFile("customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        VirtualChannel channel = mock(VirtualChannel.class);
        when(channel.call(any(DelegatingCallable.class))).thenReturn(false, true);
        workspace = new FilePath(channel, workspace.getName());

        final TestType[] tools = new TestType[] { new MyCustomType(customXSL.getName()) };

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300, new ExtraConfiguration(3000, true, 10, true));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        Assert.assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    public void slave_exception_unwraps_to_TransformerException_if_any_is_found_in_chain() throws Exception {
        NoTestFoundException noTestFoundException = new NoTestFoundException("test");
        exceptionRule.expect(CoreMatchers.sameInstance(noTestFoundException));

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        IOException callableException = new IOException(new Exception(noTestFoundException));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(callableException);

        processor.process(build, workspace, listener, null, Collections.<TestDataPublisher>emptyList(), null);
    }

    @Test
    public void slave_exception_unwraps_to_self_if_no_TransformerException_is_found_in_chain() throws Exception {
        IOException callableException = new IOException(new Exception(new Exception()));
        exceptionRule.expect(CoreMatchers.sameInstance(callableException));

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(callableException);

        processor.process(build, workspace, listener, null, Collections.<TestDataPublisher>emptyList(), null);
    }

    @Test
    public void slave_exception_with_no_cause_unwraps_to_self() throws Exception {
        IOException callableException = new IOException();
        exceptionRule.expect(CoreMatchers.sameInstance(callableException));

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(callableException);

        processor.process(build, workspace, listener, null, Collections.<TestDataPublisher>emptyList(), null);
    }

    private XUnitProcessor buildProcessor(TestType[] tools) {
        return new XUnitProcessor(tools, null, 0, new ExtraConfiguration(3000, true, 10, true));
    }

    private XUnitProcessor buildProcessor() {
        return buildProcessor(new TestType[] { new CppUnitJunitHudsonTestType("**/TEST-*.xml", false, false, true, true) });
    }

}
