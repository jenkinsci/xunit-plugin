<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:a ="http://microsoft.com/schemas/VisualStudio/TeamTest/2006"
                xmlns:b ="http://microsoft.com/schemas/VisualStudio/TeamTest/2010" >
    <xsl:output encoding="UTF-8" method="xml" indent="yes" />
    <xsl:template match="/">
        <testsuites>
            <xsl:variable name="numberOfTests" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@total | /b:TestRun/b:ResultSummary/b:Counters/@total)"/>
            <xsl:variable name="numberOfFailures" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@failed | /b:TestRun/b:ResultSummary/b:Counters/@failed)" />
            <xsl:variable name="numberOfErrors" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@error | /b:TestRun/b:ResultSummary/b:Counters/@error | /a:TestRun/a:ResultSummary/a:Counters/@timeout | /b:TestRun/b:ResultSummary/b:Counters/@timeout)" />
            <xsl:variable name="skipped2006" select="/a:TestRun/a:ResultSummary/a:Counters/@inconclusive + /a:TestRun/a:ResultSummary/a:Counters/@total - /a:TestRun/a:ResultSummary/a:Counters/@executed"/>
            <xsl:variable name="skipped2010" select="/b:TestRun/b:ResultSummary/b:Counters/@inconclusive + /b:TestRun/b:ResultSummary/b:Counters/@total - /b:TestRun/b:ResultSummary/b:Counters/@executed"/>
            <xsl:variable name="numberSkipped">
                <xsl:choose>
                    <xsl:when test="$skipped2006 > 0"><xsl:value-of select="$skipped2006"/></xsl:when>
                    <xsl:when test="$skipped2010 > 0"><xsl:value-of select="$skipped2010"/></xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <testsuite name="MSTestSuite"
                tests="{$numberOfTests}" time="0"
                failures="{$numberOfFailures}"  errors="{$numberOfErrors}"
                skipped="{$numberSkipped}">

                <xsl:for-each select="//a:UnitTestResult[@resultType='DataDrivenDataRow' or not(@resultType)] | //b:UnitTestResult[@resultType='DataDrivenDataRow' or not(@resultType)] | //a:WebTestResult | //b:WebTestResult">
                    <xsl:variable name="stdout">
                        <xsl:for-each select="a:Output/a:StdOut | b:Output/b:StdOut">
                            <xsl:value-of select="text()"/><xsl:text>&#10;</xsl:text>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:if test="not(starts-with($stdout, 'omit-from-jenkins:true'))">
                        <xsl:variable name="testId" select="@testId" />
                        <xsl:variable name="duration" select="@duration"/>
                        <xsl:variable name="outcome" select="@outcome"/>
                        <xsl:variable name="message" select="a:Output/a:ErrorInfo/a:Message | b:Output/b:ErrorInfo/b:Message"/>
                        <xsl:variable name="stacktrace" select="a:Output/a:ErrorInfo/a:StackTrace | b:Output/b:ErrorInfo/b:StackTrace"/>
                        <xsl:variable name="textMessages">
                            <xsl:for-each select="a:Output/a:TextMessages/a:Message | b:Output/b:TextMessages/b:Message">
                                <xsl:value-of select="text()"/><xsl:text>&#10;</xsl:text>
                            </xsl:for-each>
                        </xsl:variable>
                        <xsl:variable name="stderr">
                            <xsl:for-each select="a:Output/a:StdErr | b:Output/b:StdErr">
                                <xsl:value-of select="text()"/><xsl:text>&#10;</xsl:text>
                            </xsl:for-each>
                        </xsl:variable>
                        <xsl:variable name="testName">
                            <xsl:choose>
                                <xsl:when test="starts-with($stdout, 'test-alternate-name:')">
                                    <xsl:value-of select="substring-before(substring-after($stdout, 'test-alternate-name:'), ':')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="@testName"/>
                                    <xsl:choose>
                                        <xsl:when test="starts-with($stdout, 'test-instance-name:')">
                                            <xsl:text>.</xsl:text><xsl:value-of select="substring-before(substring-after($stdout, 'test-instance-name:'), ':')"/>
                                        </xsl:when>
                                        <xsl:when test="@dataRowInfo"> row <xsl:value-of select="@dataRowInfo"/></xsl:when>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:for-each select="//a:UnitTest[@id=$testId]/a:TestMethod | //b:UnitTest[@id=$testId]/b:TestMethod | //a:WebTest[@id=$testId] | //b:WebTest[@id=$testId]">
                            <xsl:variable name="className">
                                <xsl:choose>
                                    <xsl:when test="contains(@className, ',')">
                                        <xsl:value-of select="substring-before(@className, ',')"/>
                                    </xsl:when>
                                    <xsl:when test="@storage"><xsl:value-of select="@storage"/></xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="@className"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:call-template name="format-test-case">
                                <xsl:with-param name="className" select="$className"/>
                                <xsl:with-param name="duration" select="$duration"/>
                                <xsl:with-param name="message" select="$message"/>
                                <xsl:with-param name="outcome" select="$outcome"/>
                                <xsl:with-param name="stacktrace" select="$stacktrace"/>
                                <xsl:with-param name="testName" select="$testName"/>
                                <xsl:with-param name="textMessages" select="$textMessages"/>
                                <xsl:with-param name="stdout" select="$stdout"/>
                                <xsl:with-param name="stderr" select="$stderr"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each>

            </testsuite>
        </testsuites>
    </xsl:template>

    <xsl:template name="format-test-case">
        <xsl:param name="className"/>
        <xsl:param name="testName"/>
        <xsl:param name="duration"/>
        <xsl:param name="outcome"/>
        <xsl:param name="message"/>
        <xsl:param name="stacktrace"/>
        <xsl:param name="textMessages"/>
        <xsl:param name="stdout"/>
        <xsl:param name="stderr"/>
        <xsl:variable name="duration_seconds" select="substring($duration, 7)"/>
        <xsl:variable name="duration_minutes" select="substring($duration, 4, 2 )"/>
        <xsl:variable name="duration_hours" select="substring($duration, 1, 2)"/>
        <testcase classname="{$className}" name="{$testName}">
            <xsl:if test="$duration">
                <xsl:attribute name="time">
                    <xsl:value-of select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="$outcome != 'Passed' or (not($outcome) and ($message or $stacktrace))">
                <xsl:variable name="tag">
                    <xsl:choose>
                        <xsl:when test="$outcome = 'Failed'">failure</xsl:when>
                        <xsl:when test="$outcome = 'NotExecuted' or $outcome = 'Inconclusive'">skipped</xsl:when>
                        <xsl:otherwise>error</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:element name="{$tag}">
                    <xsl:if test="$message">
                        <xsl:attribute name="message"><xsl:value-of select="$message" /></xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$stacktrace">
                        <xsl:value-of select="$stacktrace" />
                    </xsl:if>
                </xsl:element>
            </xsl:if>
            <xsl:if test="$textMessages != '' or $stdout != ''">
                <system-out>
                    <xsl:if test="$textMessages != ''"><xsl:value-of select="$textMessages"/></xsl:if>
                    <xsl:if test="$stdout != ''"><xsl:value-of select="$stdout"/></xsl:if>
                </system-out>
            </xsl:if>
            <xsl:if test="$stderr != ''">
                <system-err><xsl:value-of select="$stderr"/></system-err>
            </xsl:if>
        </testcase>
    </xsl:template>
</xsl:stylesheet>
