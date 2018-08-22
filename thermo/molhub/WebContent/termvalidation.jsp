<?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<style>
td {width:100%;float:left;clear:left; white-space:pre }
</style>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Query submission notification</title>
</head>
<body>

<h3>Thank you for your query.</h3>

<s:bean name="uk.ac.ceb.como.molhub.bean.Term" var="termBean"/>

<p>Your query string is: <s:property value="term" /></p>

<p>Your propositional formula is: <s:property value="formula" /> </p>

<p>Your periodic table elements are: <s:property value="periodicTableElement" /> </p>

<p>Is propositional formula satisfiable (true in at least one valuation)? : <s:property value="satisfiable" /></p>

<h3> Search Results: </h3>

<s:iterator value="queryResult">
<s:property value="moleculeId"/>
<s:property value="moleculeName"/>
<P/>
</s:iterator>

<s:iterator value="queryResultString">
<s:property/>
<P/>
</s:iterator>

</body>
</html>