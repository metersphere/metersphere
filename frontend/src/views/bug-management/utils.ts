/** *
 * 缺陷管理
 * @description 将文件信息转换为文件格式
 * @param {stafileInfotus} 文件file
 */

import { MsFileItem } from '@/components/pure/ms-upload/types';

import { AssociatedList } from '@/models/caseManagement/featureCase';

export function convertToFileByBug(fileInfo: AssociatedList): MsFileItem {
  const gatewayAddress = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
  const fileName = `${fileInfo.name}`;
  const type = `${fileInfo.fileType}`;
  const file = new File([new Blob()], `${fileName}`, {
    type: `application/${type}`,
  });
  Object.defineProperty(file, 'size', { value: fileInfo.fileSize });
  const { fileId, local, isUpdateFlag, refId, createUserName, createTime } = fileInfo;
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
    associateId: refId,
    createUserName,
    uploadedTime: createTime
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
