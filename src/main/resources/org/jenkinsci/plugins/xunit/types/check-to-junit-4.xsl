<!-- from src/main/resources/org/jenkinsci/plugins/xunit/types/check-to-junit-4.xsl -->
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