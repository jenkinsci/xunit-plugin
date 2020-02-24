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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        >
    <xsl:output method="xml" indent="yes" encoding="utf-8" omit-xml-declaration="no"/>

    <xsl:param name="apostrophe">&apos;</xsl:param>
    <xsl:template name="processQuote">
        <xsl:param name="string"/>
        <xsl:if test="contains($string, $apostrophe)">
            <xsl:value-of select="substring-before($string, $apostrophe)"/><xsl:text disable-output-escaping="yes">&amp;apos;</xsl:text>
            <xsl:call-template name="processQuote">
                <xsl:with-param name="string">
                    <xsl:value-of select="substring-after($string, $apostrophe)"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="not(contains($string, $apostrophe))">
            <xsl:value-of select="$string"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="testCaseContext">
        <xsl:for-each select="child::*">
            <xsl:text> == [Context] </xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>&#13;</xsl:text>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="/TestLog">

        <xsl:element name="testsuite">
            <xsl:attribute name="tests">
                <xsl:value-of select="count(//TestCase)"/>
            </xsl:attribute>

            <xsl:attribute name="errors">
                <xsl:value-of select="count(//TestCase/FatalError)+count(//TestCase/Exception)"/>
            </xsl:attribute>

            <xsl:attribute name="failures">
                <xsl:value-of select="count(//TestCase/Error)"/>
            </xsl:attribute>

            <xsl:attribute name="name">MergedTestSuite</xsl:attribute>

            <xsl:attribute name="skipped">
                <xsl:value-of select="count(//TestCase[@skipped='yes'])"/>
            </xsl:attribute>

            <xsl:for-each select="//TestCase">
                <xsl:call-template name="testCase"/>
            </xsl:for-each>
        </xsl:element>

    </xsl:template>


    <xsl:template name="testCaseContent">
        <xsl:for-each select="child::*">
            <xsl:variable name="currElt" select="."/>
            <xsl:variable name="currEltName" select="name(.)"/>
            <xsl:choose>
                <xsl:when test="$currEltName='Error'">
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text>[Error] - </xsl:text>
                    <!--<xsl:call-template name="processQuote">-->
                    <!--<xsl:with-param name="string">-->
                    <!--<xsl:value-of select="$currElt/text()"/>-->
                    <!--</xsl:with-param>-->
                    <!--</xsl:call-template>-->
                    <xsl:value-of select="$currElt"/>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:for-each select="child::Context">
                        <xsl:call-template name="testCaseContext"/>
                    </xsl:for-each>
                </xsl:when>

                <xsl:when test="$currEltName='FatalError'">
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text>[Exception] - </xsl:text>
                    <xsl:call-template name="processQuote">
                        <xsl:with-param name="string">
                            <xsl:value-of select="$currElt/text()"/>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text> == [Line] -</xsl:text><xsl:value-of select="($currElt)/@line"/>
                    <xsl:text>&#13;</xsl:text>
                    <xsl:for-each select="child::Context">
                        <xsl:call-template name="testCaseContext"/>
                    </xsl:for-each>
                </xsl:when>

                <xsl:when test="$currEltName='Exception'">
                    <xsl:text>&#13;</xsl:text>
                    <xsl:text>[Exception] - </xsl:text>
                    <xsl:call-template name="processQuote">
                        <xsl:with-param name="string">
                            <xsl:value-of select="$currElt/text()"/>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:choose>
                        <xsl:when test="($currElt)/LastCheckpoint">
                            <xsl:value-of select="($currElt)/LastCheckpoint/text()"/>
                            <xsl:text>&#13;</xsl:text>
                            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/LastCheckpoint/@file"/>
                            <xsl:text>&#13;</xsl:text>
                            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/LastCheckpoint/@line"/>
                            <xsl:text>&#13;</xsl:text>
                        </xsl:when>

                        <xsl:otherwise>
                            <xsl:text>&#13;</xsl:text>
                            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                            <xsl:text>&#13;</xsl:text>
                            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                            <xsl:text>&#13;</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:for-each select="child::Context">
                        <xsl:call-template name="testCaseContext"/>
                    </xsl:for-each>
                </xsl:when>

                <xsl:when test="$currEltName='Info'"></xsl:when>

            </xsl:choose>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="testCase">

        <xsl:variable name="curElt" select="."/>
        <xsl:variable name="suiteName">
            <xsl:for-each select="($curElt/ancestor::TestSuite)">
                <xsl:variable name="nameTrimed" select="replace(./@name,' ','.')"/>
                <xsl:value-of select="$nameTrimed"/><xsl:text>.</xsl:text>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="packageName" select="($suiteName)"/>

        <xsl:element name="testcase">
            <xsl:variable name="elt" select="(child::*[position()=1])"/>
            <xsl:variable name="time" select="TestingTime"/>

            <xsl:attribute name="classname">
                <xsl:variable name="packageStr" select="concat($packageName, substring-before(($elt)/@file, '.'))"/>
                <xsl:choose>
                    <xsl:when test="ends-with($packageStr, '.')">
                        <xsl:variable name="packageStr2"
                                      select="substring($packageStr, 1, string-length($packageStr)-1)"/>
                        <xsl:value-of select="replace($packageStr2,' ','.')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="packageStr2" select="($packageStr)"/>
                        <xsl:value-of select="replace($packageStr2,' ','.')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>

            <!-- When there is only Exception with LastCheckpoint, override classname attribute -->

            <xsl:if
                    test="((count(child::Exception))=1) and ((count(child::Info)+ count(child::Warning) + count(child::Message))=0)">
                <xsl:variable name="fileName" select="substring-before((./Exception/LastCheckpoint)/@file, '.')"/>
                <xsl:attribute name="classname">
                    <!-- <xsl:value-of select="concat($packageName, $fileName)"/> -->
                    <xsl:variable name="packageStr" select="concat($packageName, $fileName)"/>
                    <xsl:choose>
                        <xsl:when test="ends-with($packageStr, '.')">
                            <xsl:variable name="packageStr2"
                                          select="substring($packageStr, 1, string-length($packageStr)-1)"/>
                            <xsl:value-of select="replace($packageStr2,' ','.')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="packageStr2" select="($packageStr)"/>
                            <xsl:value-of select="replace($packageStr2,' ','.')"/>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:attribute>
            </xsl:if>


            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>


            <xsl:attribute name="time">
                <xsl:choose>
                    <xsl:when test="TestingTime"><xsl:value-of select="$time div 1000000"/></xsl:when>
                    <xsl:otherwise>0.000</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>

            <xsl:variable name="nbErrors" select="count(Error)"/>
            <xsl:variable name="nbFatalErrors" select="count(FatalError)+count(Exception)"/>

            <xsl:choose>
                <xsl:when test="$nbFatalErrors&gt;0">
                    <xsl:element name="error">
                        <xsl:call-template name="testCaseContent"/>
                    </xsl:element>
                </xsl:when>

                <xsl:when test="$nbErrors&gt;0">
                    <xsl:element name="failure">
                        <xsl:call-template name="testCaseContent"/>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>

            <xsl:if test="@skipped">
                <skipped />
            </xsl:if>

            <xsl:if test="(count(child::Info)+ count(child::Warning) + count(child::Message))>0">
                <xsl:element name="system-out">
                    <xsl:for-each select="child::Info">
                        <xsl:variable name="currElt" select="."/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Info] - </xsl:text>
                        <xsl:call-template name="processQuote">
                            <xsl:with-param name="string">
                                <xsl:value-of select="$currElt/text()"/>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                        <xsl:text>&#13;</xsl:text>
                    </xsl:for-each>

                    <xsl:for-each select="child::Warning">
                        <xsl:variable name="currElt" select="."/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Warning] - </xsl:text>

                        <xsl:call-template name="processQuote">
                            <xsl:with-param name="string">
                                <xsl:value-of select="$currElt/text()"/>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                        <xsl:text>&#13;</xsl:text>
                    </xsl:for-each>

                    <xsl:for-each select="child::Message">
                        <xsl:variable name="currElt" select="."/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Message] - </xsl:text>
                        <xsl:call-template name="processQuote">
                            <xsl:with-param name="string">
                                <xsl:value-of select="$currElt/text()"/>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                        <xsl:text>&#13;</xsl:text>
                    </xsl:for-each>

                    <xsl:for-each select="child::*/Context">
                        <xsl:call-template name="testCaseContext"/>
                    </xsl:for-each>

                </xsl:element>
            </xsl:if>


            <xsl:if test="count(child::Exception)>0">
                <xsl:element name="system-err">
                    <xsl:for-each select="child::Exception">
                        <xsl:variable name="currElt" select="."/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Exception] - </xsl:text>
                        <xsl:call-template name="processQuote">
                            <xsl:with-param name="string">
                                <xsl:value-of select="$currElt/text()"/>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:choose>
                            <xsl:when test="($currElt)/LastCheckpoint">
                                <xsl:value-of select="($currElt)/LastCheckpoint/text()"/>
                                <xsl:text>&#13;</xsl:text>
                                <xsl:text> == [File] - </xsl:text><xsl:value-of
                                    select="($currElt)/LastCheckpoint/@file"/>
                                <xsl:text>&#13;</xsl:text>
                                <xsl:text> == [Line] - </xsl:text><xsl:value-of
                                    select="($currElt)/LastCheckpoint/@line"/>
                                <xsl:text>&#13;</xsl:text>
                            </xsl:when>

                            <xsl:otherwise>
                                <xsl:text>&#13;</xsl:text>
                                <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
                                <xsl:text>&#13;</xsl:text>
                                <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
                                <xsl:text>&#13;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>

                    </xsl:for-each>

                    <xsl:for-each select="child::*/Context">
                        <xsl:call-template name="testCaseContext"/>
                    </xsl:for-each>

                </xsl:element>
            </xsl:if>


        </xsl:element>

    </xsl:template>

    <xsl:template match="text()|@*"/>
</xsl:stylesheet>
