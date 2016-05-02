<?xml version="1.0" encoding="UTF-8"?>
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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/test-results">
        <testsuites>
            <xsl:for-each select="test-suite//results//test-case[1]">

                <xsl:for-each select="../..">
                    <xsl:variable name="firstTestName"
                                  select="results//test-case[1]//@name"/>

                    <xsl:variable name="assembly">
                        <xsl:choose>
                            <xsl:when test="substring($firstTestName, string-length($firstTestName)) = ')'">
                                <xsl:value-of
                                        select="substring-before($firstTestName, concat('.', @name))"></xsl:value-of>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat(substring-before($firstTestName, @name), @name)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <!--
                    <xsl:variable name="assembly"
                        select="concat(substring-before($firstTestName, @name), @name)" />
                    -->

                    <!--  <redirect:write file="{$outputpath}/TEST-{$assembly}.xml">-->

                    <testsuite name="{$assembly}"
                               tests="{count(*/test-case)}"
                               failures="{count(*/test-case/failure)}" errors="0"
                               skipped="{count(*/test-case[@executed='False'])}"
							   >
					   <xsl:if test="@time!=''">
							<xsl:attribute name="time">
								<xsl:value-of select="@time"/>
							</xsl:attribute>
						</xsl:if>
                        <xsl:for-each select="*/test-case">
                            <xsl:variable name="testcaseName">
                                <xsl:choose>
                                    <xsl:when test="contains(./@name, concat($assembly,'.'))">
                                        <xsl:value-of select="substring-after(./@name, concat($assembly,'.'))"/>
                                        <!-- We either instantiate a "15" -->
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./@name"/>
                                        <!-- ...or a "20" -->
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>

                            <testcase classname="{$assembly}"
                                      name="{$testcaseName}">
                                <xsl:if test="@time!=''">
                                    <xsl:attribute name="time">
                                        <xsl:value-of select="@time"/>
                                    </xsl:attribute>
                                </xsl:if>

                                <xsl:variable name="generalfailure"
                                              select="./failure"/>

                                <xsl:if test="./failure">
                                    <xsl:variable name="failstack"
                                                  select="count(./failure/stack-trace/*) + count(./failure/stack-trace/text())"/>
                                    <failure>
                                        <xsl:choose>
                                            <xsl:when test="$failstack &gt; 0 or not($generalfailure)">
                                                MESSAGE:
                                                <xsl:value-of select="./failure/message"/>
                                                +++++++++++++++++++
                                                STACK TRACE:
                                                <xsl:value-of select="./failure/stack-trace"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                MESSAGE:
                                                <xsl:value-of select="$generalfailure/message"/>
                                                +++++++++++++++++++
                                                STACK TRACE:
                                                <xsl:value-of select="$generalfailure/stack-trace"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </failure>
                                </xsl:if>
                                <xsl:if test="@executed='False'">
                                    <skipped>
                                        <xsl:attribute name="message">
                                            <xsl:value-of select="./reason/message"/>
                                        </xsl:attribute>
                                    </skipped>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                    <!--  </redirect:write>-->
                </xsl:for-each>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>