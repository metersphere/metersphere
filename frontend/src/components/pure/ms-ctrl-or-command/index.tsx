import { isMacOs } from '@/utils';

export default defineComponent(() => {
  const isMac = isMacOs();

  return () => (isMac ? <icon-command size={14} /> : 'Ctrl');
});
