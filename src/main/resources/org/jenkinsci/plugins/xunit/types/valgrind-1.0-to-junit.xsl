<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014, Gregory Boissinot, Falco Nikolas

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
    <xsl:output method="xml" indent="yes" encoding="UTF-8" cdata-section-elements="system-out system-err failure"/>
    <xsl:decimal-format decimal-separator="." grouping-separator=","/>

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

    <xsl:function name="xunit:millis-from-time" as="xs:double">
        <xsl:param name="value" as="xs:string?" />

        <xsl:variable name="formattedTime" select="xunit:if-empty(string($value), '00:00:00')" />
        <xsl:variable name="formattedTime" select="replace(translate($formattedTime,',','.'), '^(\d:.+)', '0$1')" />
        <xsl:variable name="time" select="xs:time($formattedTime)" />
        <xsl:value-of select="hours-from-time($time)*3600 + minutes-from-time($time)*60 + seconds-from-time($time)" />
    </xsl:function>

    <xsl:variable name="tool" select="/valgrindoutput/tool"/>
    <xsl:variable name="numerrors" select="count(/valgrindoutput/error)"/>
    <xsl:variable name="xmlfile">
        <xsl:for-each select="/valgrindoutput/args/vargv/arg">
            <xsl:if test="substring-before(current(), '=') = '--xml-file'">
                <xsl:value-of select="substring-after(current(), '=')"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="casename">
        <xsl:choose>
            <xsl:when test="$xmlfile != ''">
                <xsl:call-template name="basename_noext">
                    <xsl:with-param name="path" select="$xmlfile"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="/valgrindoutput/args/argv/exe">
                <xsl:value-of select="/valgrindoutput/args/argv/exe"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="/valgrindoutput/pid"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="/valgrindoutput">
        <xsl:variable name="startTime" select="xunit:millis-from-time(xunit:if-empty(substring(status/state[text() = 'RUNNING']/../time, 4), 0))" />
        <xsl:variable name="endTime" select="xunit:millis-from-time(xunit:if-empty(substring(status/state[text() = 'FINISHED']/../time, 4), 0))" />

        <xsl:element name="testsuite">
            <xsl:attribute name="name" select="concat('valgrind-', $tool)" />
            <xsl:attribute name="tests" select="1" />
            <xsl:attribute name="errors" select="0" />
            <xsl:attribute name="time" select="xunit:junit-time($endTime - $startTime)" />
            <xsl:choose>
                <xsl:when test="$numerrors &gt;= 1">
                    <xsl:attribute name="failures" select="1" />
                    <testcase name="{$casename}">
                        <failure>
                            <xsl:apply-templates select="error" />
                        </failure>
                    </testcase>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="failures" select="0" />
                    <testcase name="{$casename}" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template match="error">
        <xsl:variable name="kind" select="kind"/>
        <xsl:for-each select="*">
            <xsl:choose>
                <xsl:when test="name()='what'">
                    <xsl:call-template name="what">
                        <xsl:with-param name="indent" select="string(' ')"/>
                        <xsl:with-param name="text" select="."/>
                        <xsl:with-param name="kind" select="$kind"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="name()='xwhat'">
                    <xsl:call-template name="what">
                        <xsl:with-param name="indent" select="string(' ')"/>
                        <xsl:with-param name="text" select="text"/>
                        <xsl:with-param name="kind" select="$kind"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="name()='auxwhat'">
                    <xsl:call-template name="what">
                        <xsl:with-param name="indent" select="string('  ')"/>
                        <xsl:with-param name="text" select="."/>

                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="name()='stack'">
                    <xsl:call-template name="stack"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template name="what">
        <xsl:param name="indent"/>
        <xsl:param name="text"/>
        <xsl:param name="kind"/>
        <xsl:variable name="link">
            <xsl:choose>
                <xsl:when test="$kind='InvalidRead' or $kind='InvalidWrite'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.badrw</xsl:text>
                </xsl:when>
                <xsl:when
                        test="$kind='InvalidJump' or $kind='InvalidMemPool' or $kind='UninitCondition' or $kind='UninitValue' or $kind='ClientCheck'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.uninitvals</xsl:text>
                </xsl:when>
                <xsl:when test="$kind='SyscallParam'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.bad-syscall-args</xsl:text>
                </xsl:when>
                <xsl:when test="$kind='InvalidFree'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.badfrees</xsl:text>
                </xsl:when>
                <xsl:when test="$kind='MisMatchedFree'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.rudefn</xsl:text>
                </xsl:when>
                <xsl:when test="$kind='Overlap'">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.overlap</xsl:text>
                </xsl:when>
                <xsl:when test="contains($kind, 'Leak')">
                    <xsl:text>http://valgrind.org/docs/manual/mc-manual.html#mc-manual.leaks</xsl:text>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="$indent"/>
        <xsl:value-of select="$text"/>
        <xsl:text>&#10;</xsl:text>
        <xsl:if test="$link != ''">
            <xsl:value-of select="$indent"/>
            <xsl:text>(see: </xsl:text>
            <xsl:value-of select="$link"/>
            <xsl:text>)</xsl:text>
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template name="stack">
        <xsl:apply-templates select="frame"/>
    </xsl:template>

    <xsl:template match="frame">
        <xsl:choose>
            <xsl:when test="position() = 1">
                <xsl:text>    at </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>    by </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="ip"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="fn"/>
        <xsl:if test="file">
            <xsl:text> </xsl:text>
            <xsl:text>(</xsl:text>
            <xsl:value-of select="file"/>
            <xsl:if test="line">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="line"/>
            </xsl:if>
            <xsl:text>)</xsl:text>
        </xsl:if>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="text()"/>

    <xsl:template name="basename_noext">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="contains($path, '/')">
                <xsl:call-template name="basename_noext">
                    <xsl:with-param name="path" select="substring-after($path, '/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring-before($path, '.')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
