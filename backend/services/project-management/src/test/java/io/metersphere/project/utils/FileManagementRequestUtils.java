package io.metersphere.project.utils;

public class FileManagementRequestUtils {
    //文件模块树查询
    public static final String URL_MODULE_TREE = "/project/file-module/tree/%s";
    //添加文件模块
    public static final String URL_MODULE_ADD = "/project/file-module/add";
    //修改文件模块
    public static final String URL_MODULE_UPDATE = "/project/file-module/update";
    //删除文件模块
    public static final String URL_MODULE_DELETE = "/project/file-module/delete/%s";
    //移动文件模块
    public static final String URL_MODULE_MOVE = "/project/file-module/move";

    //文件上传
    public static final String URL_FILE_UPLOAD = "/project/file/upload";
    //获取文件类型
    public static final String URL_FILE_TYPE = "/project/file/type/%s";
    //获取文件
    public static final String URL_FILE = "/project/file/get/%s";
    //文件列表查询
    public static final String URL_FILE_PAGE = "/project/file/page";
    //文件列表查询对应的模块统计
    public static final String URL_FILE_MODULE_COUNT = "/project/file/module/count";
    //文件重传
    public static final String URL_FILE_RE_UPLOAD = "/project/file/re-upload";
    //文件下载（权限判断需要提前上传文件，所以放在了主测试类里）
    public static final String URL_FILE_DOWNLOAD = "/project/file/download/%s";
    //文件预览缩略图下载
    public static final String URL_FILE_PREVIEW_COMPRESSED = "/file/preview/compressed/%s/%s";
    //文件预览原图下载
    public static final String URL_FILE_PREVIEW_ORIGINAL = "/file/preview/original/%s/%s";
    //文件批量下载 （权限判断需要提前上传文件，所以放在了主测试类里）
    public static final String URL_FILE_BATCH_DOWNLOAD = "/project/file/batch-download";
    //启用/禁用jar文件
    public static final String URL_CHANGE_JAR_ENABLE = "/project/file/jar-file-status/%s/%s";
    //文件批量删除
    public static final String URL_FILE_DELETE = "/project/file/delete";
    //文件信息修改
    public static final String URL_FILE_UPDATE = "/project/file/update";
    //文件批量移动（权限判断需要提前上传文件，所以放在了主测试类里）
    public static final String URL_FILE_BATCH_UPDATE = "/project/file/batch-move";
    //文件批量移动（权限判断需要提前上传文件，所以放在了主测试类里）
    public static final String URL_FILE_VERSION = "/project/file/file-version/%s";

    /*
    存储库相关路径
     */
    //存储库列表
    public static final String URL_FILE_REPOSITORY_LIST = "/project/file/repository/list/%s";
    //存储库详情
    public static final String URL_FILE_REPOSITORY_INFO = "/project/file/repository/info/%s";
    //存储库文件类型
    public static final String URL_FILE_REPOSITORY_FILE_TYPE = "/project/file/repository/file-type/%s";
    //测试连接存储库
    public static final String URL_FILE_REPOSITORY_CONNECT = "/project/file/repository/connect";
    //添加存储库
    public static final String URL_FILE_REPOSITORY_CREATE = "/project/file/repository/add-repository";
    //修改存储库
    public static final String URL_FILE_REPOSITORY_UPDATE = "/project/file/repository/update-repository";
    //添加文件
    public static final String URL_FILE_REPOSITORY_FILE_ADD = "/project/file/repository/add-file";
    public static final String URL_FILE_REPOSITORY_FILE_PULL = "/project/file/repository/pull-file/%s";

    /*
    文件关联
     */
    public static final String URL_FILE_ASSOCIATION_LIST = "/project/file/association/list/%s";
    public static final String URL_FILE_ASSOCIATION_UPGRADE = "/project/file/association/upgrade/%s/%s";
    public static final String URL_FILE_ASSOCIATION_DELETE = "/project/file/association/delete";


}
