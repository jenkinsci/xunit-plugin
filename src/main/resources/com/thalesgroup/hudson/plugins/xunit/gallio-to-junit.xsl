<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:a="http://www.gallio.org/">
  <xsl:template match="/">
    <xsl:for-each select="a:report/a:testPackageRun">
      <testsuites>
        <xsl:for-each select="a:testStepRun/a:children/a:testStepRun/a:children/a:testStepRun/a:children/a:testStepRun">
          <testsuite skipped="0" failures="0" errors="0">
            <xsl:attribute name="time">
              <xsl:value-of select="a:result/@duration"/>
            </xsl:attribute>
            <xsl:attribute name="tests">
              <xsl:value-of select="count(a:children/a:testStepRun)"/>
            </xsl:attribute>
            <xsl:attribute name="errors">
              <xsl:value-of select="count(a:children/a:testStepRun/a:result/a:outcome[@status = 'failed' and @category='error'])"/>
            </xsl:attribute>
            <xsl:attribute name="failures">
              <xsl:value-of select="count(a:children/a:testStepRun/a:result/a:outcome[@status = 'failed'])"/>
            </xsl:attribute>
            <xsl:attribute name="skipped">
              <xsl:value-of select="count(a:children/a:testStepRun/a:result/a:outcome[@status = 'skipped'])"/>
            </xsl:attribute>
            <xsl:attribute name="name">
              <xsl:value-of select="a:testStep/a:codeReference/@type"/>
            </xsl:attribute>
            <xsl:for-each select="a:children/a:testStepRun">
              <xsl:if test="a:result/a:outcome/@status != 'skipped'">
                <testcase>
                  <xsl:attribute name="time">
                    <xsl:value-of select="a:result/@duration"/>
                  </xsl:attribute>
                  <xsl:attribute name="name">
                    <xsl:value-of select="a:testStep/@name"/>
                  </xsl:attribute>
                  <xsl:attribute name="classname">
                    <xsl:value-of select="../../a:testStep/a:codeReference/@type"/>
                  </xsl:attribute>
                  <xsl:if test="a:result/a:outcome/@status = 'failed'">
                    <failure>
                      <xsl:attribute name="message">
                        <!--for Gallio/Nunit-->
                        <xsl:value-of select="a:testLog/a:streams/a:stream/a:body/a:contents/a:section/a:contents/a:text"/>
                        <!--for Gallio/Mbunit-->
                        <!--ExceptionMessage Gallio 3.0.6 !?!-->
                        <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:section/a:contents/a:marker">
                          <xsl:if test="@class = 'Exception'">
                          <xsl:for-each select="a:contents/a:marker">
                          <xsl:if test="@class = 'ExceptionType'">
                            <xsl:value-of select="a:contents/a:text"/>
                          </xsl:if>
                        </xsl:for-each>
                        <xsl:value-of select="a:contents/a:text"/>
                        <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:section/a:contents/a:marker/a:contents/a:marker">
                          <xsl:if test="@class = 'ExceptionMessage'">
                            <xsl:value-of select="a:contents/a:text"/>
                          </xsl:if>
                        </xsl:for-each>
                        </xsl:if>
                        </xsl:for-each>
                        <!--ExceptionMessage-->
                        <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:marker">
                          <xsl:if test="@class = 'ExceptionMessage'">
                            <xsl:value-of select="a:contents/a:text"/>
                          </xsl:if>
                        </xsl:for-each>
                        <!--Assertion Failure-->
                        <xsl:value-of select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:section/@name"/>
                        <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:section/a:contents/a:marker/a:contents/a:marker">
                          <xsl:value-of select="a:contents/a:text"/>
                        </xsl:for-each>
                        <xsl:value-of select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:section/a:contents/a:marker/a:contents/a:text"/>
                      </xsl:attribute>
                      <!--=====StackTrace=====-->
                      <!--for Gallio/Nunit-->
                      <xsl:value-of select="a:testLog/a:streams/a:stream/a:body/a:contents/a:section/a:contents/a:marker/a:contents/a:text"/>
                      <!--for Gallio/Mbunit-->
                      <!--ExceptionMessage Gallio 3.0.6 !?!-->
                      <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:section/a:contents/a:marker/a:contents/a:marker" >
                        <xsl:if test="@class = 'StackTrace'">
                          <xsl:value-of select="a:contents/a:text"/>
                          <xsl:for-each select="a:contents/a:marker">
                            <xsl:value-of select="a:contents/a:text"/>
                          </xsl:for-each>
                        </xsl:if>
                      </xsl:for-each>
                      <!--StackTrace Gallio 3.0.5-->
                      <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:marker">
                        <xsl:if test="@class = 'StackTrace'">
                          <xsl:value-of select="a:contents/a:text"/>
                        </xsl:if>
                      </xsl:for-each>
                      <!--<xsl:value-of select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:section/a:contents/a:marker/a:contents/a:text"/>-->
                      <xsl:for-each select="a:testLog/a:streams/a:stream/a:body/a:contents/a:marker/a:contents/a:section/a:contents/a:marker">
                        <xsl:if test="@class = 'StackTrace'">
                          <xsl:value-of select="a:contents/a:text"/>
                        </xsl:if>
                      </xsl:for-each>
                    </failure>
                  </xsl:if>
                </testcase>
              </xsl:if>
            </xsl:for-each>
          </testsuite>
        </xsl:for-each>
      </testsuites>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>