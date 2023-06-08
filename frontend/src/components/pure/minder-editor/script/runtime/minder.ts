/**
 * @fileOverview
 *
 * 脑图示例运行时
 *
 * @author: techird
 * @copyright: Baidu FEX, 2014
 */
import { useI18n } from '@/hooks/useI18n';

const { t } = useI18n();

export default function MinderRuntime(this: { selector: string; minder?: any }) {
  // 不使用 kityminder 的按键处理，由 ReceiverRuntime 统一处理
  const { Minder } = window.kityminder;
  const minder = new Minder({
    enableKeyReceiver: false,
    enableAnimation: true,
  });

  // 渲染，初始化
  minder.renderTo(this.selector);
  minder.setTheme(null);
  minder.select(minder.getRoot(), true);
  minder.execCommand('text', t('minder.main.subject.central'));

  // 导出给其它 Runtime 使用
  this.minder = minder;
  window.minder = minder;
}
