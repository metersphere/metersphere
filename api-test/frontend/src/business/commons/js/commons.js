/* 报告状态 */
export const REPORT_STATUS = [
  { text: 'Pending', value: 'PENDING' },
  { text: 'Running', value: 'RUNNING' },
  { text: 'Rerunning', value: 'RERUNNING' },
  { text: 'Success', value: 'SUCCESS' },
  { text: 'Error', value: 'ERROR' },
  { text: 'FakeError', value: 'FAKE_ERROR' },
  { text: 'Stopped', value: 'STOPPED' },
];

export function getReportStatusColor(status) {
  if (status) {
    status = status.toUpperCase();
  }
  if (status === 'SUCCESS') {
    return '#5daf34';
  } else if (status === 'FAKE_ERROR') {
    return '#F6972A';
  } else if (status === 'ERROR') {
    return '#FE6F71';
  } else {
    return '';
  }
}
