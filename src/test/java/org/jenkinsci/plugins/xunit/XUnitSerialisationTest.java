/*
The MIT License (MIT)

Copyright (c) 2018, Falco Nikolas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package org.jenkinsci.plugins.xunit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.types.JUnitType;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.model.FreeStyleProject;
import hudson.model.Items;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

public class XUnitSerialisationTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @BeforeClass
    public static void dirtyXStreamSetup() {
        // used to simulate the AliasInitializer that is not triggered by the JenkinsRule
        Items.XSTREAM.alias("xunit", XUnitPublisher.class);
        Items.XSTREAM.alias(JUnitType.class.getSimpleName(), JUnitType.class);
    }

    @LocalData("publisher_1_103")
    @Test
    public void verify_publisher_compatible_before_1_103() throws Exception {
        FreeStyleProject project = (FreeStyleProject) r.jenkins.getItem("foo");

        assertThat(project.getPublishersList().size(), is(1));

        Publisher publisher = project.getPublishersList().get(0);
        assertThat(publisher, instanceOf(XUnitPublisher.class));

        XUnitPublisher xunitPublisher = (XUnitPublisher) publisher;
        assertThat(xunitPublisher.getTools().length, is(1));

        verifyJUnitTool(xunitPublisher.getTools()[0]);
    }

    @LocalData("builder_1_103")
    @Test
    public void verify_builder_compatible_before_1_103() throws Exception {
        FreeStyleProject project = (FreeStyleProject) r.jenkins.getItem("foo");

        assertThat(project.getBuildersList().size(), is(1));

        Builder builders = project.getBuildersList().get(0);
        assertThat(builders, instanceOf(XUnitBuilder.class));

        XUnitBuilder xunitBuilder = (XUnitBuilder) builders;
        assertThat(xunitBuilder.getTools().length, is(1));

        verifyJUnitTool(xunitBuilder.getTools()[0]);
    }

    private void verifyJUnitTool(TestType tool) {
        assertThat(tool, instanceOf(JUnitType.class));
        assertThat(tool.getPattern(), is("**/target/surefire-reports/*.xml"));
        assertThat(tool.isDeleteOutputFiles(), is(true));
        assertThat(tool.isFailIfNotNew(), is(false));
        assertThat(tool.isSkipNoTestFiles(), is(false));
        assertThat(tool.isStopProcessingIfError(), is(true));
    }

}