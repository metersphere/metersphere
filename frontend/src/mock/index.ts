import Mock from 'mockjs';

import './user';
import './message-box';
import './api-test';

import '@/views/dashboard/workplace/mock';

Mock.setup({
  timeout: '600-1000',
});
