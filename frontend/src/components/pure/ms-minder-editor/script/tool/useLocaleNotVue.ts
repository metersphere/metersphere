import enUS from '../../locale/en-US';
import zhCN from '../../locale/zh-CN';
import type { Recordable } from '#/global';

const findCode = (arr: string[], tree: Recordable<Recordable>) => {
  let curCode = arr.shift();
  let curNode = tree[curCode || ''];
  while (curCode) {
    if (!curCode || !curNode) {
      return '';
    }
    if (curNode && arr.length === 0) {
      return curNode;
    }
    curCode = arr.shift();

    curNode = curNode[curCode || ''];
  }
};

export default function useLocaleNotVue(key: string) {
  const locale = localStorage.getItem('MS-locale') || 'zh-CN';

  const arr = key.split('.');
  switch (locale) {
    case 'zh-CN':
      // eslint-disable-next-line no-eval
      return findCode(arr, zhCN);
    case 'en-US':
      return findCode(arr, enUS);
    default:
      return key;
  }
}
