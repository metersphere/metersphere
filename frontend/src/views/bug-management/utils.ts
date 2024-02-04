/** *
 * 缺陷管理
 * @description 将文件信息转换为文件格式
 * @param {stafileInfotus} 文件file
 */

import { MsFileItem } from '@/components/pure/ms-upload/types';

import { AssociatedList } from '@/models/caseManagement/featureCase';

export function convertToFileByBug(fileInfo: AssociatedList): MsFileItem {
  const gatewayAddress = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
  const fileName = fileInfo.fileType ? `${fileInfo.name}.${fileInfo.fileType || ''}` : `${fileInfo.name}`;
  const type = fileName.split('.')[1];
  const file = new File([new Blob()], `${fileName}`, {
    type: `application/${type}`,
  });
  Object.defineProperty(file, 'size', { value: fileInfo.fileSize });
  const { fileId, associated, isUpdateFlag, refId } = fileInfo;
  return {
    enable: fileInfo.enable || false,
    file,
    name: fileName,
    percent: 0,
    status: 'done',
    uid: fileId,
    url: `${gatewayAddress}/${fileInfo.filePath || ''}`,
    local: !associated,
    deleteContent: associated ? 'caseManagement.featureCase.cancelLink' : '',
    isUpdateFlag,
    associateId: refId,
  };
}

export default {};
