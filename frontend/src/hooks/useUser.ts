import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import { useUserStore } from '@/store';

export default function useUser() {
  const router = useRouter();
  const userStore = useUserStore();
  const { t } = useI18n();

  const logout = async (logoutTo?: string) => {
    await userStore.logout();
    const currentRoute = router.currentRoute.value;
    Message.success(t('message.logoutSuccess'));
    router.push({
      name: logoutTo && typeof logoutTo === 'string' ? logoutTo : 'login',
      query: {
        ...router.currentRoute.value.query,
        redirect: currentRoute.name as string,
      },
    });
  };

  const isLoginPage = () => {
    return window.location.hash.indexOf('login') > -1;
  };

  return {
    logout,
    isLoginPage,
  };
}
