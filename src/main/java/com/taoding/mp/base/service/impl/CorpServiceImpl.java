package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.base.dao.CorpRepository;
import com.taoding.mp.base.entity.Corp;
import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.CorpService;
import com.taoding.mp.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wuwentan
 * @date 2018/11/7
 */
@Service
public class CorpServiceImpl extends BaseDAO implements CorpService {

    @Autowired
    CorpRepository corpRepository;

    @Override
    public PageVO<Corp> getPage(Map<String, String> params) {
        int pageNo = params.get("pageNo") == null ? 1 : Integer.parseInt(params.get("pageNo"));
        int pageSize = params.get("pageSize") == null ? 15 : Integer.parseInt(params.get("pageSize"));
        String corpId = params.get("corpId");
        String name = params.get("name");
        String status = params.get("status");

        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from corp where 1=1 ");
        if (StringUtils.isNotBlank(corpId)) {
            sql.append("and corp_id like ? ");
            args.add("%" + corpId + "%");
        }
        if (StringUtils.isNotBlank(name)) {
            sql.append("and name like ? ");
            args.add("%" + name + "%");
        }
        if (StringUtils.isNotBlank(status)) {
            sql.append("and status = ? ");
            args.add(status);
        }
        return getPage(sql.toString(), pageNo, pageSize, args, new BeanPropertyRowMapper(Corp.class));
    }

    @Override
    public List<Corp> getList(Map<String, String> params) {
        params.put("pageSize", "-1");
        return getPage(params).getItems();
    }

    @Override
    public Corp save(Corp corp) {
        corp.setCorpId(CommonUtils.getUUID());
        return corpRepository.save(corp);
    }

    @Override
    public Corp getById(String id) {
        return corpRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        Corp corp = getById(id);
        if (corp != null) {
            corp.setStatus(0);
            corp.setOperator(UserSession.getUserSession().getUserId());
            save(corp);
        }
    }
}
