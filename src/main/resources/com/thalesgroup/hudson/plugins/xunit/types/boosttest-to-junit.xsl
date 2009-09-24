<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" />


   <xsl:template match="/TestLog">    
 
         <xsl:choose>
            <xsl:when test="/TestLog/TestSuite/TestSuite">
      
	            <xsl:call-template name="TestSuiteElement">
			        <xsl:with-param name="childs" select="/TestLog/TestSuite/TestSuite/child::*"/>
			      	<xsl:with-param name="nbErrors">0</xsl:with-param>
			      	<xsl:with-param name="nbFailures">0</xsl:with-param>
			      	<xsl:with-param name="nbTests">0</xsl:with-param>      		
			      	<xsl:with-param name="suiteName" select="/TestLog/TestSuite/TestSuite/@name" />
			      	<xsl:with-param name="masterSuiteName" select="/TestLog/TestSuite/@name" />
	      		</xsl:call-template>
      		
      		</xsl:when>
		 	
		 	<xsl:otherwise>
		 	
		 		<xsl:call-template name="TestSuiteElement">
			        <xsl:with-param name="childs" select="/TestLog/TestSuite/child::*"/>
			      	<xsl:with-param name="nbErrors">0</xsl:with-param>
			      	<xsl:with-param name="nbFailures">0</xsl:with-param>
			      	<xsl:with-param name="nbTests">0</xsl:with-param>      		
			      	<xsl:with-param name="masterSuiteName" select="/TestLog/TestSuite/@name" />
	      		</xsl:call-template>
		 	
		 	</xsl:otherwise>
		 </xsl:choose> 	

   </xsl:template>



   <xsl:template match="/TestLog/TestSuite/TestCase">   
      <xsl:call-template name="testCase" />
   </xsl:template>

   <xsl:template match="/TestLog/TestSuite/TestSuite/TestCase">   
      <xsl:call-template name="testCase" />
   </xsl:template>

   <xsl:template name="TestSuiteElement">
                     
   		 <xsl:param name="nbErrors"/>
 		 <xsl:param name="nbFailures"/>
 		 <xsl:param name="nbTests"/>

		 <xsl:param name="childs"/>
		 
		 <xsl:param name="masterSuiteName"/>
		 
         <xsl:choose>
            <xsl:when test="count($childs) &gt; 0">

				 <xsl:variable name="testcaseElt" select="($childs[position()=1])" />		  
				 <xsl:variable name="errors" select="count($testcaseElt/FatalError)" /> 
				 <xsl:variable name="failures" select="count($testcaseElt/Error)" />

			     <xsl:call-template name="TestSuiteElement">
		 			<xsl:with-param name="childs" select="$testcaseElt/following-sibling::*"/>
		 			<xsl:with-param name="nbErrors" select="$nbErrors + $errors"/>
		 			<xsl:with-param name="nbFailures" select="$nbFailures + $failures"/>
		 			<xsl:with-param name="nbTests" select="$nbTests + 1"/>
		 			<xsl:with-param name="masterSuiteName" select="$masterSuiteName"/>
		 		</xsl:call-template> 			 				 
		 
		 	</xsl:when>
		 	
		 	<xsl:otherwise>
		 	
		 	
		      <testsuite>
		         <xsl:attribute name="tests">
		            <xsl:value-of select="$nbTests" />
		         </xsl:attribute>

		         <xsl:attribute name="errors">
		            <xsl:value-of select="$nbErrors" />
		         </xsl:attribute>

		         <xsl:attribute name="failures">
		            <xsl:value-of select="$nbFailures" />
		         </xsl:attribute>

		         <xsl:attribute name="name">
		         	<xsl:value-of select="$masterSuiteName" />
		         </xsl:attribute>
		          
		
		         <xsl:attribute name="skipped">0</xsl:attribute>
		
		         <xsl:apply-templates />
		      </testsuite>		 	

		 	</xsl:otherwise>
		 </xsl:choose> 		

   </xsl:template>

   <xsl:template name="testCaseContent">
      <xsl:for-each select="child::*">
         <xsl:variable name="currElt" select="." />
         <xsl:variable name="currEltName" select="name(.)" />         
         <xsl:choose>
            <xsl:when test="$currEltName='Error'">
