import i18n from "metersphere-frontend/src/i18n/index"

//用例评审-测试用例
export const Test_Case_Review = [
  {id: 'name', label: i18n.t('test_track.review.review_name')},
  {id: 'reviewer', label: i18n.t('test_track.review.reviewer')},
  {id: 'projectName', label: i18n.t('test_track.review.review_project')},
  {id: 'creatorName', label: i18n.t('test_track.review.creator')},
  {id: 'status', label: i18n.t('test_track.review.review_status')},
  {id: 'createTime', label: i18n.t('commons.create_time')},
  {id: 'endTime', label: i18n.t('test_track.review.end_time')},
  {id: 'tags', label: i18n.t('commons.tag')},
]

//测试评审-测试用例
export const Test_Case_Review_Case_List = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('commons.name')},
  {id: 'priority', label: i18n.t('test_track.case.priority')},
  {id: 'nodePath', label: i18n.t('test_track.case.module')},
  {id: 'projectName', label: i18n.t('test_track.review.review_project')},
  {id: 'reviewerName', label: i18n.t('test_track.review.reviewer')},
  {id: 'reviewStatus', label: i18n.t('test_track.case.status')},
  {id: 'updateTime', label: i18n.t('commons.update_time')},
  {id: 'maintainer', label: i18n.t('custom_field.case_maintainer')},
]

export const API_METHOD_COLOUR = [
  ['GET', "#61AFFE"], ['POST', '#49CC90'], ['PUT', '#fca130'],
  ['PATCH', '#E2EE11'], ['DELETE', '#f93e3d'], ['OPTIONS', '#0EF5DA'],
  ['HEAD', '#8E58E7'], ['CONNECT', '#90AFAE'],
  ['DUBBO', '#C36EEF'], ['dubbo://', '#C36EEF'], ['SQL', '#0AEAD4'], ['TCP', '#0A52DF'],
];

export const CASE_PRIORITY = [
  {id: 'P0', label: 'P0'},
  {id: 'P1', label: 'P1'},
  {id: 'P2', label: 'P2'},
  {id: 'P3', label: 'P3'}
];
