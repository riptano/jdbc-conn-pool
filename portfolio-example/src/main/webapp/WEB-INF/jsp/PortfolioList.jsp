<%@ include file="/WEB-INF/jsp/include.jsp" %>

<HTML xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<HEAD>
  <META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <LINK rel="stylesheet" href="<spring:url value="/static/styles/portfoliodemo.css" htmlEscape="true" />" type="text/css"/>
  <TITLE>DSE Portfolio Demo - Portfolio</TITLE>
</HEAD>

<BODY>
<DIV ID="main">
<TABLE>
  <TR>
    <TH>Portfolio Id</TH>
    <TH>Tickers</TH>
  </TR>
  <c:forEach var="portfolio" items="${portfolioList}">
    <TR>
      <TD>
        <spring:url value="Portfolio?id={portfolioId}" var="portfolioUrl">
            <spring:param name="portfolioId" value="${portfolio.name}"/>
        </spring:url>
        <A HREF="${fn:escapeXml(portfolioUrl)}">${portfolio.name}</A>
      </TD>
      <TD>
        <c:forEach var="ticker" items="${portfolio.tickers}">
           ${ticker}&nbsp;
        </c:forEach>
      </TD>
    </TR>
  </c:forEach>
</TABLE>
</BODY>
</HTML>
