package cn.hmcode.middle.db.router.strategy.impl;

import cn.hmcode.middle.db.router.DBContextHolder;
import cn.hmcode.middle.db.router.DBRouterConfig;
import cn.hmcode.middle.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 哈希路由策略算法实现
 * @author: hm
 * @date: 2023/5/11
 */
public class DBRouterStrategyImpl implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyImpl.class);

    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyImpl(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }


    /*
     *通过配置文件中所定义得库 表数量 获取乘积
     * 再基于hashmap得扰动函数得实现思路，进一步求出库表索引
     * 并将库表索引设置到Theadlocal中去
     */
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

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }

}
