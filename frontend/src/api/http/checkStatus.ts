import { Message, Modal } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import useUser from '@/hooks/useUser';
import router from '@/router';
import { NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
import useLicenseStore from '@/store/modules/setting/license';

import type { ErrorMessageMode } from '#/axios';

export default function checkStatus(
  status: number,
  msg: string,
  code?: number,
  errorMessageMode: ErrorMessageMode = 'message'
): void {
  const { t } = useI18n();
  const licenseStore = useLicenseStore();
  const { logout, isLoginPage, isWhiteListPage } = useUser();
  let errMessage = '';
  switch (status) {
    case 400:
      errMessage = `${msg}`;
      break;
    case 401: {
      errMessage = msg || t('api.errMsg401');
      if (!isLoginPage() && !isWhiteListPage()) {
        // 不是登录页再调用logout
        logout();
      }
      break;
    }
    case 403:
      if (router.currentRoute.value.name !== NO_RESOURCE_ROUTE_NAME) {
        router.push({ name: NO_RESOURCE_ROUTE_NAME });
      }
      break;
    // 404请求不存在
    case 404:
      errMessage = msg || t('api.errMsg404');
      break;
    case 405:
      errMessage = msg || t('api.errMsg405');
      break;
    case 408:
      errMessage = msg || t('api.errMsg408');
      break;
    case 500:
      if (code === 101511) {
        // 开源版创建用户超数量
        Message.error({
          content: () =>
            h(
              'div',
              {
                style: {
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                },
              },
              [
                h('span', t('user.openSourceCreateUsersLimit')),
                h(
                  'a',
                  {
                    href: 'https://jinshuju.net/f/CzzAOe',
                    target: '_blank',
                    style: {
                      color: 'rgb(var(--primary-5))',
                    },
                  },
                  t('user.businessTry')
                ),
              ]
            ),
          duration: 0,
          closable: true,
        });
        return;
      }
      if (code === 101512) {
        // 企业版创建用户超数量
        Message.error({
          content: () =>
            h(
              'div',
              {
                style: {
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                },
              },
              [
                h(
                  'span',
                  (licenseStore.licenseInfo?.license.count || 30) <= 30
                    ? t('user.businessCreateUsersLimitThirty')
                    : t('user.businessCreateUsersLimitMax', { count: licenseStore.licenseInfo?.license.count })
                ),
                h(
                  'a',
                  {
                    href: 'https://jinshuju.net/f/CzzAOe',
                    target: '_blank',
                    style: {
                      color: 'rgb(var(--primary-5))',
                    },
                  },
                  t('user.businessScaling')
                ),
              ]
            ),
          duration: 0,
          closable: true,
        });
        return;
      }
      errMessage = msg || t('api.errMsg500');
      break;
    case 501:
      errMessage = msg || t('api.errMsg501');
      break;
    case 502:
      errMessage = msg || t('api.errMsg502');
      break;
    case 503:
      errMessage = msg || t('api.errMsg503');
      break;
    case 504:
      errMessage = msg || t('api.errMsg504');
      break;
    case 505:
      errMessage = msg || t('api.errMsg505');
      break;
    default:
  }

  if (errMessage) {
    if (errorMessageMode === 'modal') {
      Modal.error({ title: t('api.errorTip'), content: errMessage });
    } else if (errorMessageMode === 'message') {
      Message.error(errMessage);
    }
  }
}
