/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit;

import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkinsci.lib.dryrun.DryRun;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;

import java.io.IOException;
import java.io.Serializable;

/**
 * Class that converting custom reports to Junit reports and records them
 *
 * @author Gregory Boissinot
 */
@SuppressWarnings({"unchecked", "unused"})
public class XUnitPublisher extends Recorder implements DryRun, Serializable {

    private TestType[] types;

    private XUnitThreshold[] thresholds;

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return false;
    }

    @SuppressWarnings("unused")
    public static final class XUnitDescriptorPublisher extends BuildStepDescriptor<Publisher> {

        public XUnitDescriptorPublisher() {
            super(XUnitPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public boolean isApplicable(Class type) {
            return false;
        }
    }


    private Object readResolve() {
        return new org.jenkinsci.plugins.xunit.XUnitPublisher(types, thresholds, 1);
    }
}




