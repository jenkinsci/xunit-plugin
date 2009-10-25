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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thalesgroup.hudson.plugins.xunit.model.TypeConfig;
import com.thalesgroup.hudson.plugins.xunit.types.TypeDescriptor;

public class XUnitConfig implements Serializable {


    private static final long serialVersionUID = 1L;

    @SuppressWarnings("deprecation")
    public static final Map<String, TypeDescriptor> TOOLS = new HashMap<String, TypeDescriptor>();

    @SuppressWarnings("deprecation")
    public static void addDescriptor(TypeDescriptor t) {
        TOOLS.put(t.getName(), t);
    }

    @SuppressWarnings("deprecation")
    private List<TypeConfig> testTools = new ArrayList<TypeConfig>();

    @SuppressWarnings("deprecation")
    private List<TypeConfig> customTools = new ArrayList<TypeConfig>();


    public XUnitConfig() {
    }

    @SuppressWarnings("deprecation")
    public List<TypeConfig> getTestTools() {
        return testTools;
    }

    @SuppressWarnings("deprecation")
    public List<TypeConfig> getCustomTools() {
        return customTools;
    }
}
