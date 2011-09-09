<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ck="http://check.sourceforge.net/ns"
                exclude-result-prefixes="ck">
    <xsl:output method="xml" indent="yes"/>

    <xsl:variable name="checkCount" select="count(//ck:suite/ck:test)"/>
    <xsl:variable name="checkCountFailure"
                  select="count(//ck:suite/ck:test[@result='failure'])"/>
    <xsl:variable name="suitename" select="//ck:suite/ck:title"/>

    <xsl:template match="/">
        <testsuite>
            <xsl:attribute name="errors">0</xsl:attribute>
            <xsl:attribute name="tests">
                <xsl:value-of select="$checkCount"/>
            </xsl:attribute>
            <xsl:attribute name="failures">
                <xsl:value-of select="$checkCountFailure"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="$suitename"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </testsuite>
    </xsl:template>

    <xsl:template match="//ck:suite/ck:test">
        <testcase>
            <xsl:attribute name="name">
                <xsl:value-of select="./ck:id"/>
            </xsl:attribute>
            <xsl:attribute name="classname">
                <xsl:value-of select="$suitename"/>
            </xsl:attribute>
            <xsl:attribute name="time">0</xsl:attribute>
            <xsl:if test="@result = 'failure'">
                <error type="error">
                    <xsl:attribute name="message">
                        <xsl:value-of select="./ck:message"/>
                    </xsl:attribute>
                </error>
            </xsl:if>
        </testcase>
    </xsl:template>

    <!-- this swallows all unmatched text -->
    <xsl:template match="text()|@*"/>
</xsl:stylesheet>