<?xml version="1.0" encoding="UTF-8"?>
<!-- this small scripts is written by Jan De Bleser (jan at commsquare dot com) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes" />
<xsl:template match="/">
<testsuites>
	<xsl:for-each select="TestResults[1]//TestListing[1]//TestSuite">
	<testsuite>
		<xsl:attribute name="name">
			<xsl:value-of select="@Name"/>
		</xsl:attribute>
		<xsl:attribute name="tests">
			<xsl:value-of select="@NumberOfRunTests"/>
		</xsl:attribute>
		<xsl:attribute name="time">
			<xsl:value-of select="@ElapsedTime"/>
		</xsl:attribute>
		<xsl:attribute name="failures">
			<xsl:value-of select="@NumberOfFailures"/>
		</xsl:attribute>
		<xsl:attribute name="errors">
			<xsl:value-of select="@NumberOfErrors"/>
		</xsl:attribute>
		<xsl:attribute name="skipped">
			<xsl:value-of select="@NumberOfIgnoredTests"/>
		</xsl:attribute>
		<xsl:for-each select="Test">
		<testcase>
			<xsl:attribute name="classname">
				<xsl:value-of select="../@Name"/>
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="@Name"/>
			</xsl:attribute>
			<xsl:attribute name="time">
				<xsl:value-of select="@ElapsedTime"/>
			</xsl:attribute>
			<xsl:if test="@Result='Failed'">
			<failure>
				<xsl:attribute name="message">
					<xsl:value-of select="Message"/>
				</xsl:attribute>
				<xsl:attribute name="type">
					<xsl:value-of select="ExceptionClass"/>
				</xsl:attribute>
				<xsl:value-of select="ExceptionMessage"/>
			</failure>
			</xsl:if>
			<xsl:if test="@Result='Error'">
			<failure>
				<xsl:attribute name="message">
					<xsl:value-of select="Message"/>
				</xsl:attribute>
				<xsl:attribute name="type">
					<xsl:value-of select="ExceptionClass"/>
				</xsl:attribute>
				<xsl:value-of select="SourceUnitName"/> (line <xsl:value-of select="LineNumber"/>):[<xsl:value-of select="FailedMethodName"/>]:<xsl:value-of select="ExceptionMessage"/>
			</failure>
			</xsl:if>
		</testcase>
		</xsl:for-each>
	</testsuite>
	</xsl:for-each>
</testsuites>
</xsl:template>
</xsl:stylesheet>