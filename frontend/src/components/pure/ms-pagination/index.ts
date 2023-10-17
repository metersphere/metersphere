import _Pagination from './pagination';
import type { ArcoOptions } from './types';
import { getComponentPrefix, setGlobalConfig } from './utils';
import type { App } from 'vue';

const MsPagination = Object.assign(_Pagination, {
  install: (app: App, options?: ArcoOptions) => {
    setGlobalConfig(app, options);
    const componentPrefix = getComponentPrefix(options);

    app.component(componentPrefix + _Pagination.name, _Pagination);
  },
});

export type PaginationInstance = InstanceType<typeof _Pagination>;
export type { PaginationProps } from './interface';

export default MsPagination;
