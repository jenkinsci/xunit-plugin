<?xml version="1.0" encoding="UTF-8"?>
<!--
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
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <testsuite>
            <xsl:attribute name="errors">
                <xsl:value-of select="unittest-results/@failedtests"/>
            </xsl:attribute>

            <xsl:attribute name="failures">
                <xsl:value-of select="unittest-results/@failures"/>
            </xsl:attribute>

            <xsl:attribute name="tests">
                <xsl:value-of select="unittest-results/@tests"/>
            </xsl:attribute>

            <xsl:attribute name="name">unittest</xsl:attribute>

            <xsl:apply-templates/>
        </testsuite>
    </xsl:template>

    <xsl:template match="/unittest-results/test">
        <testcase>


            <xsl:attribute name="classname">
                <xsl:value-of select="@suite"/>
            </xsl:attribute>

            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>

            <xsl:attribute name="time">0</xsl:attribute>

            <xsl:copy-of select="child::*"/>

        </testcase>
    </xsl:template>

</xsl:stylesheet>