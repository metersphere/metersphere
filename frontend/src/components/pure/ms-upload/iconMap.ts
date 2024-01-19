import { UploadAcceptEnum, UploadStatus } from '@/enums/uploadEnum';

import type { MsFileItem } from './types';

// 使用映射类型和索引签名
export type FileIconMapping = {
  [key in keyof typeof UploadAcceptEnum]: any;
};

export const FileIconMap: FileIconMapping = {
  csv: {
    [UploadStatus.init]: 'icon-icon_file-CSV_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-CSV_colorful1',
  },
  excel: {
    [UploadStatus.init]: 'icon-icon_file-excel_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-excel_colorful1',
  },
  image: {
    [UploadStatus.init]: 'icon-icon_file-image_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-image_colorful1',
  },
  jar: {
    [UploadStatus.init]: 'icon-a-icon_file-jar_colorful_ash',
    [UploadStatus.done]: 'icon-a-icon_file-jar_colorful',
  },
  pdf: {
    [UploadStatus.init]: 'icon-icon_file-pdf_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-pdf_colorful1',
  },
  sql: {
    [UploadStatus.init]: 'icon-icon_file-sql_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-sql_colorful1',
  },
  plain: {
    [UploadStatus.init]: 'icon-icon_file-text_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-text_colorful1',
  },
  txt: {
    [UploadStatus.init]: 'icon-icon_file-text_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-text_colorful1',
  },
  word: {
    [UploadStatus.init]: 'icon-icon_file-word_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-word_colorful1',
  },
  video: {
    [UploadStatus.init]: 'icon-icon_file-vedio_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-vedio_colorful1',
  },
  xmind: {
    [UploadStatus.init]: 'icon-icon_file-xmind_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-xmind_colorful1',
  },
  zip: {
    [UploadStatus.init]: 'icon-a-icon_file-compressed_colorful_ash2',
    [UploadStatus.done]: 'icon-a-icon_file-compressed_colorful2',
  },
  sketch: {
    [UploadStatus.init]: 'icon-icon_file-sketch_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-sketch_colorful1',
  },
  ppt: {
    [UploadStatus.init]: 'icon-icon_file-ppt_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-ppt_colorful1',
  },
  unknown: {
    [UploadStatus.init]: 'icon-icon_file-unknow_colorful1',
    [UploadStatus.done]: 'icon-icon_file-unknow_colorful1',
  },
  none: {
    [UploadStatus.init]: 'icon-icon_file-unknow_colorful1',
    [UploadStatus.done]: 'icon-icon_file-unknow_colorful1',
  },
  json: {
    [UploadStatus.init]: 'icon-icon_file-json_colorful_ash',
    [UploadStatus.done]: 'icon-icon_file-json_colorful1',
  },
};

/**
 * 获取文件类型枚举
 * @param fileType 文件类型
 */
export function getFileEnum(fileType?: string): keyof typeof UploadAcceptEnum {
  if (fileType) {
    const keys = Object.keys(UploadAcceptEnum);
    for (let i = 0; i < keys.length; i++) {
      const key = keys[i] as unknown as keyof typeof UploadAcceptEnum;
      if (UploadAcceptEnum[key].split(',').includes(`.${fileType.split('/')[1]}`)) {
        return key;
      }
    }
  }
  return 'unknown' as keyof typeof UploadAcceptEnum;
}

/**
 * 获取文件图标
 * @param item 文件项
 */
export function getFileIcon(item: MsFileItem) {
  if (item.status === UploadStatus.done) {
    return FileIconMap[getFileEnum(item.file?.type)]?.[item.status] ?? FileIconMap.unknown[UploadStatus.done];
  }
  return FileIconMap[getFileEnum(item.file?.type)]?.[UploadStatus.init] ?? FileIconMap.unknown[UploadStatus.done];
}
