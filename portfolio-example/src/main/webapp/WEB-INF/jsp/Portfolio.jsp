<%@ include file="/WEB-INF/jsp/include.jsp" %>

<HTML xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<HEAD>
  <META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <LINK rel="stylesheet" href="<spring:url value="/static/styles/portfoliodemo.css" htmlEscape="true" />" type="text/css"/>
  <TITLE>DSE Portfolio Demo - Portfolio : ${portfolio.name}</TITLE>
</HEAD>

<BODY>
<DIV ID="main">
Portfolio Name : ${portfolio.name}<BR/>
<LI>Price : ${portfolio.formattedPrice}<BR/>
<LI>Basis : ${portfolio.formattedBasis}<BR/>
<P/>
<TABLE>
 <TR>
  <TH>Ticker</TH>
  <c:forEach var="position" items="${portfolio.constituents}">
    <TH>${position.ticker}</TH>
  </c:forEach>
 </TR>
 <TR>
  <TD># Shares</TD>
  <c:forEach var="position" items="${portfolio.constituents}">
    <TD>${position.shares}</TD>
  </c:forEach>
 </TR>
 <TR>
  <TD>Price</TD>
  <c:forEach var="position" items="${portfolio.constituents}">
    <TD>${position.formattedPrice}</TD>
  </c:forEach>
 </TR>
</TABLE>
</BODY>
</HTML>
