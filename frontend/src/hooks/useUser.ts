import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { useUserStore } from '@/store';
import { useI18n } from '@/hooks/useI18n';

/**
 * 用户相关
 * @returns 调用方法
 */
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
  return {
    logout,
  };
}
