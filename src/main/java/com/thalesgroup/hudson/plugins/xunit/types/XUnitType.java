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

import com.thalesgroup.hudson.library.tusarconversion.model.InputType;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Hudson;

import java.io.Serializable;


public abstract class XUnitType implements ExtensionPoint, Describable<XUnitType>, Serializable {

    private final String pattern;

    private final Boolean faildedIfNotNew;

    private final Boolean deleteJUnitFiles;

    protected final String customXSL;

    private final InputType inputType;

    @Deprecated
    protected XUnitType(String pattern) {
        this.inputType = null;
        this.pattern = pattern;
        this.customXSL = null;
        this.faildedIfNotNew = true;
        this.deleteJUnitFiles = true;
    }

    @Deprecated
    protected XUnitType(String pattern, String customXSL) {
        this.inputType = null;
        this.pattern = pattern;
        this.customXSL = customXSL;
        this.faildedIfNotNew = true;
        this.deleteJUnitFiles = true;
    }

    @Deprecated
    protected XUnitType(String pattern, String customXSL, boolean faildedIfNotNew) {
        this.inputType = null;
        this.pattern = pattern;
        this.customXSL = customXSL;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = true;
    }

    @Deprecated
    protected XUnitType(String pattern, boolean faildedIfNotNew) {
        this.inputType = null;
        this.pattern = pattern;
        this.customXSL = null;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = true;
    }


    protected XUnitType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        this.inputType = null;
        this.pattern = pattern;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = deleteJUnitFiles;
        this.customXSL = customXSL;
    }

    @Deprecated
    protected XUnitType(String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        this.inputType = null;
        this.pattern = pattern;
        this.customXSL = null;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = deleteJUnitFiles;
    }

    protected XUnitType(InputType inputType, String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        this.inputType = inputType;
        this.pattern = pattern;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = deleteJUnitFiles;
        this.customXSL = customXSL;
    }

    protected XUnitType(InputType inputType, String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        this.inputType = inputType;
        this.pattern = pattern;
        this.customXSL = null;
        this.faildedIfNotNew = faildedIfNotNew;
        this.deleteJUnitFiles = deleteJUnitFiles;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isFaildedIfNotNew() {
        return (faildedIfNotNew == null ? true : faildedIfNotNew.booleanValue());
    }

    public boolean isDeleteJUnitFiles() {
        return (deleteJUnitFiles == null ? true : deleteJUnitFiles.booleanValue());
    }

    @SuppressWarnings("unused")
    public String getCustomXSL() {
        return customXSL;
    }

    /**
     * All registered instances.
     */
    public static ExtensionList<XUnitType> all() {
        return Hudson.getInstance().getExtensionList(XUnitType.class);
    }

    @SuppressWarnings("unchecked")
    public XUnitTypeDescriptor<?> getDescriptor() {
        return (XUnitTypeDescriptor<?>) Hudson.getInstance().getDescriptor(getClass());
    }

    /**
     * Get the associated Xsl to the type
     *
     * @return
     */
    public String getXsl() {
        if (inputType != null) {
            return inputType.getXsl();
        }
        return null;
    }

}
