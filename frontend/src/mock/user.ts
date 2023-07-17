import { mock } from '@/utils/setup-mock';
import { RequestEnum } from '@/enums/httpEnum';
import { LogoutUrl, LoginUrl } from '@/api/requrls/user';

// mock(RequestEnum.POST, LoginUrl, {}, 200);
mock(RequestEnum.POST, LogoutUrl, null, 200);
