<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="xml"
                indent="yes"/>
    <xsl:template match="/">
        <testsuites>
            <xsl:apply-templates/>
        </testsuites>
    </xsl:template>
    <xsl:template match="//TestRun/*">
        <testsuite>
            <xsl:attribute name="errors">0</xsl:attribute>
            <xsl:attribute name="failures">
                <xsl:value-of select="//Statistics/Failures"/>
            </xsl:attribute>
            <xsl:attribute name="tests">
                <xsl:value-of select="//Statistics/Tests"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="name(.)"/>
            </xsl:attribute>
            <properties></properties>
            <system-err></system-err>
            <system-out></system-out>
            <xsl:apply-templates/>
        </testsuite>
    </xsl:template>
    <xsl:template match="//TestRun/Statistics"></xsl:template>
    <xsl:template match="//TestRun/*/Test">
        <testcase>
            <xsl:attribute name="name">
                <xsl:value-of select="Name"/>
            </xsl:attribute>
        </testcase>
    </xsl:template>
    <xsl:template match="//TestRun/*/FailedTest">
        <testcase>
            <xsl:attribute name="name">
                <xsl:value-of select="Name"/>
            </xsl:attribute>
            <failure>
                <xsl:attribute name="message">
                    <xsl:value-of select="Message"/>
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="Location/File"/>
                    <xsl:text>:</xsl:text>
                    <xsl:value-of select="Location/Line"/>
                    <xsl:text>)</xsl:text>
                </xsl:attribute>
            </failure>
        </testcase>
    </xsl:template>
    <xsl:template match="text()|@*"/>
</xsl:stylesheet>
