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

package com.thalesgroup.hudson.plugins.xunit.types;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import java.io.Serializable;


public abstract class XUnitType implements ExtensionPoint, Describable<XUnitType>, Serializable {

    private final String pattern;

    private final Boolean faildedIfNotNew;

    protected final String customXSL;


    @Deprecated
    protected XUnitType(String pattern) {
        this.pattern = pattern;
        customXSL = null;
        faildedIfNotNew=true;
    }

    @Deprecated
    protected XUnitType(String pattern, String customXSL) {
        this.pattern = pattern;
        this.customXSL = customXSL;
        this.faildedIfNotNew=true;
    }

    protected XUnitType(String pattern, String customXSL, boolean faildedIfNotNew) {
        this.pattern = pattern;
        this.customXSL = customXSL;
        this.faildedIfNotNew = faildedIfNotNew;
    }

    protected XUnitType(String pattern, boolean faildedIfNotNew) {
        this.pattern = pattern;
        customXSL = null;
        this.faildedIfNotNew = faildedIfNotNew;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isFaildedIfNotNew() {
        return faildedIfNotNew;
    }

    @SuppressWarnings("unused")
    public String getCustomXSL() {
        return customXSL;
    }

    /**
     * All regsitered instances.
     */
    public static ExtensionList<XUnitType> all() {
        return Hudson.getInstance().getExtensionList(XUnitType.class);
    }

    @SuppressWarnings("unchecked")
    public Descriptor<XUnitType> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }


    /**
     * Gets the associated Xsl to the type
     *
     * @return
     */
    public abstract String getXsl();

}
