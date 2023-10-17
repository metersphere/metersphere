import { ref } from 'vue';
import dayjs from 'dayjs';

// 字段类型-日期
const dateOptions = ref([
  {
    label: dayjs().format('YYYY/MM/DD'),
    value: 'DATE',
  },
  {
    label: dayjs().format('YYYY/MM/DD HH:mm:ss'),
    value: 'DATETIME',
  },
]);

// 字段类型- 数字
const numberTypeOptions = ref([
  {
    label: '整数',
    value: 'INT',
  },
  {
    label: '保留小数',
    value: 'FLOAT',
  },
]);

export const getFieldType = (selectFieldType: string) => {
  switch (selectFieldType) {
    case 'DATE':
      return dateOptions.value;
    case 'NUMBER':
      return numberTypeOptions.value;
    default:
      break;
  }
};

export default {};
