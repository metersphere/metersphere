import { Message } from '@arco-design/web-vue';

import MsButton from '@/components/pure/ms-button/index.vue';

import { switchProject } from '@/api/modules/project-management/project';
import { switchUserOrg } from '@/api/modules/system';
import { useI18n } from '@/hooks/useI18n';
import router from '@/router';
import { NO_PROJECT_ROUTE_NAME } from '@/router/constants';
import { useUserStore } from '@/store';
import useAppStore from '@/store/modules/app';
import { hasAnyPermission } from '@/utils/permission';

import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

const { t } = useI18n();
const userStore = useUserStore();
const appStore = useAppStore();

export async function enterProject(projectId: string, organizationId?: string) {
  try {
    appStore.showLoading();
    // 切换组织
    if (organizationId) {
      await switchUserOrg(organizationId, userStore.id || '');
    }
    await userStore.isLogin(true);
    // 切换项目
    await switchProject({
      projectId,
      userId: userStore.id || '',
    });
    appStore.setCurrentProjectId(projectId);
    if (!appStore.currentProjectId || appStore.currentProjectId === 'no_such_project') {
      // 没有项目权限(组织没有项目, 或项目全被禁用)，则重定向到无项目权限页面
      router.push({
        name: NO_PROJECT_ROUTE_NAME,
      });
      return;
    }
    // 跳转到项目页面
    router.replace({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION,
      query: {
        orgId: appStore.currentOrgId,
        pId: appStore.currentProjectId,
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
