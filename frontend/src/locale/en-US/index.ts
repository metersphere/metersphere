import dayjsLocale from 'dayjs/locale/en';
import localeSettings from './settings';
import sys from './sys';
import localeMessageBox from '@/components/pure/message-box/locale/en-US';
import minder from '@/components/pure/minder-editor/locale/en-US';
import localeLogin from '@/views/login/locale/en-US';
import localeTable from '@/components/pure/ms-table/locale/en-US';
import localeApiTest from '@/views/api-test/locale/en-US';
import localeSystem from '@/views/system/locale/en-US';

export default {
  message: {
    'menu.apiTest': 'Api Test',
    'menu.settings': 'System Settings',
    'menu.settings.usergroup': 'User Group',
    'menu.settings.user': 'User',
    'menu.settings.organization': 'Organization',
    'navbar.action.locale': 'Switch to English',
    ...sys,
    ...localeSettings,
    ...localeMessageBox,
    ...localeLogin,
    ...minder,
    ...localeTable,
    ...localeApiTest,
    ...localeSystem,
  },
  dayjsLocale,
  dayjsLocaleName: 'en-US',
};
