import { ExecuteBody } from '@/models/apiTest/debug';
import { RequestParamsType } from '@/enums/apiEnum';

export default {};

export interface ParseResult {
  uploadFileIds: string[];
  linkFileIds: string[];
  deleteFileIds: string[];
  unLinkFileIds: string[];
}

/**
 * 解析接口请求 body 内的文件列表
 * @param body body 参数对象
 */
export function parseRequestBodyFiles(
  body: ExecuteBody,
  saveUploadFileIds?: string[],
  saveLinkFileIds?: string[]
): ParseResult {
  const { formDataBody, binaryBody } = body;
  const uploadFileIds = new Set<string>(); // 存储本地上传的文件 id 集合
  const linkFileIds = new Set<string>(); // 存储关联文件 id 集合
  const tempSaveUploadFileIds = new Set<string>(); // 临时存储 body 内已保存的上传文件 id 集合，用于对比 saveUploadFileIds 以判断有哪些文件被删除
  const tempSaveLinkFileIds = new Set<string>(); // 临时存储 body 内已保存的关联文件 id 集合，用于对比 saveLinkFileIds 以判断有哪些文件被取消关联
  // 获取上传文件和关联文件
  for (let i = 0; i < formDataBody.formValues.length; i++) {
    const item = formDataBody.formValues[i];
    if (item.paramType === RequestParamsType.FILE) {
      if (item.files) {
        for (let j = 0; j < item.files.length; j++) {
          const file = item.files[j];
          if (file.local) {
            // 本地上传的文件
            if (saveUploadFileIds) {
              // 如果有已保存的上传文件id集合
              if (saveUploadFileIds.includes(file.fileId)) {
                // 当前文件是已保存的文件，存入 tempSaveUploadFileIds
                tempSaveUploadFileIds.add(file.fileId);
              } else {
                // 当前文件不是已保存的文件，存入 uploadFileIds
                uploadFileIds.add(file.fileId);
              }
            } else {
              // 没有已保存的文件id集合，直接存入 uploadFileIds
              uploadFileIds.add(file.fileId);
            }
          } else if (saveLinkFileIds) {
            // 如果有已保存的关联文件id集合
            if (saveLinkFileIds.includes(file.fileId)) {
              // 当前文件是已保存的文件，存入
              tempSaveLinkFileIds.add(file.fileId);
            } else {
              // 当前文件不是已保存的文件，存入 uploadFileIds
              linkFileIds.add(file.fileId);
            }
          } else {
            // 关联的文件
            linkFileIds.add(file.fileId);
          }
        }
      }
    }
  }
  if (binaryBody && binaryBody.file) {
    const { fileId } = binaryBody.file;
    if (binaryBody.file?.local) {
      if (saveUploadFileIds) {
        // 如果有已保存的上传文件id集合
        if (saveUploadFileIds.includes(fileId)) {
          // 当前文件是已保存的文件，存入 tempSaveUploadFileIds
          tempSaveUploadFileIds.add(fileId);
        } else {
          // 当前文件不是已保存的文件，存入 uploadFileIds
          uploadFileIds.add(fileId);
        }
      } else {
        // 没有已保存的文件id集合，直接存入 uploadFileIds
        uploadFileIds.add(fileId);
      }
    } else if (saveLinkFileIds) {
      // 如果有已保存的关联文件id集合
      if (saveLinkFileIds.includes(fileId)) {
        // 当前文件是已保存的文件，存入
        tempSaveLinkFileIds.add(fileId);
      } else {
        // 当前文件不是已保存的文件，存入 uploadFileIds
        linkFileIds.add(fileId);
      }
    } else {
      // 关联的文件
      linkFileIds.add(fileId);
    }
  }
  return {
    uploadFileIds: Array.from(uploadFileIds),
    linkFileIds: Array.from(linkFileIds),
    deleteFileIds: saveUploadFileIds?.filter((id) => !tempSaveUploadFileIds.has(id)) || [], // 存储对比已保存的文件后，需要删除的文件 id 集合
    unLinkFileIds: saveLinkFileIds?.filter((id) => !tempSaveLinkFileIds.has(id)) || [], // 存储对比已保存的文件后，需要取消关联的文件 id 集合
  };
}
