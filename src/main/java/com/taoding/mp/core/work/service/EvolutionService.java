package com.taoding.mp.core.work.service;

import java.util.Map;

/**
 * @author: youngsapling
 * @date: 2019-04-20
 * @modifyTime:
 * @description: 版本切换使用
 */
public interface EvolutionService {

    /**
     *  版本切换时调用的方法.
     *
     *  需要数据 1.oldFlowModelId 2.flowTreeMap 3.treeIdMap 4.workLineMap 5.FlowWorkFileMap
     *  [
     *  1. flowModelId 用途是从现在的表中直接找到所有的要更新的对象.
     *  2. flowTreeMap<String, FlowTree> 存的是flowTree对象的id 和 该对象的映射.
     *  3. treeIdMap<String, String> 存的是flowTree对象新旧id的映射关系.
     *  4. workLineMap<String, String> 存的是workLine的新旧id的映射关系.
     *  5. FlowWorkFileMap<String, String> 存的是file的新旧id的映射关系.
     *  ]
     *  执行顺序:
     *  1. 将workLine中的数据全部插入到workLineHistory中.
     *  2. 将workLine表清空.
     *  3. 更新内存中的这些workLine, 将旧的id更新为新的.
     *    3.1 需要改的字段: flowModelId, flowTreeId, name, num, unitIds, deptIds, hasFlowWorkFile, treeTopId
     *        需要的数据: 新的flowModelId对应的List<FlowTree>, flowTreeMap.
     *    3.2 将内存中的的List<WorkLine>遍历, 如果旧节点不在新的tree中
     *      3.2.1 判断自身status == 1 ? 如果相等说明待办的节点被删除了, 那么这个环节重新走.
     *      3.2.2 否则, 需要过滤掉, 不再维护.
     *    3.3 维护一个workLineMap, 存储workLine的oldId and newId, 用于更新workRecord的时候使用.
     *  4. 将3中处理好的数据落库.
     *  5. 使用 workLineMap, flowTreeMap, FlowWorkFileMap 将workRecord按3的逻辑处理.
     */
    boolean evolution(String newFlowModelId);

    /**
     * 由用户确定, 项目已执行到哪一步, 待处理的是哪些.
     * @param flowTreeMap -> doing  did  skip
     * @return
     */
    boolean initWorkLine(Map<String, String> flowTreeMap);

    /**
     * 删除 initWorkLine方法添加的这些批量导入的节点, 真删除.
     * 然后修改项目详情表中的result为0, 可以重新操作.
     * @param projectId
     * @return
     */
    boolean deleteWorkLine(String projectId);
}
