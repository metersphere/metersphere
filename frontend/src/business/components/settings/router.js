import Setting from "@/business/components/settings/Setting";
import User from "@/business/components/settings/system/User";
import Organization from "@/business/components/settings/system/Organization";
import OrganizationMember from "@/business/components/settings/organization/OrganizationMember";
import OrganizationWorkspace from "@/business/components/settings/organization/OrganizationWorkspace";
import ServiceIntegration from "@/business/components/settings/organization/ServiceIntegration";
import PersonSetting from "@/business/components/settings/personal/PersonSetting";
import ApiKeys from "@/business/components/settings/personal/ApiKeys";
import Member from "@/business/components/settings/workspace/WorkspaceMember";
import SystemWorkspace from "@/business/components/settings/system/SystemWorkspace";
import TestResourcePool from "@/business/components/settings/system/TestResourcePool";
import SystemParameterSetting from "@/business/components/settings/system/SystemParameterSetting";
import TestCaseReportTemplate from "@/business/components/settings/workspace/TestCaseReportTemplate";

const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/)

export default {
  path: "/setting",
  name: "Setting",
  components: {
    content: Setting
  },
  children: [
    {
      path: 'user',
      component: User,
      meta: {system: true, title: 'commons.user'}
    },
    {
      path: 'organization',
      component: Organization,
      meta: {system: true, title: 'commons.organization'}
    },
    {
      path: 'systemworkspace',
      component: SystemWorkspace,
      meta: {system: true, title: 'commons.workspace'}
    },
    {
      path: 'testresourcepool',
      component: TestResourcePool,
      meta: {system: true, title: 'commons.test_resource_pool'}
    },
    {
      path: 'systemparametersetting',
      component: SystemParameterSetting,
      meta: {system: true, title: 'commons.system_parameter_setting'}
    },
    ...requireContext.keys().map(key => requireContext(key).system),
    {
      path: 'organizationmember',
      component: OrganizationMember,
      meta: {organization: true, title: 'commons.member'}
    },
    {
      path: 'organizationworkspace',
      component: OrganizationWorkspace,
      meta: {organization: true, title: 'commons.workspace'}
    },
    {
      path: 'serviceintegration',
      component: ServiceIntegration,
      meta: {organization: true, title: 'organization.service_integration'}
    },
    {
      path: 'member',
      component: Member,
      meta: {workspace: true, title: 'commons.member'}
    },
    {
      path: 'testcase/report/template',
      name: 'testCaseReportTemplate',
      component: TestCaseReportTemplate,
      meta: {workspace: true, title: 'test_track.plan_view.report_template'}
    },
    {
      path: 'personsetting',
      component: PersonSetting,
      meta: {person: true, title: 'commons.personal_setting'}
    },
    {
      path: 'apikeys',
      component: ApiKeys,
      meta: {person: true, title: 'commons.api_keys'}
    },

  ]
}
