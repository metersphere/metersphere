import { LoginUrl, LogoutUrl } from '@/api/requrls/user';
import { mock } from '@/utils/setup-mock';

import { RequestEnum } from '@/enums/httpEnum';

// mock(RequestEnum.POST, LoginUrl, {}, 200);
mock(RequestEnum.POST, LogoutUrl, null, 200);
