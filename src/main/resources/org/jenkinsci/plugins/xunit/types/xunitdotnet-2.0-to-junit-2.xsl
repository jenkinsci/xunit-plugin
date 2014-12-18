<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014, Jedidja Bourgeois / Dave Hamilton

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
  <xsl:output method="xml" indent="yes" encoding="UTF-8"
              cdata-section-elements="failure skipped"/>

  <xsl:template match="/assemblies">
    <testsuites>
      <xsl:for-each select="assembly">

        <xsl:variable name="assemblyFileName">
          <xsl:call-template name="substring-after-last">
            <xsl:with-param name="string" select="@name" />
            <xsl:with-param name="delimiter" select="'\'" />
          </xsl:call-template>
        </xsl:variable>
        
        <xsl:variable name="assemblyName" select="substring-before($assemblyFileName,'.DLL')"/>
        
        <xsl:variable name="timeStamp">
          <xsl:value-of select="concat(@run-date, 'T', @run-time)"/>
        </xsl:variable>

        <testsuite name="{$assemblyName}"
         tests="{@total}" time="{@time}" timestamp="{$timeStamp}"
         failures="{@failed}" errors="{@errors}" skipped="{@skipped}">

          <properties>
            <property name="environment" value="{@environment}"/>
            <property name="test-framework" value="{@test-framework}"/>
            <property name="config-file" value="{@config-file}"/>
          </properties>

          <xsl:for-each select="collection/test">

            <xsl:variable name="testMethodName" select="substring(@name, string-length(@type)+2)"/>

            <testcase classname="{@type}" name="{$testMethodName}" time="{@time}">

              <xsl:if test="./failure">
                <failure>
MESSAGE:
<xsl:value-of select="normalize-space(./failure/message)"/>
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="normalize-space(./failure/stack-trace)"/>
                </failure>
              </xsl:if>

              <xsl:if test="@result='Skip'">
                <skipped>
                  <xsl:attribute name="message">
                    <xsl:value-of select="normalize-space(./reason)"/>
                  </xsl:attribute>
                </skipped>
              </xsl:if>

            </testcase>
          </xsl:for-each>
        </testsuite>

      </xsl:for-each>
    </testsuites>
  </xsl:template>

  <xsl:template name="substring-after-last">
    <xsl:param name="string" />
    <xsl:param name="delimiter" />
    <xsl:choose>
      <xsl:when test="contains($string, $delimiter)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="string"
            select="substring-after($string, $delimiter)" />
          <xsl:with-param name="delimiter" select="$delimiter" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
     select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>