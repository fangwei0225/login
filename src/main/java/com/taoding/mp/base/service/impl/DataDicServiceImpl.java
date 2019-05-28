package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.DataDicRepository;
import com.taoding.mp.base.entity.DataDictionary;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.DataDicService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.IdWorker;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典接口实现类
 * @author wuwentan
 * @date 2019/3/7
 */
@Service
public class DataDicServiceImpl extends BaseDAO implements DataDicService {

    @Autowired
    DataDicRepository dataDicRepository;

    @Override
    public PageVO<DataDictionary> page(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String type = params.get("type");
        String name = params.get("name");
        String corpId = UserSession.getUserSession().getCorpId();

        StringBuilder sql = new StringBuilder("select * from data_dictionary where corp_id = ? and is_delete = 1 ");
        List<Object> args = new ArrayList<>();
        args.add(corpId);
        if (StringUtils.isNotBlank(type)) {
            sql.append("and type = ? ");
            args.add(type);
        }if (StringUtils.isNotBlank(name)) {
            sql.append("and name like ? ");
            args.add("%" + name + "%");
        }
        sql.append("order by num desc,create_time ");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(DataDictionary.class));
    }

    @Override
    public List<Map<String, Object>> list(String type, String parentId) {
        String corpId = UserSession.getUserSession().getCorpId();
        String sql = "select id,name,data_value AS dataValue,remark from data_dictionary where corp_id = ? and is_delete = 1 and type = ? ";
        sql += "and parent_id = ? order by num desc,create_time ";
        return jdbc.queryForList(sql, corpId, type, parentId);
    }

    @Override
    public List<DataDictionary> treeList(String name, String type) {
        String corpId = UserSession.getUserSession().getCorpId();
        StringBuilder sql = new StringBuilder("select * from data_dictionary where corp_id = ? and is_delete = 1 ");
        List<Object> args = new ArrayList<>();
        args.add(corpId);
        if (StringUtils.isNotBlank(type)) {
            sql.append("and type = ? ");
            args.add(type);
        }if (StringUtils.isNotBlank(name)) {
            sql.append("and name like ? ");
            args.add("%" + name + "%");
        }
        sql.append("order by num desc,create_time ");
        List<DataDictionary> list = jdbc.query(sql.toString(), args.toArray(), new BeanPropertyRowMapper<>(DataDictionary.class));
        list = makeTreeList(list,"0");
        return list;
    }

    /**
     * 递归数据字典树形结构
     * @param ddList
     * @param pid
     * @return
     */
    private List<DataDictionary> makeTreeList(List<DataDictionary> ddList, String pid){
        //直属下级节点
        List<DataDictionary> children = ddList.stream().filter(x -> x.getParentId().equals(pid)).collect(Collectors.toList());
        //非直属节点
        List<DataDictionary> peer = ddList.stream().filter(x -> !x.getParentId().equals(pid)).collect(Collectors.toList());
        children.forEach(d -> makeTreeList(peer, d.getId()).forEach(p -> d.getChildren().add(p)));
        return children;
    }

    @Override
    public DataDictionary save(DataDictionary dd) {
        if(StringUtils.isBlank(dd.getId())){
            dd.setId(IdWorker.createId());
            dd.setCreateTime(CommonUtils.getStringDate(new Date()));
            dd.setUpdateTime(CommonUtils.getStringDate(new Date()));
            dd.setCorpId(UserSession.getUserSession().getCorpId());
            dd.setIsDelete(Constants.STATUE_NORMAL);
        }else{
            DataDictionary source = this.getById(dd.getId());
            UpdateUtils.copyNonNullProperties(source, dd);
        }
        if(StringUtils.isBlank(dd.getParentId())){
            dd.setParentId("0");
        }
        dd = dataDicRepository.saveAndFlush(dd);
        return dd;
    }

    @Override
    public DataDictionary getById(String id) {
        return dataDicRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        String sql = "update data_dictionary set is_delete = ? where id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);
    }
}
