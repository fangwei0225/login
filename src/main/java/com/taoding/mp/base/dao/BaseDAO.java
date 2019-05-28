package com.taoding.mp.base.dao;

import com.taoding.mp.base.model.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Jdbc基础类
 *
 * @author wuwentan
 * @date 2018/7/16
 */
@Repository
public class BaseDAO {

    @Autowired
    protected JdbcTemplate jdbc;

    /**
     * 分页查询通用方法
     *
     * @param sql
     * @param pageNo
     * @param pageSize
     * @param params
     * @param rowMapper
     * @return
     */
    public PageVO getPage(String sql, int pageNo, int pageSize, List<Object> params, RowMapper rowMapper) {
        //查询条件
        List<Object> args = new ArrayList();
        args.addAll(params);
        //总条数
        String totalSql = "select count(*) from (" + sql + ") t ";
        int total = jdbc.queryForObject(totalSql, args.toArray(), Integer.class);
        //分页查询
        String querySql = "select * from (" + sql + ") t limit ?,? ";
        //分页参数
        pageSize = pageSize < 1 ? total : pageSize;
        pageNo = pageNo < 1 ? 0 : pageSize * (pageNo - 1);
        args.add(pageNo);
        args.add(pageSize);
        List list = jdbc.query(querySql, args.toArray(), rowMapper);
        return new PageVO<>(pageNo, pageSize, total, list);
    }

    /**
     * 通过数据库连接信息动态获取JdbcTemplate
     *
     * @param driverClassName 数据库驱动类：com.mysql.jdbc.Driver, oracle.jdbc.driver.OracleDriver
     * @param url             数据库连接
     * @param user            用户名
     * @param pass            密码
     * @return
     */
    public static JdbcTemplate getJdbcTemplate(String driverClassName, String url, String user, String pass) {
        try {
            Class.forName(driverClassName);
            DataSource dataSource = new DriverManagerDataSource(url, user, pass);
            //dataSource = new DriverManagerDataSource("jdbc:oracle:thin:@10.3.17.16:1521:lis","lis_cw","lis_cw");
            return new JdbcTemplate(dataSource);
        } catch (ClassNotFoundException e) {
            //logger.error("数据库驱动类加载失败！["+driverClassName+"]",e);
        }
        return null;
    }
}
