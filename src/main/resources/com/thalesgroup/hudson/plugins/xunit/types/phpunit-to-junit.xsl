<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <xsl:element name="testsuites">
 	   <xsl:for-each select="testsuites/testsuite">	     
	       	<xsl:call-template name="testSuiteFirstLevel" />
       </xsl:for-each>
      </xsl:element>
   </xsl:template>

   <xsl:template name="testSuiteFirstLevel">

	 <xsl:variable name="curTestSuite" select="."/>
     <xsl:variable name="nbNestedTestSuites" select="count($curTestSuite/testsuite)" />      
     
	 <xsl:if test="$nbNestedTestSuites &gt; 0">	 	
	 	<xsl:for-each select="$curTestSuite/testsuite">	    
	 		<xsl:copy-of select="."/>
	 	</xsl:for-each>
	 </xsl:if>


	 <xsl:if test="$nbNestedTestSuites = 0">	 	
	 	<xsl:copy-of select="."/>
	 </xsl:if>

   
   </xsl:template>
   
   
   
</xsl:stylesheet>

