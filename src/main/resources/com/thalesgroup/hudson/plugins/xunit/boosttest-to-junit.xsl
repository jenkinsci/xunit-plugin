<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" />

   <xsl:template match="/">
      <testsuite>
         <xsl:attribute name="errors">
            <xsl:value-of select="TestLog/TestSuite/Errors" />
         </xsl:attribute>

         <xsl:apply-templates />
      </testsuite>
   </xsl:template>

   <xsl:template match="/TestLog/TestSuite/TestSuite/TestCase">
      <testcase>
         <xsl:variable name="elt" select="(child::*[position()=1])" />

         <xsl:variable name="time" select="(child::*[position()=2])" />

         <xsl:attribute name="classname">
            <xsl:value-of select="($elt)/@file" />
         </xsl:attribute>

         <xsl:attribute name="name">
            <xsl:value-of select="@name" />
         </xsl:attribute>

         <xsl:attribute name="time">
            <xsl:value-of select="($time)" />
         </xsl:attribute>

         <xsl:choose>
            <xsl:when test="name($elt)='Error'">
               <error>
               <xsl:attribute name="message">
                  <xsl:value-of select="($elt)" />
               </xsl:attribute>
               <xsl:value-of select="($elt)" />
               File:<xsl:value-of select="($elt)/@file" />
               Line:<xsl:value-of select="($elt)/@line" />
               </error>
            </xsl:when>

            <xsl:otherwise>
               <failure>
               <xsl:attribute name="message">
                  <xsl:value-of select="($elt)" />
               </xsl:attribute>
               <xsl:value-of select="($elt)" />
               File:<xsl:value-of select="($elt)/@file" />
               Line:<xsl:value-of select="($elt)/@line" />
               </failure>
            </xsl:otherwise>
         </xsl:choose>
      </testcase>
   </xsl:template>

   <xsl:template match="text()|@*" />
</xsl:stylesheet>

