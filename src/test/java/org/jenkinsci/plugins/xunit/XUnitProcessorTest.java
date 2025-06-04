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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.VirtualChannel;
import hudson.util.ReflectionUtils;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.NoTestFoundException;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.service.XUnitTransformerCallable;
import org.jenkinsci.plugins.xunit.types.AUnitJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.CppUnitJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.CustomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@WithJenkins
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XUnitProcessorTest {

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

    @TempDir
    private File fileRule;

    private JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) throws Exception {
        jenkinsRule = rule;
        workspace = new FilePath(newFolder(fileRule, "junit"));
        when(listener.getLogger()).thenReturn(System.out);
        when(build.getEnvironment(listener)).thenReturn(new EnvVars());
    }

    @Test
    void user_XSL_override_embedded() throws Exception {
        TestType tool = new AUnitJunitHudsonTestType("**/TEST-*.xml", false, false, true, true);

        FilePath userContent = new FilePath(jenkinsRule.jenkins.getRootDir()).child("userContent")
                .child("xunit").child("AUnit").child("3.x");
        userContent.mkdirs();
        FilePath customXSL = userContent.child("customXSL.xsl");
        FileUtils.write(new File(customXSL.getRemote()), "test", StandardCharsets.UTF_8);

        XUnitProcessor processor = new XUnitProcessor(new TestType[]{tool}, null, 300,
                new ExtraConfiguration(3000, true, 10, true, true, null));
        Field field = ReflectionUtils.findField(XUnitProcessor.class, "logger");
        field.setAccessible(true);
        ReflectionUtils.setField(field, processor, mock(XUnitLog.class));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tool, build, workspace, listener);

        assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    void custom_tools_as_URL() throws Exception {
        final File customXSL = newFile(fileRule, "customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        final TestType[] tools = new TestType[]{
                new MyCustomType(customXSL.toURI().toURL().toExternalForm())};

        XUnitProcessor processor = buildProcessor(tools);
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    void custom_tools_resolve_against_master() throws Exception {
        final File customXSL = newFile(fileRule, "customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        final TestType[] tools = new TestType[]{new MyCustomType(customXSL.getAbsolutePath())};

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300,
                new ExtraConfiguration(3000, true, 10, true, true, null));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        assertEquals("test", toolInfo.getXSLFile());
    }

    @Disabled
    @Test
    void custom_tools_resolve_against_workspace() throws Throwable {
        final File customXSL = newFile(fileRule, "customXSL.xsl");
        FileUtils.write(customXSL, "test", StandardCharsets.UTF_8);

        VirtualChannel channel = mock(VirtualChannel.class);
        when(channel.call(any(DelegatingCallable.class))).thenReturn(false, true);
        workspace = new FilePath(channel, workspace.getName());

        final TestType[] tools = new TestType[]{new MyCustomType(customXSL.getName())};

        XUnitProcessor processor = new XUnitProcessor(tools, null, 300,
                new ExtraConfiguration(3000, true, 10, true, true, null));
        XUnitToolInfo toolInfo = processor.buildXUnitToolInfo(tools[0], build, workspace, listener);

        assertEquals("test", toolInfo.getXSLFile());
    }

    @Test
    void slave_exception_unwraps_to_TransformerException_if_any_is_found_in_chain() throws Exception {
        NoTestFoundException noTestFoundException = new NoTestFoundException("test");

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        IOException callableException = new IOException(new Exception(noTestFoundException));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(
                callableException);

        NoTestFoundException exception = assertThrows(NoTestFoundException.class, () -> processor.process(build, workspace, listener, null, Collections.emptyList(), null));
        assertSame(exception, noTestFoundException);
    }

    @Test
    void slave_exception_unwraps_to_self_if_no_TransformerException_is_found_in_chain() throws Exception {
        IOException callableException = new IOException(new Exception(new Exception()));

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(
                callableException);

        IOException exception = assertThrows(IOException.class, () -> processor.process(build, workspace, listener, null, Collections.emptyList(), null));
        assertSame(exception, callableException);
    }

    @Test
    void slave_exception_with_no_cause_unwraps_to_self() throws Exception {
        IOException callableException = new IOException();

        XUnitProcessor processor = spy(buildProcessor());

        XUnitTransformerCallable callable = mock(XUnitTransformerCallable.class);
        doReturn(callable).when(processor).newXUnitTransformer(any(XUnitToolInfo.class));
        when(callable.invoke(any(File.class), any(VirtualChannel.class))).thenThrow(
                callableException);

        IOException exception = assertThrows(IOException.class, () -> processor.process(build, workspace, listener, null, Collections.emptyList(), null));
        assertSame(exception, callableException);
    }

    private XUnitProcessor buildProcessor(TestType[] tools) {
        return new XUnitProcessor(tools, null, 0,
                new ExtraConfiguration(3000, true, 10, true, true, null));
    }

    private XUnitProcessor buildProcessor() {
        return buildProcessor(new TestType[]{
                new CppUnitJunitHudsonTestType("**/TEST-*.xml", false, false, true, true)});
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }

    private static File newFile(File parent, String child) throws IOException {
        File result = new File(parent, child);
        if (!result.createNewFile()) {
            throw new IOException("Couldn't create file " + result);
        }
        return result;
    }

}
