<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" indent="yes" />
  <xsl:decimal-format decimal-separator="." grouping-separator="," />

  <!-- misc variables -->
  <xsl:variable name="classname" select="/TestCase/@name" />
  <xsl:variable name="total-tests" select="count(/TestCase/TestFunction)" />
  <xsl:variable name="total-failures" select="count(/TestCase/TestFunction/Incident[@type='fail'])" />

  <!-- main template call -->
  <xsl:template match="/">
    <xsl:apply-templates select="TestCase"/>
  </xsl:template>

  <xsl:template match="TestCase">
    <xsl:variable name="msecsTest">
      <xsl:choose>
        <xsl:when test="Duration"><xsl:value-of select="Duration/@msecs" /></xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <testsuite name="{$classname}" tests="{$total-tests}" failures="{$total-failures}" errors="0" time="{format-number($msecsTest div 1000,'0.000')}">
      <xsl:apply-templates select="Environment"/>
      <xsl:apply-templates select="TestFunction" />
      <xsl:call-template name="display-system-out" />
      <xsl:call-template name="display-system-err" />
    </testsuite>
  </xsl:template>

  <xsl:template match="Environment">
    <properties>
      <xsl:for-each select="*">
        <property name="{name()}" value="{text()}" />
      </xsl:for-each>
    </properties>
  </xsl:template>

  <xsl:template match="TestFunction">
    <xsl:variable name="msecsFunction">
      <xsl:choose>
        <xsl:when test="Duration"><xsl:value-of select="Duration/@msecs" /></xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <testcase classname="{$classname}" name="{@name}" time="{format-number($msecsFunction div 1000,'0.000')}">

      <!-- handle skip -->
      <xsl:if test="Message/@type = 'skip'">
        <!-- will be used to generate "nice" error message -->
        <xsl:variable name="file" select="Message[@type='skip']/@file" />
        <xsl:variable name="line" select="Message[@type='skip']/@line" />
        <xsl:variable name="description">
          <xsl:value-of select="Message[@type='skip']/Description" />
        </xsl:variable>

        <!-- display a reasonable skipped message -->
        <xsl:element name="skipped">
            <xsl:value-of select="concat($file,':',$line,' :: ',$description)" />
        </xsl:element>
      </xsl:if>

      <!-- handle fail -->
      <xsl:if test="Incident/@type = 'fail'">
        <!-- will be used to generate "nice" error message -->
        <xsl:variable name="file" select="Incident[@type='fail']/@file" />
        <xsl:variable name="line" select="Incident[@type='fail']/@line" />
        <xsl:variable name="description">
          <xsl:value-of select="Incident[@type='fail']/Description" />
        </xsl:variable>

        <!-- display a reasonable error message -->
        <xsl:element name="failure">
          <xsl:attribute name="type">failure</xsl:attribute>
          <xsl:attribute name="message">
            <xsl:value-of select="concat($file,':',$line,' :: ',$description)" />
          </xsl:attribute>
        </xsl:element>
      </xsl:if>

      <!-- handle xfail -->
      <xsl:if test="Incident/@type = 'xfail'">
        <system-out>
          <xsl:for-each select="Incident[@type='xfail']">
            <!-- will be used to generate "nice" error message -->
            <xsl:variable name="file" select="@file" />
            <xsl:variable name="line" select="@line" />
            <xsl:variable name="description">
              <xsl:value-of select="Description" />
            </xsl:variable>

            <!-- display a reasonable error message -->
            <xsl:text>&#10;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;![CDATA[XFAIL : </xsl:text>
            <xsl:value-of select="concat($file,':',$line,' :: ',$description)" disable-output-escaping="yes"/>
            <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
          </xsl:for-each>
        </system-out>
      </xsl:if>

      <!-- handle pass -->
      <xsl:if test="Incident/@type = 'pass'">
        <xsl:if test="Message[@type='qdebug'] | Message[@type='qwarn'] | Message[@type='warn']">
          <system-err>
            <xsl:for-each select="Message[@type='qdebug'] | Message[@type='qwarn'] | Message[@type='warn']">
              <xsl:choose>
                <xsl:when test="@type='qdebug'">
                  <xsl:text>&#10;</xsl:text>
                  <xsl:text disable-output-escaping="yes">&lt;![CDATA[QDEBUG : </xsl:text>
                  <xsl:value-of select="Description" disable-output-escaping="yes"/>
                  <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                </xsl:when>
                <xsl:when test="@type='qwarn'">
                  <xsl:text>&#10;</xsl:text>
                  <xsl:text disable-output-escaping="yes">&lt;![CDATA[QWARN : </xsl:text>
                  <xsl:value-of select="Description" disable-output-escaping="yes"/>
                  <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                </xsl:when>
                <xsl:when test="@type='warn'">
                  <xsl:text>&#10;</xsl:text>
                  <xsl:text disable-output-escaping="yes">&lt;![CDATA[WARNING : </xsl:text>
                  <xsl:value-of select="Description" disable-output-escaping="yes"/>
                  <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                </xsl:when>
              </xsl:choose>
            </xsl:for-each>
          </system-err>
        </xsl:if>
      </xsl:if>

    </testcase>

  </xsl:template>

  <xsl:template name="display-system-out">
    <system-out/>
  </xsl:template>

  <xsl:template name="display-system-err">
    <system-err/>
  </xsl:template>

</xsl:stylesheet>
