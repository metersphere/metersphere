/** *
 * 缺陷管理
 * @description 将文件信息转换为文件格式
 * @param {stafileInfotus} 文件file
 */

import { FormRuleItem } from '@/components/pure/ms-form-create/types';
import { getFileEnum } from '@/components/pure/ms-upload/iconMap';
import { MsFileItem } from '@/components/pure/ms-upload/types';

import { findParents, Option } from '@/utils/recursion';

import { BugEditCustomFieldItem } from '@/models/bug-management';
import { AssociatedList } from '@/models/caseManagement/featureCase';

export function convertToFileByBug(fileInfo: AssociatedList): MsFileItem {
  const gatewayAddress = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
  const fileName = `${fileInfo.name}`;
  const fileFormatMatch = fileName.match(/\.([a-zA-Z0-9]+)$/);
  const fileFormatType = fileFormatMatch ? fileFormatMatch[1] : 'none';
  const type = getFileEnum(fileFormatType);
  const file = new File([new Blob()], `${fileName}`, {
    type: `application/${type}`,
  });
  Object.defineProperty(file, 'size', { value: fileInfo.fileSize });
  Object.defineProperty(file, 'type', { value: type });
  const { fileId, local, isUpdateFlag, isCopyFlag, refId, createUserName, createTime } = fileInfo;
  return {
    enable: fileInfo.enable || false,
    file,
    name: fileName,
    percent: 0,
    status: 'done',
    uid: fileId,
    url: `${gatewayAddress}/${fileInfo.filePath || ''}`,
    local,
    deleteContent: !local ? 'caseManagement.featureCase.cancelLink' : '',
    isUpdateFlag,
    isCopyFlag,
    associateId: refId,
    createUserName,
    createTime,
    uploadedTime: createTime,
  };
}
/** *
 *
 * @description 将文件信息转换为文件格式
 * @param {stafileInfotus} 文件file
 */

export function convertToFileByDetail(fileInfo: AssociatedList): MsFileItem {
  const gatewayAddress = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
  const fileName = fileInfo.fileType ? `${fileInfo.name}.${fileInfo.fileType || ''}` : `${fileInfo.name}`;
  const type = fileName.split('.')[1];
  const file = new File([new Blob()], `${fileName}`, {
    type: `application/${type}`,
  });
  Object.defineProperty(file, 'size', { value: fileInfo.size });
  const { id, local, isUpdateFlag, associateId } = fileInfo;
  return {
    enable: fileInfo.enable || false,
    file,
    name: fileName,
    percent: 0,
    status: 'done',
    uid: id,
    url: `${gatewayAddress}/${fileInfo.filePath || ''}`,
    local,
    deleteContent: local ? '' : 'caseManagement.featureCase.cancelLink',
    isUpdateFlag,
    associateId,
  };
}

export function makeCustomFieldsParams(formItem: FormRuleItem[]) {
  const customFields: BugEditCustomFieldItem[] = [];
  if (formItem && formItem.length) {
    formItem.forEach((item: FormRuleItem) => {
      let itemVal = item.value;
      if (item.sourceType === 'CASCADER') {
        itemVal = findParents(item.options as Option[], item.value as string, []) || '';
      }
      customFields.push({
        id: item.field as string,
        name: item.title as string,
        type: item.sourceType as string,
        value: Array.isArray(itemVal) ? JSON.stringify(itemVal) : (itemVal as string),
      });
    });
  }
  return customFields;
}
