/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
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

import java.util.Iterator;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;

import hudson.DescriptorExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
public class AliasInitializer {

    @Initializer(before = InitMilestone.JOB_LOADED)
    public static void init(Jenkins jenkins) {
        Items.XSTREAM.alias("xunit", XUnitPublisher.class);
        DescriptorExtensionList<TestType, TestTypeDescriptor<TestType>> extensionList = jenkins.getDescriptorList(TestType.class);
        for (Iterator<TestTypeDescriptor<TestType>> it = extensionList.iterator(); it.hasNext(); ) {
            Class<? extends TestType> classType = it.next().clazz;
            String className = getClassName(classType);
            Items.XSTREAM.alias(className, classType);
        }
        Items.XSTREAM.processAnnotations(XUnitPublisher.class);
    }

    private static String getClassName(Class<? extends TestType> classType) {
        String name = classType.getName();
        String packageSep = ".";
        if (name.contains(packageSep)) {
            return name.substring(name.lastIndexOf(packageSep) + 1);
        }
        return name;
    }
}
