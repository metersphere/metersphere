import { cloneDeep, isEqual } from 'lodash-es';

import {
  type ExecuteAssertionConfig,
  ExecuteBody,
  type ExecuteConditionConfig,
  type ResponseDefinition,
} from '@/models/apiTest/common';
import type { MockBody } from '@/models/apiTest/mock';
import { RequestConditionProcessor, RequestParamsType } from '@/enums/apiEnum';

import {
  assertDefaultParamsItem,
  defaultAssertParamsItem,
  defaultBodyParamsItem,
  defaultHeaderParamsItem,
  defaultKeyValueParamItem,
  defaultRequestParamsItem,
  jsonPathDefaultParamItem,
  regexDefaultParamItem,
} from './config';
import type { RequestParam } from './requestComposition/index.vue';

export interface ParseResult {
  uploadFileIds: string[];
  linkFileIds: string[];
  deleteFileIds: string[];
  unLinkFileIds: string[];
}

/**
 * 解析接口请求/Mock body 内的文件列表
 * @param body body 参数对象
 */
export function parseRequestBodyFiles(
  body: ExecuteBody | MockBody,
  response?: ResponseDefinition[],
  saveUploadFileIds?: string[],
  saveLinkFileIds?: string[]
): ParseResult {
  const { binaryBody } = body;
  const uploadFileIds = new Set<string>(); // 存储本地上传的文件 id 集合
  const linkFileIds = new Set<string>(); // 存储关联文件 id 集合
  const tempSaveUploadFileIds = new Set<string>(); // 临时存储 body 内已保存的上传文件 id 集合，用于对比 saveUploadFileIds 以判断有哪些文件被删除
  const tempSaveLinkFileIds = new Set<string>(); // 临时存储 body 内已保存的关联文件 id 集合，用于对比 saveLinkFileIds 以判断有哪些文件被取消关联
  // 获取上传文件和关联文件
  const formValues =
    ((body as ExecuteBody).formDataBody?.formValues || (body as MockBody).formDataBody?.matchRules || []).filter(
      (e) => e
    ) || [];
  for (let i = 0; i < formValues.length; i++) {
    const item = formValues[i];
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
  if (response) {
    response.forEach((res) => {
      if (res.body.binaryBody && res.body.binaryBody.file) {
        const { fileId } = res.body.binaryBody.file;
        if (res.body.binaryBody.file?.local) {
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
    });
  }
  return {
    uploadFileIds: Array.from(uploadFileIds),
    linkFileIds: Array.from(linkFileIds),
    deleteFileIds: saveUploadFileIds?.filter((id) => !tempSaveUploadFileIds.has(id)) || [], // 存储对比已保存的文件后，需要删除的文件 id 集合
    unLinkFileIds: saveLinkFileIds?.filter((id) => !tempSaveLinkFileIds.has(id)) || [], // 存储对比已保存的文件后，需要取消关联的文件 id 集合
  };
}

/**
 * 过滤无效参数
 * @param params 原始参数数组
 * @param defaultParamItem 默认参数项
 * @param filterEnable 是否过滤 enable 为 false 的参数
 */
export function filterKeyValParams<T>(
  params: (T & Record<string, any>)[],
  defaultParamItem: Record<string, any>,
  filterEnable = false
) {
  const lastData = cloneDeep(params[params.length - 1]);
  const defaultParam = cloneDeep(defaultParamItem);
  if (!lastData || !defaultParam) {
    return {
      lastDataIsDefault: true,
      validParams: params,
    };
  }
  // id、enable、valid属性不参与比较
  delete lastData.id;
  delete lastData.valid;
  delete defaultParam.id;
  delete defaultParam.valid;
  if (!filterEnable) {
    delete lastData.enable;
    delete defaultParam.enable;
  }
  const lastDataIsDefault = isEqual(lastData, defaultParam) || lastData.key === '';
  let validParams: (T & Record<string, any>)[];
  if (lastDataIsDefault) {
    // 如果最后一条数据是默认数据，非用户添加更改的，说明是无效参数，删除最后一个
    validParams = params.slice(0, params.length - 1);
  } else {
    validParams = params;
  }
  if (filterEnable) {
    validParams = validParams.filter((e) => e.enable === true);
  }
  return {
    lastDataIsDefault,
    validParams,
  };
}

/**
 * 获取有效的请求表格参数
 * @param requestVModel 请求参数对象
 */
export function getValidRequestTableParams(requestVModel: RequestParam) {
  const { formDataBody, wwwFormBody } = requestVModel.body;
  return {
    formDataBodyTableParams: filterKeyValParams(formDataBody.formValues || [], defaultBodyParamsItem).validParams,
    wwwFormBodyTableParams: filterKeyValParams(wwwFormBody.formValues || [], defaultBodyParamsItem).validParams,
    headers: filterKeyValParams(requestVModel.headers || [], defaultHeaderParamsItem).validParams,
    query: filterKeyValParams(requestVModel.query || [], defaultRequestParamsItem).validParams,
    rest: filterKeyValParams(requestVModel.rest || [], defaultRequestParamsItem).validParams,
    response:
      requestVModel.responseDefinition?.map((e) => ({
        ...e,
        headers: filterKeyValParams(e.headers || [], defaultKeyValueParamItem).validParams,
      })) || [],
  };
}

/**
 * 过滤无效的条件配置参数
 * @param condition 条件配置对象
 */
export function filterConditionsSqlValidParams(condition: ExecuteConditionConfig) {
  const conditionCopy = cloneDeep(condition);
  conditionCopy.processors = conditionCopy.processors.map((processor) => {
    if (processor.processorType === RequestConditionProcessor.SQL) {
      processor.extractParams = filterKeyValParams(processor.extractParams || [], defaultKeyValueParamItem).validParams;
    }
    return processor;
  });
  return conditionCopy;
}

/**
 * 过滤无效的断言配置参数
 * @param assertionConfig 断言配置对象
 * @param isExecute 是否是执行时调用
 */
export function filterAssertions(assertionConfig: ExecuteAssertionConfig, isExecute = false) {
  return assertionConfig.assertions.map((assertItem: any) => {
    return {
      ...assertItem,
      bodyAssertionDataByType: {
        ...assertItem.bodyAssertionDataByType,
        assertions: filterKeyValParams(
          assertItem?.bodyAssertionDataByType?.assertions || [],
          defaultAssertParamsItem,
          isExecute
        ).validParams,
      },
      regexAssertion: {
        ...assertItem?.regexAssertion,
        assertions: filterKeyValParams(assertItem?.regexAssertion?.assertions || [], regexDefaultParamItem, isExecute)
          .validParams,
      },
      xpathAssertion: {
        ...assertItem.xpathAssertion,
        assertions: filterKeyValParams(assertItem?.xpathAssertion?.assertions || [], assertDefaultParamsItem, isExecute)
          .validParams,
      },
      jsonPathAssertion: {
        ...assertItem.jsonPathAssertion,
        assertions: filterKeyValParams(
          assertItem?.jsonPathAssertion?.assertions || [],
          jsonPathDefaultParamItem,
          isExecute
        ).validParams,
      },
    };
  });
}
