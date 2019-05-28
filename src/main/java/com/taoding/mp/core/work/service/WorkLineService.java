package com.taoding.mp.core.work.service;

import com.taoding.mp.base.model.PageVO;
import com.taoding.mp.core.flow.entity.FlowTree;
import com.taoding.mp.core.flow.vo.PreviewVO;
import com.taoding.mp.core.project.entity.ProjectInfo;
import com.taoding.mp.core.work.entity.WorkLine;
import com.taoding.mp.core.work.vo.BacklogVO;

import java.util.List;
import java.util.Map;

/**
 * @author youngsapling on 2019-04-15
 */
public interface WorkLineService {
    /**
     * 初始化项目流水线.
     * @param projectId
     * @return
     */
    boolean initWorkLine(String projectId);
    /**
     * 用户点击 提交下一步
     * 1. 先判断当前页面上的这些workLineIds是否都 已处理. boolean isAllDid(String workLineId);通过wli查询到wl, 再通过groups查询其兄弟, 判断是否完成
     *  1.1 不是, 跳过.
     *  1.2 是, 判断是否还有下一个treeId or List.  List<FlowTree> flowTreeService.selectByPId(String pid);(前端必须返回num最大那个)
     *      1.2.1 没有下一个了, 走主流程的下一个treeId    需要一个方法.并且得到主节点后要判断这个主节点能不能创建.-->4.
     *      1.2.2 有下一个, 走2.
     * 2. 判断下一个treeId的前置workLineId是否都确认     boolean isAllDidByFlowTree();通过下一个tree对象获得pid,查询到对应
     *                                              --> List<WorkLine>, 判断这些是否都确认了,使用 isAllDid()方法
     *  2.2.1 不是, 跳过.
     *  2.2.2 是, 可以将下一个转换为节点, 走3.
     * 3. 创建List<WorkLine>, 将本次要插入的wls记录下来.
     *      将下一个treeId(A)转换为workLineId存入wls.
     *  3.1 确认该treeId(A)的下一个treeId(B)是否存在
     *      3.1.1 存在, 判断A 与 B是否是同一个科室
     *          3.1.1.1 不是同一科室, 结束.
     *          3.1.1.2 是同一科室, 将B转换为workLine存入wls.
     *      3.1.2 不存在, 结束.
     *
     * @param projectId
     */
    void toNext(String projectId, String workLineId);

    /**
     * 用户点击完成/跳过。
     * @param workLineId 即id
     * @param result 1 完成 / 2 跳过
     * @param remark
     * @param operatorName
     */
    boolean completeWorkLine(WorkLine workLine);

    /**
     * [待办详情]
     * 通过 项目 和 科室 查询待办List
     * 因为在添加的时候, 会将本次该处理的所有的节点都添加进去.所以查到的结果: 对于同一个project, 会有多个workLine,
     * 他们的groups是一样的.
     * 参数有一个 groups, 如果是从我的已办页面进来, 会传这个Id, 查询的是这个groups下的数据.
     */
    List<WorkLine> getByProjectIdAndDeptId(Map<String, String> map);

    /**
     * [查询待办/已办]
     * 内部区分科室/部门
     * @param status = 1 待办     /    status = 2已办
     * @return
     */
    PageVO<BacklogVO> getBaseInfoFromDeptId(Map<String, String> params);


    /**
     * 通过wli查询到wl, 再通过groups查询其兄弟, 判断是否完成。
     * @param workLine
     * @param condition 判断兄弟节点的时候使用 result, 判断前置节点的时候使用status
     * @return
     */
    boolean isAllDid(WorkLine workLine, String condition);

    /**
     * 判断这个ft的前置节点是否都已经完成。
     * @param flowTree
     * @param condition 判断兄弟节点的时候使用 result, 判断前置节点的时候使用status
     * @return
     */
    boolean isAllDidByFlowTree(String project, FlowTree flowTree, String condition);

    /**
     * [办理进度]
     * @param projectId
     * @param level
     * @param topId  点击的主流程节点的flowTreeId
     * @return
     */
    PreviewVO getPlan(String projectId, Integer level, String flowTreeId);

    /**
     * 通过主流程节点来开启, 这个独立出来, 在流转过程中可以复用.
     * @param flowTreeBase
     * @param projectInfo
     * @param type
     * @return
     */
    boolean startWorkLine(FlowTree flowTreeBase, ProjectInfo projectInfo, Integer type);

    /**
     * 这个方法包容上面那个方法, 重载.
     * @param flowTreeBase
     * @param projectId
     * @param type
     * @return
     */
    boolean startWorkLine(FlowTree flowTreeBase, String projectId, Integer type);
    /**
     * 通过projectId逻辑删除
     * @param projectId
     * @return
     */
    boolean deleteByProjectId(String projectId);

    /**
     * 用户设置某个子流程重置, 会逻辑删除已经操作了的子流程所有节点以及workRecord.
     * @param projectId
     * @param treeTopId 待办详情页面获取到的treeTopId.
     * @return
     */
    boolean resetChildWorkLine(String projectId, String treeTopId);

    /**
     * [推送消息]
     * 将入参保存.然后推送消息.
     * 给入参对应的待办科室推送待办/给项目推送动态
     * @param workLineList
     * @param projectInfo
     */
    void saveAllWithNotify(List<WorkLine> workLineList, ProjectInfo projectInfo, boolean project, boolean backlog);
}
