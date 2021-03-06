package com.shareyi.molicode.common.constants;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * 魔力code 常量
 *
 * @author zhangshibin
 * @date 2018/10/3
 */
public class MoliCodeConstant {

    /**
     * 表模型数据
     */
    public static final String CTX_KEY_TABLE_MODEL = "tableModel";
    /**
     * autoCode.xml的解析模型
     */
    public static final String CTX_KEY_AUTO_MAKE = "autoMake";

    /**
     * 原始数据
     */
    public static String CTX_KEY_SRC_CONTENT = "srcContent";

    /**
     * 处理后的数据 data
     */
    public static String CTX_KEY_DEF_DATA = "data";


    /**
     * 参数key 是否trim
     */
    public static String PARAM_KEY_TRIM_TYPE = "trimType";







    /**
     * 创建时间
     */
    public final static String BIZ_FIELDS_KEY_CREATETIME = "createTime";
    /**
     * 修改时间
     */
    public final static String BIZ_FIELDS_KEY_UPDATETIME = "updateTime";
    /**
     * 查询列表
     */
    public final static String BIZ_FIELDS_KEY_QUERYLIST = "queryList";
    /**
     * 新增列表
     */
    public final static String BIZ_FIELDS_KEY_ADDLIST = "addList";
    /**
     * 修改列表
     */
    public final static String BIZ_FIELDS_KEY_UPDATELIST = "updateList";
    /**
     * 查看列表
     */
    public final static String BIZ_FIELDS_KEY_VIEWLIST = "viewList";
    /**
     * 查询参数列表
     */
    public final static String BIZ_FIELDS_KEY_SEARCHKEY = "searchKey";
    /**
     * 全部字段
     */
    public final static String BIZ_FIELDS_KEY_ALLCOLUMN = "allColumn";
    /**
     * 业务字段keyList
     */
    public static List<String> BIZ_FIELDS_KEY_LIST = Lists.newArrayList(BIZ_FIELDS_KEY_CREATETIME,
            BIZ_FIELDS_KEY_UPDATETIME, BIZ_FIELDS_KEY_QUERYLIST, BIZ_FIELDS_KEY_ADDLIST,
            BIZ_FIELDS_KEY_UPDATELIST, BIZ_FIELDS_KEY_VIEWLIST, BIZ_FIELDS_KEY_SEARCHKEY,
            BIZ_FIELDS_KEY_ALLCOLUMN);


    public static Map<String, String> BIZ_FIELDS_DEF_EXP_MAP = Maps.newLinkedHashMap();

    static {
        /**
         * 创建时间
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_CREATETIME, "gmtCreate,created");
        /**
         * 修改时间
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_UPDATETIME, "gmtModify,modified");
        /**
         * 查询列表
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_QUERYLIST, "^gmtCreate,created,*note,*remark");
        /**
         * 新增列表
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_ADDLIST, "^gmtCreate,created,gmtModify,modified");
        /**
         * 修改列表
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_UPDATELIST, "^gmtCreate,created,gmtModify,modified");
        /**
         * 查看列表
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_VIEWLIST, "");
        /**
         * 查询参数列表
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_SEARCHKEY, "*name,*state,*status,*type,*key");
        /**
         * 全部字段
         */
        BIZ_FIELDS_DEF_EXP_MAP.put(BIZ_FIELDS_KEY_ALLCOLUMN, "");


    }


}
