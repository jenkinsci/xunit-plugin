<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2018, R. Tyler Croy, André Klitzing, Falco Nikolas

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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xunit="http://www.xunit.org">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" cdata-section-elements="system-out system-err failure" />
    <xsl:decimal-format decimal-separator="." grouping-separator="," />

    <xsl:function name="xunit:junit-time" as="xs:string">
        <xsl:param name="value" as="xs:anyAtomicType?" />

        <xsl:variable name="time" as="xs:double">
            <xsl:choose>
                <xsl:when test="$value instance of xs:double">
                    <xsl:value-of select="$value" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="translate(string(xunit:if-empty($value, 0)), ',', '.')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="format-number($time, '0.000')" />
    </xsl:function>

    <xsl:function name="xunit:if-empty" as="xs:string">
        <xsl:param name="value" as="xs:anyAtomicType?" />
        <xsl:param name="default" as="xs:anyAtomicType" />
        <xsl:value-of select="if (string($value) != '') then string($value) else $default" />
    </xsl:function>

    <xsl:function name="xunit:is-empty" as="xs:boolean">
        <xsl:param name="value" as="xs:string?" />
        <xsl:value-of select="string($value) != ''" />
    </xsl:function>

    <xsl:function name="xunit:index-of-string" as="xs:integer*">
        <xsl:param name="arg" as="xs:string?" />
        <xsl:param name="substring" as="xs:string" />

        <xsl:sequence
            select="
      if (contains($arg, $substring))
      then (string-length(substring-before($arg, $substring))+1,
            for $other in
               xunit:index-of-string(substring-after($arg, $substring), $substring)
            return
              $other +
              string-length(substring-before($arg, $substring)) +
              string-length($substring))
      else ()" />
    </xsl:function>

    <xsl:function name="xunit:last-index-of" as="xs:integer?">
        <xsl:param name="arg" as="xs:string?" />
        <xsl:param name="substring" as="xs:string" />

        <xsl:sequence select="xunit:index-of-string($arg, $substring)[last()]" />
    </xsl:function>

    <xsl:template name="removedelimiter">
        <xsl:param name="string" />
        <xsl:param name="token" />

        <xsl:choose>
            <xsl:when test="starts-with($string, $token)">
                <xsl:choose>
                    <xsl:when test="ends-with($string, $token)">
                        <xsl:value-of select="substring($string, 2, string-length($string) - 2)" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring($string, 2)" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="ends-with($string, $token)">
                <xsl:value-of select="substring($string, 1, string-length($string) - 2)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/">
        <testsuites>
            <xsl:for-each select="gtester">

                <xsl:for-each select="testbinary">
                    <testsuite errors="0">
                        <xsl:attribute name="name">
                            <xsl:value-of select="@path" />
                        </xsl:attribute>
                        <xsl:attribute name="tests">
                            <xsl:value-of select="count(testcase)" />
                        </xsl:attribute>
                        <xsl:attribute name="time">
                            <xsl:choose>
                                <xsl:when test="duration">
                                    <xsl:value-of select="xunit:junit-time(duration)" />
                                </xsl:when>
                                <xsl:otherwise>
        	                       <xsl:value-of select="xunit:junit-time(sum(testcase/duration))" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="failures">
                            <xsl:value-of select="count(testcase/status[@result='failed'])" />
                        </xsl:attribute>
                        <xsl:attribute name="skipped">
                            <xsl:value-of select="count(testcase/@skipped)" />
                        </xsl:attribute>

                        <xsl:for-each select="testcase">
                            <testcase>
                                <xsl:variable name="path">
                                    <xsl:call-template name="removedelimiter">
                                        <xsl:with-param name="string" select="@path" />
                                        <xsl:with-param name="token" select="'/'" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:attribute name="classname">
                                    <xsl:choose>
                                        <xsl:when test="contains($path, '/')">
                                            <xsl:value-of select="replace(substring($path, 1, xunit:last-index-of($path, '/') - 1), '/', '.')" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$path" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:choose>
                                        <xsl:when test="contains($path, '/')">
                                            <xsl:value-of select="replace(substring($path, xunit:last-index-of($path, '/') + 1), '/', '.')" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$path" />
                                        </xsl:otherwise>
                                    </xsl:choose>
	                           </xsl:attribute>
                                <xsl:attribute name="time">
	                               <xsl:value-of select="xunit:junit-time(duration)" />
	                           </xsl:attribute>
                                <xsl:if test="@skipped">
                                    <skipped />
                                </xsl:if>
                                <xsl:if test="status[@result = 'failed']">
                                    <failure>
                                        <xsl:attribute name="message">
                                           <xsl:value-of select="concat('exit-status=', status/@exit-status)" />
                                       </xsl:attribute>
                                        <xsl:value-of select="error/text()" />
                                    </failure>

                                    <xsl:if test="count(message) > 0">
                                        <system-out>
                                            <xsl:value-of select="string-join(message, '&#10;')" />
                                        </system-out>
                                    </xsl:if>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                </xsl:for-each>

            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>