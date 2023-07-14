import { defineStore } from 'pinia';
import { MsTableState } from './types';

const msTableStore = defineStore('msTable', {
  state: (): MsTableState => ({}),
});

export default msTableStore;
