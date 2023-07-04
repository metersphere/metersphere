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
  /**
   * 登出
   * @param logoutTo 登出后跳转的页面
   * @returns
   */
  const logout = async () => {
    await userStore.logout();
    Message.success(t('message.logoutSuccess'));
    router.push({ name: 'login' });
  };
  return {
    logout,
  };
}
