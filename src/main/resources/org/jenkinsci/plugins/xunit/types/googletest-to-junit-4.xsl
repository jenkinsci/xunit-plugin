<!-- from src/main/resources/org/jenkinsci/plugins/xunit/types/googletest-to-junit-4.xsl -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" cdata-section-elements="system-out"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="//testsuites">
		<testsuites>
			<xsl:apply-templates/>
		</testsuites>
	</xsl:template>
	<xsl:template match="//testsuite">
		<testsuite>
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:attribute name="tests"><xsl:value-of select="@tests"/></xsl:attribute>
			<xsl:apply-templates select="testcase"/>
		</testsuite>
	</xsl:template>
	<xsl:template match="//testcase">
		<testcase>
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:attribute name="time"><xsl:value-of select="@time"/></xsl:attribute>
			<xsl:attribute name="classname"><xsl:value-of select="@classname"/></xsl:attribute>
			<xsl:if test="@status = 'notrun'">
					<skipped/>
			</xsl:if>
			<xsl:if test="failure">
				<failure>
					<xsl:for-each select="failure">
						<xsl:value-of select="@message"/>
						<xsl:text>&#xa;&#xa;</xsl:text>
					</xsl:for-each>
				</failure>
				<system-out>
					<xsl:for-each select="failure">
						<xsl:value-of select="."/>
						<xsl:text>&#xa;&#xa;</xsl:text>
					</xsl:for-each>
				</system-out>
			</xsl:if>
		</testcase>
	</xsl:template>
	<!-- this swallows all unmatched text -->
	<!-- <xsl:template match="text()|@*"/>-->
</xsl:stylesheet>
