package com.taoding.mp.core.datetime.dao;

import com.taoding.mp.core.datetime.entity.Kalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/7 000719:20
 */
public interface KalendarRepository extends JpaRepository<Kalendar, String> {

    List<Kalendar> findByYearAndIsDelete(Integer year,Integer isDelete);
    Boolean existsByYearAndIsDelete(Integer year,Integer isDelete);
    List<Kalendar> findByTypeAndIsDelete(Integer type,Integer isDelete);

}
