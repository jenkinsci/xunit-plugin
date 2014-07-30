<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

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
        <xsl:choose>
            <xsl:when test="$numerrors &gt;= 1">
                <testsuite name="valgrind-{$tool}" errors="1" tests="1">
                    <testcase name="{$casename}">
                        <failure>
                            <xsl:apply-templates select="error"/>
                        </failure>
                    </testcase>
                </testsuite>
            </xsl:when>
            <xsl:otherwise>
                <testsuite name="valgrind-{$tool}" errors="0" tests="1">
                    <testcase name="{$casename}"/>
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
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
