package com.taoding.mp.core.company.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.company.dao.CompanyRepository;
import com.taoding.mp.core.company.entity.Company;
import com.taoding.mp.core.company.service.CompanyService;
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

/**
 * @author wuwentan
 * @date 2019/5/7
 */
@Service
public class CompanyServiceImpl extends BaseDAO implements CompanyService {

    @Autowired
    CompanyRepository companyRepository;

    @Override
    public PageVO<Company> getPage(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String name = params.get("name");
        String address = params.get("address");
        String corpId = UserSession.getUserSession().getCorpId();
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from company where corp_id = ? and is_delete = 1 ");
        args.add(corpId);
        if (StringUtils.isNotBlank(name)) {
            sql.append("and name like ? ");
            args.add("%" + name + "%");
        }
        if (StringUtils.isNotBlank(address)) {
            sql.append("and address like ? ");
            args.add("%" + address + "%");
        }
        sql.append("order by create_time desc ");
        PageVO<Company> page = getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(Company.class));
        return page;
    }

    @Override
    public Company save(Company company) {
        if(StringUtils.isBlank(company.getId())){
            company.setId(IdWorker.createId());
            company.setCreateTime(CommonUtils.getStringDate(new Date()));
            company.setCorpId(UserSession.getUserSession().getCorpId());
            company.setUpdateTime(CommonUtils.getStringDate(new Date()));
            company.setIsDelete(Constants.STATUE_NORMAL);
        }else{
            company.setUpdateTime(CommonUtils.getStringDate(new Date()));
            Company source = getById(company.getId());
            UpdateUtils.copyNonNullProperties(source, company);
        }
        company = companyRepository.saveAndFlush(company);
        return company;
    }

    @Override
    public Company getById(String id) {
        return companyRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        String sql = "update company set is_delete = ? where id = ? ";
        jdbc.update(sql, Constants.STATUE_DEL, id);
    }
}
