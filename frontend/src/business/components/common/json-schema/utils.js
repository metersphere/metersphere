export const JSONPATH_JOIN_CHAR = '.';
export const lang = 'zh_CN';
export const format = [
  {name: 'date-time'},
  {name: 'date'},
  {name: 'email'},
  {name: 'hostname'},
  {name: 'ipv4'},
  {name: 'ipv6'},
  {name: 'uri'}
];
export const SCHEMA_TYPE = [
  'string',
  'number',
  'array',
  'object',
  'boolean',
  'integer'
];
export const defaultInitSchemaData = {
  type: 'object',
  title: 'title',
  properties: {}
};
export const defaultSchema = {
  string: {
    type: 'string'
  },
  number: {
    type: 'number'
  },
  array: {
    type: 'array',
    items: {
      type: 'string'
    }
  },
  object: {
    type: 'object',
    properties: {}
  },
  boolean: {
    type: 'boolean'
  },
  integer: {
    type: 'integer'
  }
};

// 防抖函数，减少高频触发的函数执行的频率
// 请在 constructor 里使用:

// this.func = debounce(this.func, 400);
export const debounce = (func, wait) => {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(func, wait);
  };
};

export const getData = (state, keys) => {
  let curState = state;
  for (let i = 0; i < keys.length; i++) {
    curState = curState[keys[i]];
  }
  return curState;
};

export const setData = function (state, keys, value) {
  let curState = state;
  for (let i = 0; i < keys.length - 1; i++) {
    curState = curState[keys[i]];
  }
  curState[keys[keys.length - 1]] = value;
};

export const deleteData = function (state, keys) {
  let curState = state;
  for (let i = 0; i < keys.length - 1; i++) {
    curState = curState[keys[i]];
  }

  delete curState[keys[keys.length - 1]];
};

export const getParentKeys = function (keys) {
  if (keys.length === 1) return [];
  const arr = [].concat(keys);
  arr.splice(keys.length - 1, 1);
  return arr;
};

export const clearSomeFields = function (keys, data) {
  const newData = Object.assign({}, data);
  keys.forEach(key => {
    delete newData[key];
  });
  return newData;
};

function getFieldstitle(data) {
  const requiredtitle = [];
  Object.keys(data).map(title => {
    requiredtitle.push(title);
  });

  return requiredtitle;
}

export function handleSchemaRequired(schema, checked) {
  if (schema.type === 'object') {
    const requiredtitle = getFieldstitle(schema.properties);

    // schema.required = checked ? [].concat(requiredtitle) : [];
    if (checked) {
      schema.required = [].concat(requiredtitle);
    } else {
      delete schema.required;
    }

    handleObject(schema.properties, checked);
  } else if (schema.type === 'array') {
    handleSchemaRequired(schema.items, checked);
  } else {
    return schema;
  }
}

function handleObject(properties, checked) {
  for (var key in properties) {
    if (properties[key].type === 'array' || properties[key].type === 'object') {
      handleSchemaRequired(properties[key], checked);
    }
  }
}

export function cloneObject(obj) {
  if (typeof obj === 'object') {
    if (Array.isArray(obj)) {
      var newArr = [];
      obj.forEach(function (item, index) {
        newArr[index] = cloneObject(item);
      });
      return newArr;
    } else {
      var newObj = {};
      for (var key in obj) {
        newObj[key] = cloneObject(obj[key]);
      }
      return newObj;
    }
  } else {
    return obj;
  }
}

export const uuid = () => {
  return Math.random()
    .toString(16)
    .substr(2, 5);
};

export const log = (...args) => {

};

/**
 * val值不为空字符，null，undefined
 */
export const isNotNil = val => {
  const arr = [undefined, null, ''];
  return !arr.includes(val);
};

/**
 * form表单值校验是否为空，有值为空则返回true，值都正确则返回false
 */
export const isFormValid = obj => {
  if (typeof obj !== 'object') return true;
  const keys = Object.keys(obj);
  return keys.some(key => {
    return !isNotNil(obj[key]);
  });
};
/**
 * 只返回有值得属性新对象
 * @param {Object} formData 表单对象
 */
export const getValidFormVal = formData => {
  const obj = {};
  const keys = Object.keys(formData);
  keys.forEach(key => {
    if (isNotNil(formData[key])) {
      obj[key] = formData[key];
    }
  });

  return obj;
};
