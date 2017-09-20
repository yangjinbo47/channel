<%@ page language="java" pageEncoding="utf-8" session="false" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.List"%>
<%@ page import="java.lang.management.MemoryMXBean"%>
<%@ page import="java.lang.management.ManagementFactory"%>
<%@ page import="java.lang.management.ClassLoadingMXBean"%>
<%@ page import="java.lang.management.ThreadMXBean"%>
<%@ page import="java.lang.management.GarbageCollectorMXBean"%>
<html>
	<head>
		<meta http-equiv=content-type content="text/html; charset=UTF-8">
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Expires" content="0" />
		<title>UserAgent</title>
		<style>
		body {font-family: "微软雅黑", "宋体";margin: 0;padding: 0;font-size: 13px;}
		* {margin: 0;padding: 0;}
		p {line-height:190%;margin:3px 0;}
		form {margin-bottom:10px;}
		fieldset {padding:10px;}
		fieldset input {padding:3px;}
		fieldset select {width:100px;padding:3px;}
		.box {border:#DCDDDD 1px solid;background:#F7FCFE;width:700px;margin:5px auto 5px auto;}
		.box h1 {font-size:14px;margin:15px 10px;}
		.box th {color:#678FB1;font-size:13px;margin:0;padding:0 0 5px 0;text-align:left;border-bottom:#DCDDDD 1px solid;}
		.box td {font-size:13px;text-align:left;height:190%;line-height:190%;}
		.title {padding-right:20px;}
		</style>
	</head>
	<body>
		<%
			String ctl = request.getParameter("ctl") == null ? "UA" : request.getParameter("ctl").toUpperCase();
			// 打印HTTP HEAD
			if (ctl.equals("UA")){
				@SuppressWarnings("unchecked")
				Enumeration enu = request.getHeaderNames();
				while (enu.hasMoreElements()) {
					String tmp = (String) enu.nextElement();
					out.print("<p>" + tmp + " = " + request.getHeader(tmp) + "</p>");
					System.out.println(tmp + "=" + request.getHeader(tmp));
				}
				out.print("<p>REMOTE_ADDR = "+request.getRemoteAddr()+"</p>");
				out.print("<p>REMOTE_HOST = "+request.getRemoteHost()+"</p>");
			}
			// 显示系统信息
			if (ctl.equals("SYSTEM")){
				MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
				%>
				<center>
				<div class="box">
					<h1>JVM内存信息</h1>
					<table cellpadding="0" cellspacing="0" align="center" width="95%">
					<thead>
						<tr>
							<th>项目</th><th>初始化</th><th>已使用</th><th>已提交</th><th>最大值</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>堆内存</td>
							<td><b><%=memoryMXBean.getHeapMemoryUsage().getInit()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getHeapMemoryUsage().getUsed()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getHeapMemoryUsage().getCommitted()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getHeapMemoryUsage().getMax()/1024/1024 %></b>&nbsp;MB</td>
						</tr>
						<tr>
							<td>非堆内存</td>
							<td><b><%=memoryMXBean.getNonHeapMemoryUsage().getInit()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getNonHeapMemoryUsage().getUsed()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getNonHeapMemoryUsage().getCommitted()/1024/1024 %></b>&nbsp;MB</td>
							<td><b><%=memoryMXBean.getNonHeapMemoryUsage().getMax()/1024/1024 %></b>&nbsp;MB</td>
						</tr>
					</tbody>
					</table>
					<p>JVM内存总量:<b><%=Runtime.getRuntime().totalMemory()/1024/1024 %></b>&nbsp;MB&nbsp;&nbsp;&nbsp;&nbsp;JVM空闲内存：<b><%=Runtime.getRuntime().freeMemory()/1024/1024 %></b>&nbsp;MB&nbsp;&nbsp;&nbsp;&nbsp;JVM最大内存：<b><%=Runtime.getRuntime().maxMemory()/1024/1024 %></b>&nbsp;MB</p>
				</div>
				<%
				ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
				%>
				<div class="box">
					<h1>类加载信息</h1>
					<table cellpadding="0" cellspacing="0" align="center" width="95%">
					<thead>
						<tr>
							<th>项目</th><th>数量</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="title">当前加载到Java虚拟机中的类的数量</td><td><b><%=classLoadingMXBean.getLoadedClassCount() %></b>&nbsp;个</td>
						</tr>
						<tr>
							<td class="title">自Java虚拟机开始执行到目前已经加载的类的总数</td><td><b><%=classLoadingMXBean.getTotalLoadedClassCount() %></b>&nbsp;个</td>
						</tr>
						<tr>
							<td class="title">自Java虚拟机开始执行到目前已经卸载的类的总数</td><td><b><%=classLoadingMXBean.getUnloadedClassCount() %></b>&nbsp;个</td>
						</tr>
					</tbody>
					</table>
				</div>
				<%
				ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
				%>
				<div class="box">
					<h1>线程信息</h1>
					<table cellpadding="0" cellspacing="0" align="center" width="95%">
					<thead>
						<tr>
							<th>项目</th><th>数量</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="title">当前活动线程</td><td><b><%=threadMXBean.getThreadCount() %></b>&nbsp;个</td>
						</tr>
						<tr>
							<td class="title">当前守护线程</td><td><b><%=threadMXBean.getDaemonThreadCount() %></b>&nbsp;个</td>
						</tr>
						<tr>
							<td class="title">峰值活动线程</td><td><b><%=threadMXBean.getPeakThreadCount() %></b>&nbsp;个</td>
						</tr>
						<tr>
							<td class="title">当前线程的总CPU时间</td><td><b><%=threadMXBean.getCurrentThreadCpuTime()/1000 %></b>&nbsp;MS</td>
						</tr>
						<tr>
							<td class="title">当前线程在用户模式中执行的CPU时间</td><td><b><%=threadMXBean.getCurrentThreadUserTime()/1000 %></b>&nbsp;MS</td>
						</tr>
					</tbody>
					</table>
				</div>
				<%
				List<GarbageCollectorMXBean> garbageList = ManagementFactory.getGarbageCollectorMXBeans();
				%>
				<div class="box">
					<h1>垃圾回收信息</h1>
					<table cellpadding="0" cellspacing="0" align="center" width="95%">
					<thead>
						<tr>
							<th>收集器名称</th><th>总收集次数</th><th>累计收集时间</th>
						</tr>
					</thead>
					<tbody>
					<%
						for (GarbageCollectorMXBean garbage : garbageList) {
							%>
							<tr>
								<td><%=garbage.getName() %></td>
								<td><b><%=garbage.getCollectionCount() %>&nbsp;次</b></td>
								<td><b><%=garbage.getCollectionTime()/1000 %></b>&nbsp;S</td>
							</tr>
							<%
						}
					%>
					</tbody>
					</table>
				</div>
				</center>
				<%
			}
		%>
	</body>
</html>
