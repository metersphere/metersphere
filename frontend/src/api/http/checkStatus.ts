import { Message, Modal } from '@arco-design/web-vue';
import { useI18n } from '@/hooks/useI18n';
import { useRouter } from 'vue-router';

import type { ErrorMessageMode } from '#/axios';

export default function checkStatus(status: number, msg: string, errorMessageMode: ErrorMessageMode = 'message'): void {
  const { t } = useI18n();
  let errMessage = '';
  switch (status) {
    case 400:
      errMessage = `${msg}`;
      break;
    case 401: {
      errMessage = msg || t('api.errMsg401');
      const router = useRouter();
      router.push('/login');
      break;
    }
    case 403:
      errMessage = t('api.errMsg403');
      break;
    // 404请求不存在
    case 404:
      errMessage = t('api.errMsg404');
      break;
    case 405:
      errMessage = t('api.errMsg405');
      break;
    case 408:
      errMessage = t('api.errMsg408');
      break;
    case 500:
      errMessage = t('api.errMsg500');
      break;
    case 501:
      errMessage = t('api.errMsg501');
      break;
    case 502:
      errMessage = t('api.errMsg502');
      break;
    case 503:
      errMessage = t('api.errMsg503');
      break;
    case 504:
      errMessage = t('api.errMsg504');
      break;
    case 505:
      errMessage = t('api.errMsg505');
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