=== <xsl:value-of select="($currElt)" />
File:<xsl:value-of select="($currElt)/@file" />
Line:<xsl:value-of select="($currElt)/@line" />
            </xsl:when>

            <xsl:when test="$currEltName='FatalError'">
<xsl:text/>
=== <xsl:value-of select="($currElt)" />
File:<xsl:value-of select="($currElt)/@file" />
Line:<xsl:value-of select="($currElt)/@line" />
            </xsl:when>
         </xsl:choose>
      </xsl:for-each>
   </xsl:template>



   <xsl:template name="testCaseMessageContent">
      <xsl:for-each select="child::Message">      	
         <xsl:variable name="currElt" select="." />
=== [Message] - <xsl:value-of select="($currElt)/@file" />:<xsl:value-of select="($currElt)/@line" />:<xsl:value-of select="($currElt)" />
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="testCaseWarningContent">
      <xsl:for-each select="child::Warning">      	
         <xsl:variable name="currElt" select="." />
=== [Warning] - <xsl:value-of select="($currElt)/@file" />:<xsl:value-of select="($currElt)/@line" />:<xsl:value-of select="($currElt)" />
      </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="testCaseInfoContent">
      <xsl:for-each select="child::Info">      	
         <xsl:variable name="currElt" select="." />
=== [Info] - <xsl:value-of select="($currElt)/@file" />:<xsl:value-of select="($currElt)/@line" />:<xsl:value-of select="($currElt)" />
      </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="testCaseExceptionContent">
      <xsl:for-each select="child::Exception">      	
         <xsl:variable name="currElt" select="." />
=== [Exception] - <xsl:value-of select="($currElt)/@file" />:<xsl:value-of select="($currElt)/@line" />:<xsl:value-of select="($currElt)" />
      </xsl:for-each>
   </xsl:template>   
      

    <xsl:template name="testCase">
      
       <xsl:variable name="curElt" select="."/>
       <xsl:variable name="suiteName" select="($curElt/parent::*)[1]/@name"/>
       <xsl:variable name="packageName" select="concat($suiteName, '.')"/>
      
      <testcase>
         <xsl:variable name="elt" select="(child::*[position()=1])" />
 	 	 <xsl:variable name="time" select="TestingTime" />
 		 
         <xsl:attribute name="classname">
            <xsl:value-of select="concat($packageName,  substring-before(($elt)/@file, '.'))" /> 
         </xsl:attribute>

         <xsl:attribute name="name">
            <xsl:value-of select="@name" />           
         </xsl:attribute>


         <xsl:attribute name="time">
            <xsl:value-of select="$time div 1000000" />
         </xsl:attribute>
         
         <xsl:variable name="nbErrors" select="count(Error)" />
         <xsl:variable name="nbFatalErrors" select="count(FatalError)" />

         <xsl:choose>
            <xsl:when test="$nbFatalErrors&gt;0">
               <error>
                  <xsl:call-template name="testCaseContent" />
               </error>
            </xsl:when>

            <xsl:when test="$nbErrors&gt;0">
               <failure>
                  <xsl:call-template name="testCaseContent" />
               </failure>
            </xsl:when>
         </xsl:choose>


		<system-out>
			<xsl:call-template name="testCaseMessageContent" />
			<xsl:call-template name="testCaseInfoContent" />
			<xsl:call-template name="testCaseWarningContent" />
		</system-out>		 	
		<system-err>
			<xsl:call-template name="testCaseExceptionContent" />
		</system-err>		 	


      </testcase>
   </xsl:template>



   <xsl:template match="text()|@*" />
</xsl:stylesheet>

