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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" cdata-section-elements="system-out"/>
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="//testsuites">
        <testsuites>
            <xsl:apply-templates/>
        </testsuites>
    </xsl:template>
    <xsl:template match="//testsuite">
        <testsuite>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="tests">
                <xsl:value-of select="@tests"/>
            </xsl:attribute>
            <xsl:apply-templates select="testcase"/>
        </testsuite>
    </xsl:template>
    <xsl:template match="//testcase">
        <testcase>
            <xsl:choose>
                <xsl:when test="@value_param">
                    <xsl:attribute name="name">
                        <xsl:value-of select="@name"/> (<xsl:value-of select="@value_param"/>)
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="name">
                        <xsl:value-of select="@name"/>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="time">
                <xsl:value-of select="@time"/>
            </xsl:attribute>
            <xsl:attribute name="classname">
                <xsl:value-of select="@classname"/>
            </xsl:attribute>
            <xsl:if test="@status = 'notrun'">
                <skipped/>
            </xsl:if>
            <xsl:if test="failure">
                <failure>
                    <xsl:for-each select="failure">
                        <xsl:if test="not(position()=1)">
                            <xsl:text>&#xa;&#xa;</xsl:text>
                        </xsl:if>
                        <xsl:value-of select="@message"/>
                    </xsl:for-each>
                </failure>
                <system-out>
                    <xsl:for-each select="failure">
                        <xsl:if test="not(position()=1)">
                            <xsl:text>&#xa;&#xa;</xsl:text>
                        </xsl:if>
                        <xsl:value-of select="."/>
                    </xsl:for-each>
                </system-out>
            </xsl:if>
			<xsl:if test="error">
                <failure>
                    <xsl:for-each select="error">
                        <xsl:if test="not(position()=1)">
                            <xsl:text>&#xa;&#xa;</xsl:text>
                        </xsl:if>
                        <xsl:value-of select="@message"/>
                    </xsl:for-each>
                </failure>
                <system-out>
                    <xsl:for-each select="error">
                        <xsl:if test="not(position()=1)">
                            <xsl:text>&#xa;&#xa;</xsl:text>
                        </xsl:if>
                        <xsl:value-of select="."/>
                    </xsl:for-each>
                </system-out>
            </xsl:if>
        </testcase>
    </xsl:template>
    <!-- this swallows all unmatched text -->
    <!-- <xsl:template match="text()|@*"/>-->
</xsl:stylesheet>

