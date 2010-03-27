<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:a ="http://microsoft.com/schemas/VisualStudio/TeamTest/2006" xmlns:b ="http://microsoft.com/schemas/VisualStudio/TeamTest/2010" > 
	<xsl:output method="xml" indent="yes" />	
	<xsl:template match="/">
		<testsuites>		
			<xsl:variable name="buildName" select="//a:TestRun/@name"/>	
			<xsl:variable name="numberOfTests" select="count(//a:UnitTestResult/@outcome) + count(//b:UnitTestResult/@outcome)"/>
 			<xsl:variable name="numberOfFailures" select="count(//a:UnitTestResult/@outcome[.='Failed']) + count(//b:UnitTestResult/@outcome[.='Failed'])" />		
 			<xsl:variable name="numberSkipped" select="count(//a:UnitTestResult/@outcome[.!='Passed' and .!='Failed']) + count(//b:UnitTestResult/@outcome[.!='Passed' and .!='Failed'])" />	
			<testsuite name="MSTestSuite"
				tests="{$numberOfTests}" time="0"
				failures="{$numberOfFailures}"  errors="0"
				skipped="{$numberSkipped}">
				
				<xsl:for-each select="//a:UnitTestResult">
					<xsl:variable name="testName" select="@testName"/>
					<xsl:variable name="executionId" select="@executionId"/>
					<xsl:variable name="duration_seconds" select="substring(@duration, 7)"/>
					<xsl:variable name="duration_minutes" select="substring(@duration, 4,2 )"/>	
					<xsl:variable name="duration_hours" select="substring(@duration, 1, 2)"/>		
					<xsl:variable name="outcome" select="@outcome"/>	
					<xsl:variable name="message" select="a:Output/a:ErrorInfo/a:Message"/>	
					<xsl:variable name="stacktrace" select="a:Output/a:ErrorInfo/a:StackTrace"/>	
					<xsl:for-each select="//a:UnitTest">
						<xsl:variable name="currentExecutionId" select="a:Execution/@id"/>
						<xsl:if test="$currentExecutionId = $executionId" >
							<xsl:variable name="className" select="substring-before(a:TestMethod/@className, ',')"/>	
								<testcase classname="{$className}"
									name="{$testName}"
									time="{$duration_hours*3600 + $duration_minutes*60 + $duration_seconds }">

									<xsl:if test="contains($outcome, 'Failed')"> 
<failure>
MESSAGE:
<xsl:value-of select="$message" />
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="$stacktrace" />
</failure>
								</xsl:if>
							</testcase>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
				
				<xsl:for-each select="//b:UnitTestResult">
					<xsl:variable name="testName" select="@testName"/>
					<xsl:variable name="executionId" select="@executionId"/>
					<xsl:variable name="duration_seconds" select="substring(@duration, 7)"/>
					<xsl:variable name="duration_minutes" select="substring(@duration, 4,2 )"/>	
					<xsl:variable name="duration_hours" select="substring(@duration, 1, 2)"/>
					<xsl:variable name="outcome" select="@outcome"/>	
					<xsl:variable name="message" select="b:Output/b:ErrorInfo/b:Message"/>	
					<xsl:variable name="stacktrace" select="b:Output/b:ErrorInfo/b:StackTrace"/>	
					<xsl:for-each select="//b:UnitTest">
						<xsl:variable name="currentExecutionId" select="b:Execution/@id"/>
						<xsl:if test="$currentExecutionId = $executionId" >
							<xsl:variable name="className" select="substring-before(b:TestMethod/@className, ',')"/>	
								<testcase classname="{$className}"
									name="{$testName}"
									time="{$duration_hours*3600 + $duration_minutes*60 + $duration_seconds }">

									<xsl:if test="contains($outcome, 'Failed')"> 
<failure>
MESSAGE:
<xsl:value-of select="$message" />
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="$stacktrace" />
</failure>
								</xsl:if>
							</testcase>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
				
			</testsuite>
		</testsuites>
	</xsl:template>
</xsl:stylesheet>
