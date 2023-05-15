# hmcode-springboot-starter
hmcode-springboot-starter

关于这个路由组件 我们得做法是
通过一个自定义注解DbRouter，属性就是分库分表得字段key

然后会基于DbRouterJoinPoint得切面类 通过环绕通知来解析自定义注解，
具体得逻辑就是说 
     获取分库分表得字段key，然后判断字段是否非空-为空直接抛异常
    如果不为空，就获取到路由属性，并基于路由属性 来执行路由策略得逻辑
    路由策略得具体逻辑是
		通过配置文件中所定义得库 表数量 获取乘积
		再基于hashmap得扰动函数得实现思路，进一步求出库表索引
		并将``库表索引设置到Theadlocal中去
		
	```
	@Override
    public void doRouter(String dbKeyAttr) {
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 扰动函数
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 库表索引
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 设置到 ThreadLocal
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由 dbIdx：{} tbIdx：{}", dbIdx, tbIdx);
    }
	```
	
而接下来就是需要向数据集里写东西，--就是会通过mybatis拦截器得任务来完成
而这里得实现思路是通过实现Interceptor接口（Interceptor接口是mybatis拦截器得核心接口）
并在接口中添加@Intercepts注解
而@Intercepts注解是通过拦截点 来拦截对象里面得某个方法
intercept方法是拦截器得核心方法，当拦截条件满足时会执行该方法，
具体得实现逻辑就是说 
	1 首先获取当前sql对应得mappedStatement对象，并根据自定义注解@DBRouterStrategy判断是否需要进行分表操作，
	2 如果不需要直接调用invocation.proceed()方法 执行原有逻辑
	3 如果需要进行分表操作，就通过statementHandler 获取BoundSql对象，并从中提取出sql语句，
	4 然后使用表名匹配规则从sql中提取出表名，并根据当前分表策略进行替换，
	最后通过反射将替换后得sql语句赋值回给BoundSql对象中得sql字段
https://t.zsxq.com/0eMMC9jqQ