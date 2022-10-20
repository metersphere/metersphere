import i18n from "@/i18n";

export const REPORT_STATUS_MAP = new Map([
  ["success", {name: i18n.t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
  ["Success", {name: i18n.t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
  ["SUCCESS", {name: i18n.t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
  ["Pass", {name: i18n.t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
  ["error", {name: i18n.t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
  ["Error", {name: i18n.t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
  ["ERROR", {name: i18n.t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
  ["Fail", {name: i18n.t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
  ["Failure", {name: i18n.t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
  ["Prepare", {name: i18n.t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
  ["PENDING", {name: i18n.t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
  ["Underway", {name: i18n.t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
  ["RERUNNING", {name: i18n.t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
  ["FAKE_ERROR", {name: i18n.t('error_report_library.option.name'), itemStyle: {color: '#F6972A'}}],
  ["run", {name: i18n.t('test_track.plan_view.running'), itemStyle: {color: '#DEDE10'}}],
]);
