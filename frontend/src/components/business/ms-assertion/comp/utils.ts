import {
  CONTAINS,
  EMPTY,
  END_WITH,
  EQUAL,
  NO_CHECK,
  NO_CONTAINS,
  NOT_EMPTY,
  NOT_EQUAL,
  REGEX,
  START_WITH,
} from '@/components/pure/ms-advance-filter/index';

export const statusCodeOptions = [CONTAINS, NO_CONTAINS, EQUAL, NOT_EQUAL];
export const codeOptions = [CONTAINS, NO_CONTAINS, EQUAL, NOT_EQUAL, NO_CHECK];

// 断言响应头
export const responseHeaderOption = [
  { label: 'Content-Type', value: 'Content-Type' },
  { label: 'Content-Length', value: 'Content-Length' },
  { label: 'Content-Control', value: 'Content-Control' },
  { label: 'Content-Disposition', value: 'Content-Disposition' },
  { label: 'Content-Encoding', value: 'Content-Encoding' },
  { label: 'Location', value: 'Location' },
  { label: 'Set-Cookie', value: 'Set-Cookie' },
  { label: 'Access-Control-Allow-Origin', value: 'Access-Control-Allow-Origin' },
  { label: 'Expires', value: 'Expires' },
  { label: 'Last-Modified', value: 'Last-Modified' },
];

export default {};
