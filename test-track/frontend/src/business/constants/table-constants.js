import i18n from "@/i18n";
export const TEST_CASE_STATUS_MAP = {
  'Prepare': i18n.t('test_track.plan.plan_status_prepare'),
  'Underway': i18n.t('test_track.plan.plan_status_running'),
  'Completed': i18n.t('test_track.plan.plan_status_completed'),
  'Trash': i18n.t('test_track.plan.plan_status_trash')
}

export const CASE_DASHBOARD_CHART_COLOR = [
  '#F76964', '#FFD131', '#AA4FBF', '#10CECE',
  '#4E83FD', '#935AF6', '#50CEFB', '#FFA53D',
  '#62D256'
];

export const RELEVANCE_CASE_DASHBOARD_CHART_COLOR = [
  '#AA4FBF', '#FFD131', '#10CECE', '#4E83FD'
];

export const DEFAULT_DASHBOARD_CHART_COLOR = [
  '#AA4FBF', '#FFD131', '#10CECE', '#4E83FD',
  '#935AF6', '#50CEFB', '#FFA53D', '#62D256'
];
