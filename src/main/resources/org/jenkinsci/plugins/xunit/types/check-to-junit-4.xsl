<!--
The MIT License (MIT)

Copyright (c) 2014, Gregory Boissinot

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
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ck="http://check.sourceforge.net/ns"
                exclude-result-prefixes="ck">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <testsuites>
            <xsl:apply-templates/>
        </testsuites>
    </xsl:template>

    <xsl:template match="//ck:suite">
        <!-- we should theoretically have a package attribute, but there's
             nothing sensible to set it to (filename?) and Jenkins handles
             that being missing anyway
        -->
        <xsl:variable name="count">
            <xsl:number/>
        </xsl:variable>
        <xsl:variable name="checkCount"
                      select="count(ck:test)"/>
        <xsl:variable name="checkCountFailure"
                      select="count(ck:test[@result='failure'])"/>
        <xsl:variable name="checkCountError"
                      select="count(ck:test[@result='error'])"/>
        <xsl:variable name="suitename"
                      select="ck:title"/>
        <testsuite>
            <xsl:attribute name="errors">0</xsl:attribute>
            <xsl:attribute name="tests">
                <xsl:value-of select="$checkCount"/>
            </xsl:attribute>
            <xsl:attribute name="failures">
                <xsl:value-of select="$checkCountFailure"/>
            </xsl:attribute>
            <xsl:attribute name="errors">
                <xsl:value-of select="$checkCountError"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="$suitename"/>
            </xsl:attribute>
            <xsl:attribute name="id">
                <xsl:value-of select="$count"/>
            </xsl:attribute>
            <xsl:apply-templates>
                <xsl:with-param name="suitename" select="$suitename"/>
            </xsl:apply-templates>
        </testsuite>
    </xsl:template>

    <xsl:template match="ck:test">
        <xsl:param name="suitename"/>
        <testcase>
            <xsl:attribute name="name">
                <xsl:value-of select="./ck:id"/>
            </xsl:attribute>
            <xsl:attribute name="classname">
                <xsl:value-of select="$suitename"/>
            </xsl:attribute>
            <xsl:attribute name="time">0</xsl:attribute>
            <xsl:if test="@result = 'error'">
                <error type="error">
                    <xsl:attribute name="message">
                        <xsl:value-of select="./ck:message"/>
                    </xsl:attribute>
                </error>
            </xsl:if>
            <xsl:if test="@result = 'failure'">
                <failure type="failure">
                    <xsl:attribute name="message">
                        <xsl:value-of select="./ck:message"/>
                    </xsl:attribute>
                </failure>
            </xsl:if>
        </testcase>
    </xsl:template>

    <!-- this swallows all unmatched text -->
    <xsl:template match="text()|@*"/>
</xsl:stylesheet>