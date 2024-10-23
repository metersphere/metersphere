import { UploadAcceptEnum, UploadStatus } from '@/enums/uploadEnum';

import type { FileItem } from '@arco-design/web-vue';

// 上传类型
export type UploadType = keyof typeof UploadAcceptEnum;

// MS文件类型
export type MsFileItem = FileItem & {
  status?: keyof typeof UploadStatus;
  enable?: boolean; // jar类型文件是否可用
  uploadedTime?: string | number; // 上传完成时间
  errMsg?: string; // 上传失败的错误信息
  delete?: boolean; // 是否删除
  [key: string]: any;
};
