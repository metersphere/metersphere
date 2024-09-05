import { isMacOs } from '@/utils';

export default defineComponent((props: { size?: number }) => {
  const isMac = isMacOs();

  return () => (isMac ? <icon-command size={props.size || 14} /> : 'Ctrl');
});
