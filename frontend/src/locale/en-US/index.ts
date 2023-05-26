import dayjsLocale from 'dayjs/locale/en';
import localeSettings from './settings';
import sys from './sys';
import localeMessageBox from '@/components/message-box/locale/en-US';
import minder from '@/components/minder-editor/locale/en-US';
import localeLogin from '@/views/login/locale/en-US';
import localeWorkplace from '@/views/dashboard/workplace/locale/en-US';
import localeThemebox from '@/components/theme-box/locale/en-US';
import localeApiTest from '@/views/api-test/locale/en-US';

export default {
  message: {
    'menu.component': 'component hub',
    'menu.component.demo': 'component demo',
    'menu.apitest': 'Api Test',
    'menu.dashboard': 'Dashboard',
    'menu.minder': 'Minder',
    'menu.server.dashboard': 'Dashboard-Server',
    'menu.server.workplace': 'Workplace-Server',
    'menu.server.monitor': 'Monitor-Server',
    'menu.list': 'List',
    'menu.result': 'Result',
    'menu.exception': 'Exception',
    'menu.form': 'Form',
    'menu.profile': 'Profile',
    'menu.visualization': 'Data Visualization',
    'menu.user': 'User Center',
    'navbar.action.locale': 'Switch to English',
    ...sys,
    ...localeSettings,
    ...localeMessageBox,
    ...localeLogin,
    ...localeWorkplace,
    ...minder,
    ...localeThemebox,
    ...localeApiTest,
  },
  dayjsLocale,
  dayjsLocaleName: 'en-US',
};
