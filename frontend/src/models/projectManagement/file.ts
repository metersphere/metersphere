import { BatchApiParams, TableQueryParams } from '@/models/common';

// 文件列表查询参数
export interface FileListQueryParams extends TableQueryParams {
  moduleIds: string[];
  fileType: string;
  projectId: string;
}
// 模块统计文件数量
export interface ModuleCount {
  [key: string]: number;
}
// 文件列表项
export interface FileItem {
  id: string;
  name: string;
  fileType: string; // 文件类型
  tags: string[]; // 标签
  description: string;
  updateUser: string;
  updateTime: number;
  previewSrc: string; // 预览地址
  size: number;
  enable: boolean; // jar文件启用禁用
}
// 文件详情
export interface FileDetail extends FileItem {
  projectId: string;
  moduleName: string; // 所属模块名
  moduleId: string;
  storage?: string; // 存储方式
  createUser: string;
  createTime: number;
}
// 上传文件参数
export interface UploadFileParams {
  request: {
    projectId: string;
    moduleId: string; // 模块ID
  };
  file: File;
}
// 更新文件参数
export interface UpdateFileParams {
  id: string;
  name?: string;
  tags?: (string | number)[];
  description?: string;
  moduleId?: string;
}
// 重新上传文件参数
export interface ReuploadFileParams {
  request: {
    fileId: string;
  };
  file: File;
}
// 批量操作文件参数
export interface BatchFileApiParams extends BatchApiParams {
  projectId: string;
  fileType: string;
  moduleIds: string[];
  moveModuleId?: string | number; // 移动的模块ID
}
// 更新模块参数
export interface UpdateModuleParams {
  id: string;
  name: string;
}
// 移动模块参数
export interface MoveModuleParams {
  dragNodeId: string; // 拖拽的节点ID
  dropNodeId: string; // 拖拽的节点释放的节点ID
  dropPosition: number; // 拖拽的节点释放的位置,-1 为前面，1为后面，0为内部
}
// 添加模块参数
export interface AddModuleParams {
  projectId: string;
  name: string;
  parentId: string;
}
// 模块树节点
export interface ModuleTreeNode {
  id: string;
  name: string;
  type: string;
  children: ModuleTreeNode[];
}
