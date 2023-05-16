import CustomNum from './customNum';

export const jsonParse = (jsonStr) => {
  let index = 0;
  function parseValue() {
    let char = jsonStr[index];
    if (char === '{') {
      return parseObject();
    } else if (char === '[') {
      return parseArray();
    } else if (char === '"') {
      return parseString();
    } else if (char === 't' || char === 'f') {
      return parseBoolean();
    } else if (char === 'n') {
      return parseNull();
    } else {
      return parseNumber();
    }
  }
  function parseObject() {
    let obj = {};
    index++;
    while (jsonStr[index] !== '}') {
      let key = parseString();
      index++;
      let value = parseValue();
      obj[key] = value;
      if (jsonStr[index] === ',') {
        index++;
      }
    }
    index++;
    return obj;
  }
  function parseArray() {
    let arr = [];
    index++;
    while (jsonStr[index] !== ']') {
      arr.push(parseValue());
      if (jsonStr[index] === ',') {
        index++;
      }
    }
    index++;
    return arr;
  }
  function parseString() {
    let str = '';
    index++;
    while (jsonStr[index] !== '"') {
      str += jsonStr[index];
      index++;
    }
    index++;
    return str;
  }
  function parseNumber() {
    let numStr = '';
    while (/[0-9.+-]/.test(jsonStr[index])) {
      numStr += jsonStr[index];
      index++;
    }
    if (!isInteger(numStr) || numStr.length > 15) {
      return new CustomNum(numStr);
    }
    return parseFloat(numStr);
  }
  function parseBoolean() {
    if (jsonStr[index] === 't') {
      index += 4;
      return true;
    } else {
      index += 5;
      return false;
    }
  }
  function parseNull() {
    index += 4;
    return null;
  }
  return parseValue();
};
export const isInteger = (num) => {
  return /^\d+$/.test(num);
};
export const trimAll = (ele) => {
  if (typeof ele === 'string') {
    return ele.split(/[\t\r\f\n\s]*/g).join('');
  }
};
