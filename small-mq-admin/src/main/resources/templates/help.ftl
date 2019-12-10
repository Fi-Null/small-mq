<!DOCTYPE html>
<html>
<head>
	<#import "./common/common.macro.ftl" as netCommon>
  	<title>消息队列中心</title>
	<@netCommon.commonStyle />
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["smallmq_adminlte_settings"]?exists && "off" == cookieMap["smallmq_adminlte_settings"].value >sidebar-collapse</#if> ">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "help" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>使用教程<small></small></h1>
		</section>

		<!-- Main content -->
		<section class="content">
			<div class="callout callout-info">
				<h4>分布式消息队列SMALL-MQ</h4>
				<br>
				<p>
                    <a target="_blank" href="https://github.com/Fi-Null/small-mq">Github</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <br><br>
                    <a target="_blank" href="https://github.com/Fi-Null/small-mq/">官方文档</a>
                    <br><br>

				</p>
				<p></p>
            </div>
		</section>
		<!-- /.content -->
	</div>
	<!-- /.content-wrapper -->
	
	<!-- footer -->
	<@netCommon.commonFooter />
</div>
<@netCommon.commonScript />
</body>
</html>
