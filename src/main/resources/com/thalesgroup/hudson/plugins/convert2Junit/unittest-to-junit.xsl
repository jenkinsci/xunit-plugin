<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" />

   <xsl:template match="/">
      <testsuite>
         <xsl:attribute name="errors">
            <xsl:value-of select="unittest-results/@failedtests" />
         </xsl:attribute>

         <xsl:attribute name="failures">
            <xsl:value-of select="unittest-results/@failures" />
         </xsl:attribute>
         
         <xsl:attribute name="tests">
            <xsl:value-of select="unittest-results/@tests" />
         </xsl:attribute>         

         <xsl:attribute name="name">unittest</xsl:attribute>  
         
         <xsl:apply-templates />
      </testsuite>
   </xsl:template>

   <xsl:template match="/unittest-results/test">
      <testcase>


         <xsl:attribute name="classname">
            <xsl:value-of select="@suite" />
         </xsl:attribute>

         <xsl:attribute name="name">
            <xsl:value-of select="@name" />
         </xsl:attribute>

         <xsl:attribute name="time">0</xsl:attribute>
         
          <xsl:copy-of select="child::*" />

      </testcase>
   </xsl:template>

</xsl:stylesheet>