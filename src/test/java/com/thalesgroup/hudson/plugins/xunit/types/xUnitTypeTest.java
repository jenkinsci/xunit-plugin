/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
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

import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.hudson.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.anyString;


public class xUnitTypeTest {

    private <X extends XUnitType, H extends TestType> void processTestNewReturnObjectAllCases(Class<X> classXUnitType, Class<H> classNewHudsonType) throws Exception {
        processTestNewReturnObject(classXUnitType, classNewHudsonType, anyString(), true, true);
        processTestNewReturnObject(classXUnitType, classNewHudsonType, anyString(), true, false);
        processTestNewReturnObject(classXUnitType, classNewHudsonType, anyString(), false, true);
        processTestNewReturnObject(classXUnitType, classNewHudsonType, anyString(), false, false);
    }


    @SuppressWarnings("unchecked")
    private <X extends XUnitType, H extends TestType> void processTestNewReturnObject(Class<X> classXUnitType, Class<H> classNewHudsonType, String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Constructor c = classXUnitType.getConstructor(String.class, boolean.class, boolean.class);
        X xUnitType = (X) c.newInstance(pattern, faildedIfNotNew, deleteJUnitFiles);

        //Test new descriptor
        Assert.assertNull(xUnitType.getDescriptor());

        //Test new Object type
        Method readResolveMethod = classXUnitType.getMethod("readResolve");
        Object object = readResolveMethod.invoke(xUnitType);
        Assert.assertTrue(object.getClass() == classNewHudsonType);

        H hudsonTestType = (H) object;
        Assert.assertNotNull(hudsonTestType.getDescriptor());

        Assert.assertEquals(xUnitType.getPattern(), hudsonTestType.getPattern());
        Assert.assertEquals(xUnitType.isDeleteJUnitFiles(), hudsonTestType.isDeleteOutputFiles());
        Assert.assertEquals(xUnitType.isFaildedIfNotNew(), hudsonTestType.isFaildedIfNotNew());

        InputMetric inputMetric = hudsonTestType.getInputMetric();
        Assert.assertNotNull(inputMetric);
    }

    @Test
    public void testMSTestType() throws Exception {
        processTestNewReturnObjectAllCases(MSTestType.class, MSTestHudsonTestType.class);
    }

    @Test
    public void testBoostTestType() throws Exception {
        processTestNewReturnObjectAllCases(BoostTestType.class, BoostTestHudsonTestType.class);
    }

    @Test
    public void testFPCUnitType() throws Exception {
        processTestNewReturnObjectAllCases(FPCUnitType.class, FPCUnitHudsonTestType.class);
    }

    @Test
    public void testNUnitType() throws Exception {
        processTestNewReturnObjectAllCases(NUnitType.class, NUnitHudsonTestType.class);
    }

    @Test
    public void testPHPUnitType() throws Exception {
        processTestNewReturnObjectAllCases(PHPUnitType.class, PHPUnitHudsonTestType.class);
    }

    @Test
    public void testUnitTestType() throws Exception {
        processTestNewReturnObjectAllCases(UnitTestType.class, UnitTestHudsonTestType.class);
    }

}
