package com.taoding.mp.commons;


/**
 * 常量配置清单（按需添加）
 * @author liuxinghong
 */
public class Constants {
    /**
     * 没有分隔符的年月日
     */
    public static final String YYYYMMdd = "YYYYMMdd";
    /**
     * 没有分隔符的年月日时分秒毫秒
     */
    public static final String YYYYMMddHHmmssSSS = "YYYYMMddHHmmssSSS";
    public static final String YYYY_MM_DD_HH_MM_SS = "YYYY-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "YYYY-MM-dd";
    public static final String SPLIT = ",";
    /**
     * 文件路径分隔符，windows
     */
    public static final String PATH_SEPARATOR_WIN = "\\";
    /**
     * 文件路径分隔符 linux
     */
    public static final String PATH_SEPARATOR_LINUX = "/";
    /**
     * 状态正常
     */
    public static final Integer STATUE_NORMAL = 1;
    /**
     * 状态删除
     */
    public static final Integer STATUE_DEL = 0;


    /**
     * 顶级父节点
     */
    public static final Integer TOP_PARENT = 0;
    /**
     * 顶级父节点
     */
    public static final String STRING_TOP_PARENT = "0";

    /**
     * 默认企业平台账号
     */
    public  static final String CORP_ID = "taoding";

    /**
     * 项目状态-前期
     */
    public static final Integer PROJECT_STATUS_PRO = 0;

    /**
     * 项目状态-在建
     */
    public static final Integer PROJECT_STATUS_TODO = 1;

    /**
     * 项目类型-划拨
     */
    public static final Integer PROJECT_TYPE_ALLOT = 1;

    /**
     * 项目类型-出让
     */
    public static final Integer PROJECT_TYPE_GRANT = 2;

    /**
     * 项目类型-其他
     */
    public static final Integer PROJECT_TYPE_OTHER = 3;

   // 是否有申报材料(1.有，0没有)

    public static final Integer FLOW_FILE_OK = 1;

    public static final Integer FLOW_FILE_NO = 0;

    /**
     * 0待处理, 1处理中, 2已处理
     */
    public static final Integer WORKLINE_STATUS_WAITDO = 0;
    public static final Integer WORKLINE_STATUS_DOING = 1;
    public static final Integer WORKLINE_STATUS_DID = 2;
    /**
     * 记录状态: 成功
     */
    public static final Integer SUCCESS = 1;

    /**
     * 记录状态: 失敗
     */
    public static final Integer FAILURE = 0;

    //该版本是否生效 1.生效 ，0 未生效
    public static final Integer VERSION_EFFECT_OK = 1;

    public static final Integer VERSION_EFFECT_NO = 0;
    //流程级别level 0 主流程  1 子流程
    public static final Integer FLOW_NODE_MAIN_LEVEL = 0;

    public static final Integer FLOW_NODE_SUB_LEVEL  = 1;

    /**
     * 用户身份标识：区级领导
     */
    public static final String USER_FLAG_DISTRICT = "district";

    /**
     * 用户身份标识：部门领导、局领导
     */
    public static final String USER_FLAG_DEPT = "dept";

    /**
     * 用户身份标识：办事员
     */
    public static final String USER_FLAG_STAFF = "staff";

    /**
     * 用户身份标识：企业
     */
    public static final String USER_FLAG_COMPANY = "company";

    /**
     * button标识 1 只能看
     */
    public static final String BUTTON_READ = "1";
    /**
     * button标识 2 可以修改
     */
    public static final String BUTTON_WRITE = "2";

    /**
     * 是否为最新的版本（1,true，2,false）
     */
    public static final Integer VERSION_ISRELASR_TRUE = 1;
    public static final Integer VERSION_ISRELASR_FALSE= 0;

    /**
     * 项目隶属关系-区级
     */
    public static final Integer PROJECT_GRADE_DISTRICT = 3;

    /**
     * 项目隶属关系-市级
     */
    public static final Integer PROJECT_GRADE_CITY = 2;

    /**
     * 项目隶属关系-省级
     */
    public static final Integer PROJECT_GRADE_PROVINCE = 1;

    /**
     * 项目隶属关系-无
     */
    public static final Integer PROJECT_GRADE_NONE = 0;

    /**
     * 处理单位----科室处理
     */
    public static final Integer TODO_KESHI = 0;

    /**
     * 处理单位----街办处理(需要去项目详情表查询真实处理单位streetOffice)
     */
    public static final Integer TODO_JIEBAN = 1;

    /**
     * 处理单位----责任科室处理(需要去项目详情表查询真实处理单位responsibleDept)
     */
    public static final Integer TODO_ZERENKESHI = 2;

    /**
     * 项目审批状态：0未审批
     */
    public static final Integer PROJECT_RESULT_NONE = 0;

    /**
     * 项目审批状态：1审批中
     */
    public static final Integer PROJECT_RESULT_DOING = 1;

    /**
     * 项目审批状态：2审批结束
     */
    public static final Integer PROJECT_RESULT_OVER = 2;
    /**
     * 节点未处理(结果的未处理就是处理中)
     */
    public static final Integer WORKLINE_RESULT_DOING = 0;
    /**
     * 节点已确认
     */
    public static final Integer WORKLINE_RESULT_DID = 1;
    /**
     * 节点跳过
     */
    public static final Integer WORKLINE_RESULT_SKIP = 2;
    /**
     * 标识department 的 parentId = 0的情况
     */
    public static final String DEPARTMENT_PARENTID_NOTEXIST = "0";

    /**
     * 标识workLine没有创建
     */
    public static final Integer NOT_EXIST_WORKLINE = 4;

    /**
     * 0 正常工作日（正常上班时间），1.周末节假日（周日，周天） 2，法定节假日（各种小长假）
     */
    public static final Integer DAY_WORK = 0;
    public static final Integer DAY_WEEKEND = 1;
    public static final Integer DAY_HOLIDAY = 2;


    /**
     * 消息分类：1,app消息  2,web端消息
     */
    public static final String MSG_TYPE_APP = "1";
    public static final String MSG_TYPE_WEB = "2";

    /**
     * IOS极光推送设置：如果有，则代表为生产环境
     */
    public static final String IOS_PRODUCTION = "ios_production";

    public static final String PROJECT_TITLE = "您的项目办理进度有了新的进展.";
    public static final String BACKLOG_TITLE = "您有新的待办任务,请及时办理.";

    /**
     * 标识某节点是否已逾期, 0未逾期, 1已逾期
     */
    public static final Integer IS_OVERDUE = 1;
    public static final Integer NOT_OVERDUE = 0;
    /**
     * 消息点击后跳转到项目详情
     */
    public static final String MSG_TO_PROJECT = "project";
    /**
     * 消息点击后跳转到待办列表
     */
    public static final String MSG_TO_BACKLOG = "backlog";

    public static final String DATA_TYPE = "模板类型";

    /**
     * 某个机构的parentId = 0 说明其自身就是父机构.
     */
    public static final String DEPT_PARENT_ID = "0";

    public static final String JIEBAN_IS_NULL = "该项目街办不能为null.";
    public static final String ZERENKESHI_IS_NULL = "该项目责任科室不能为null.";
    public static final String KESHI_NAME_IS_NULL = "科室id[%s]的科室名为null";
}
