<%@ page language="java" pageEncoding="utf-8"%>
<%
	RequestDispatcher dispatcher = request.getRequestDispatcher("/manager/index.jsp");
	dispatcher.forward(request, response);
%>

