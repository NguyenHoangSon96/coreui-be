package com.sonnh.coreuibe.configs;

public class Constant {

    public static final String PATH_TEMP = "src/main/resources/temp/";


    public static final String BASE_URL = "https://training.pricefx.eu/pricefx";
    public static final String COMPANY_NODE = "training";
    public static final String USER_NAME = "admin";
    public static final String PARTITION_RM = "sce-0096";
    public static final String PARTITION_CE = "ce-0262";
    public static final String PASSWORD_RM = "W9Ma8UDZTDc5";
    public static final String PASSWORD_CE = "SXK6HYZN3Lc7";
    public static final Integer MAX_ROWS = 2000;

    public static final String POST_FETCH_LIST_OBJECT = "/fetch/";
    public static final String POST_FETCH_ALL_LOOKUP_TABLE = "/lookuptablemanager.fetch/";
    public static final String POST_FETCH_LOOKUP_TABLE_VALUES = "/lookuptablemanager.fetch/";
    public static final String POST_FETCH_PRODUCT = "/productmanager.quicksearch";
    public static final String POST_FETCH_LIST_PRODUCT = "/productmanager.fetchformulafilteredproducts";
    public static final String POST_FETCH_LIST_PRODUCT_EXTENDSION = "/productmanager.fetch/*/PX";

    public static final String REDIS_LOOKUP_TABLE_META = "LookupTableMeta";
}
