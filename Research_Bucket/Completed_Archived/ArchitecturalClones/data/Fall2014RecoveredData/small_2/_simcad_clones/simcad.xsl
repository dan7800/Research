<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template name="KeepLeadingSpace" >
	<xsl:param name="line" />
	<xsl:choose >
		<xsl:when test="contains($line, '&#x20;&#x20;')">
				
			<span style="color:#D6E4E1">~</span>
			
			<xsl:call-template name="KeepLeadingSpace">
				<xsl:with-param name="line" select="substring-after($line, '&#x20;')" />
			</xsl:call-template>
			
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$line" />
		</xsl:otherwise>
	</xsl:choose>
	 
</xsl:template>		
		
<xsl:template name="LFsToBRs" >
	<xsl:param name="input" />
	<xsl:choose>
		<xsl:when test="contains($input, '&#10;')">

			<xsl:choose>
			  <xsl:when test="string-length(substring-before($input, '&#10;')) > 0">
			    
			    <xsl:value-of select="substring-before($input, '&#10;')" xml:space="preserve"/>
			     <!--
			    <xsl:call-template name="KeepLeadingSpace">
					<xsl:with-param name="line" select="substring-before($input, '&#10;')" />
				</xsl:call-template>
				-->
			 	
			    <br />
			  </xsl:when>
			</xsl:choose> 
			
			<xsl:call-template name="LFsToBRs">
				<xsl:with-param name="input" select="substring-after($input, '&#10;')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$input" />
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />



<xsl:template match="/" >
  <html>
  <head>
  
  <link href="main.css" type="text/css" rel="stylesheet" />
  <link href="prettify.css" type="text/css" rel="stylesheet" />
  <script type="text/javascript" src="run_prettify.js"></script>
  </head>
  
  <body>    
    
<div>
<h3>SimCad Clone Detection Result</h3>
<table id="summary" cellspacing="0" cellpadding="0" border="0">
<tbody>
<tr>
<td class="label">SimCad Version</td><td>:</td><td class="value"><xsl:value-of select="SimCad/@version"/></td>
</tr>
<tr>
<td class="label">Detection TimeStamp</td><td>:</td><td class="value"><xsl:value-of select="SimCad/DetectionTimeStamp"/></td>
</tr>
<tr>
<td class="label">Search Input</td><td>:</td><td class="value"><xsl:value-of select="SimCad/SearchInput"/></td>
</tr>
<tr>
<td class="label">Search Target</td><td>:</td><td class="value"><xsl:value-of select="SimCad/SearchTarget"/></td>
</tr>
<tr>
<td class="label">Source FragmentType</td><td>:</td><td class="value"><xsl:value-of select="concat(translate(substring(SimCad/SourceFragmentType,1, 1), $smallcase, $uppercase), substring(SimCad/SourceFragmentType,2))"/></td>
</tr>
<tr>
<td class="label">Source Transformation</td><td>:</td><td class="value"><xsl:value-of select="concat(translate(substring(SimCad/SourceTransformation,1, 1), $smallcase, $uppercase), substring(SimCad/SourceTransformation,2))"/></td>
</tr>
<tr>
<td class="label">Clone Types</td><td>:</td><td class="value"><xsl:value-of select="SimCad/CloneType"/></td>
</tr>
<tr>
<td class="label">Clone Set Type</td><td>:</td><td class="value"><xsl:value-of select="concat(translate(substring(SimCad/CloneGroupingType,1, 1), $smallcase, $uppercase), substring(SimCad/CloneGroupingType,2))"/></td>
</tr>
<tr>
<td class="label">Total Source Fragments</td><td>:</td><td class="value"><xsl:value-of select="SimCad/SourceFragmentTotal"/></td>
</tr>
<tr>
<td class="label">Total Clone Fragments</td><td>:</td><td class="value"><xsl:value-of select="SimCad/CloneFragmentTotal"/></td>
</tr>
<tr>
<td class="label">Total Clone Sets</td><td>:</td><td class="value"><xsl:value-of select="SimCad/CloneSetTotal"/></td>
</tr>
</tbody>
</table>
</div>

<br/>

<xsl:for-each select="SimCad/Clones/CloneGroup">

<div class="CloneSet">

<div class="CloneSet_Header">
<span style="font-weight: bold">Clone Set Id</span> : <xsl:value-of select="@groupid"/> ||
<span style="font-weight: bold">Clone Type</span> : <xsl:value-of select="@type"/> ||
<span style="font-weight: bold">No. of Fragments</span> : <xsl:value-of select="@nfragments"/>
</div>
<hr/>

<xsl:for-each select="CloneFragment">

<div class="CloneFragment">

<div class="SourceCode" >

<?prettify?>
<pre class="prettyprint" style="border:none!important; align:left!important; margin-bottom:1px!important">

	<xsl:call-template name="LFsToBRs">
		<xsl:with-param name="input" select="."/>
	</xsl:call-template>

</pre>

</div>

<div class="CloneFragment_Header">
<span>Fragment Id</span> : <xsl:value-of select="@pcid"/> ||
<span>Source Location</span> : <xsl:value-of select="@file"/> (<xsl:value-of select="@startline"/>-<xsl:value-of select="@endline"/>) <br/>
</div>

</div>

</xsl:for-each>
</div> 
</xsl:for-each>

  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
  