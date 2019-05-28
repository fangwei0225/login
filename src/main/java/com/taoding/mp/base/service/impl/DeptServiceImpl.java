package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.DeptRepository;
import com.taoding.mp.base.entity.Department;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.DeptService;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.util.CommonUtils;
import com.taoding.mp.util.IdWorker;
import com.taoding.mp.util.UpdateUtils;
import org.apache.commons.collections4.CollectionUtils;
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
 * @author wuwentan
 * @date 2019/4/11
 */
@Service
public class DeptServiceImpl extends BaseDAO implements DeptService {

    @Autowired
    DeptRepository deptRepository;

    @Override
    public List<Department> listAll() {
        String corpId = UserSession.getUserSession().getCorpId();
        //根据序号倒序查询所有部门
        List<Department> list = deptRepository.findAllByCorpIdOrderByNumDesc(corpId);
        list = makeTreeList(list,"0");

        //组装根节点部门
        List<Department> treeList = new ArrayList<>();
        Department root = new Department();
        root.setId("0");
        root.setName("长安区政府");
        root.setChildren(list);
        treeList.add(root);
        return treeList;
    }

    /**
     * 递归部门树形结构
     * @param departmentList
     * @param pid
     * @return
     */
    private List<Department> makeTreeList(List<Department> departmentList, String pid){
        //直属下级节点
        List<Department> children = departmentList.stream().filter(x -> x.getParentId().equals(pid)).collect(Collectors.toList());
        //非直属节点
        List<Department> peer = departmentList.stream().filter(x -> !x.getParentId().equals(pid)).collect(Collectors.toList());
        children.forEach(d -> makeTreeList(peer, d.getId()).forEach(p -> d.getChildren().add(p)));
        return children;
    }

    @Override
    public PageVO<Department> getPage(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String parentId = params.get("parentId");
        String content = params.get("content");
        String corpId = UserSession.getUserSession().getCorpId();

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from department where corp_id = ? and is_delete = 1 ");
        args.add(corpId);

        String rootId = "0";
        if (StringUtils.isNotBlank(parentId) && !rootId.equals(parentId)) {
            sql.append("and parent_id = ? ");
            args.add(parentId);
        }
        if (StringUtils.isNotBlank(content)) {
            sql.append("and (name like ? or code like ?) ");
            args.add("%" + content + "%");
            args.add("%" + content + "%");
        }
        sql.append("order by num desc,create_time ");
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(Department.class));
    }

    @Override
    public Department save(Department dept) {
        if (StringUtils.isBlank(dept.getId())) {
            dept.setId(IdWorker.createId());
            dept.setCreateTime(CommonUtils.getStringDate(new Date()));
            dept.setIsDelete(Constants.STATUE_NORMAL);
            dept.setCorpId(UserSession.getUserSession().getCorpId());
            dept.setNum(dept.getNum() == null ? 0 : dept.getNum());
        } else {
            Department source = deptRepository.findById(dept.getId()).get();
            dept.setUpdateTime(CommonUtils.getStringDate(new Date()));
            UpdateUtils.copyNonNullProperties(source, dept);
        }

        //根据父节点设置当前节点path信息
        if(StringUtils.isNotBlank(dept.getParentId()) && !Constants.STRING_TOP_PARENT.equals(dept.getParentId())){
            Department getParent = deptRepository.findById(dept.getParentId()).get();
            dept.setPathIds(getParent.getPathIds()+","+dept.getId());
            dept.setPathNames(getParent.getPathNames()+","+dept.getName());
            dept.setLevel(1);
        }else{
            dept.setPathIds(dept.getId());
            dept.setParentId(Constants.STRING_TOP_PARENT);
            dept.setPathNames(dept.getName());
            dept.setLevel(0);
        }

        dept = deptRepository.saveAndFlush(dept);
        return dept;
    }

    @Override
    public Department getById(String id) {
        return deptRepository.findById(id).orElse(new Department());
    }

    @Override
    public void deleteById(String id) {
        String sql = "update department set is_delete = ? where id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);
    }

    @Override
    public List<Department> listByPid(String parentId) {
        String corpId = UserSession.getUserSession().getCorpId();
        String sql = "select * from department where corp_id = ? and is_delete = 1 and parent_id = ? order by num desc,create_time ";
        return jdbc.query(sql,new BeanPropertyRowMapper<>(Department.class),corpId,parentId);
    }

    @Override
    public String getDeptNameByIds(String ids) {
        String sql = "select GROUP_CONCAT(`name`) 'name' from department where FIND_IN_SET(id,?) ";
        return jdbc.queryForObject(sql,String.class,ids);
    }

    @Override
    public String getDeptNames(String id) {
        String sql = "select IFNULL(REPLACE(GROUP_CONCAT(`name`),',','-'),'') 'name' from department "
                + "where FIND_IN_SET(id,(select path_ids from department where id = ?))";
        return jdbc.queryForObject(sql,String.class,id);
    }

    @Override
    public String getParentId(String id) {
        String sql = "select parent_id from department where id = ? ";
        List<String> list = jdbc.queryForList(sql,String.class,id);
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : "";
    }
}
