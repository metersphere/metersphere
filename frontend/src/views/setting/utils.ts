import { Message } from '@arco-design/web-vue';

import MsButton from '@/components/pure/ms-button/index.vue';

import { switchProject } from '@/api/modules/project-management/project';
import { switchUserOrg } from '@/api/modules/system';
import { useI18n } from '@/hooks/useI18n';
import router from '@/router';
import { NO_PROJECT_ROUTE_NAME } from '@/router/constants';
import { useUserStore } from '@/store';
import useAppStore from '@/store/modules/app';
import useLicenseStore from '@/store/modules/setting/license';
import { getFirstRouteNameByPermission, hasAnyPermission } from '@/utils/permission';

const { t } = useI18n();
const userStore = useUserStore();
const appStore = useAppStore();
const licenseStore = useLicenseStore();

// 进入组织
export async function enterOrganization(organizationId: string) {
  try {
    appStore.showLoading();
    if (appStore.currentOrgId !== organizationId) {
      if (!licenseStore.hasLicense()) {
        router.push({
          name: NO_PROJECT_ROUTE_NAME,
        });
        return;
      }
      await switchUserOrg(organizationId, userStore.id || '');
      await userStore.isLogin();
      await userStore.checkIsLogin(true);
    }
  } catch (error) {
    console.log(error);
  } finally {
    appStore.hideLoading();
  }
}

export async function enterProject(projectId: string, organizationId?: string) {
  try {
    appStore.showLoading();
    // 切换组织
    if (organizationId && appStore.currentOrgId !== organizationId) {
      if (!licenseStore.hasLicense()) {
        router.push({
          name: NO_PROJECT_ROUTE_NAME,
        });
        return;
      }
      await switchUserOrg(organizationId, userStore.id || '');
    }
    await userStore.isLogin();
    // 切换项目
    await switchProject({
      projectId,
      userId: userStore.id || '',
    });
    await userStore.checkIsLogin(true);
    // 跳转到项目页面
    router.replace({
      name: getFirstRouteNameByPermission(router.getRoutes()),
      query: {
        orgId: appStore.currentOrgId,
        pId: projectId,
      },
    });
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
  } finally {
    appStore.hideLoading();
  }
}

export function showUpdateOrCreateMessage(isEdit: boolean, id: string, organizationId?: string) {
  if (isEdit) {
    Message.success(t('system.project.updateProjectSuccess'));
  } else if (!hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
    Message.success(t('system.project.createProjectSuccess'));
  } else {
    Message.success({
      content: () =>
        h('div', { class: 'flex items-center gap-[12px]' }, [
          h('div', t('system.project.createProjectSuccess')),
          h(
            MsButton,
            {
              type: 'text',
              onClick() {
                enterProject(id, organizationId);
              },
            },
            { default: () => t('system.project.enterProject') }
          ),
        ]),
    });
  }
}
