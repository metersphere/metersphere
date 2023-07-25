import CustomNum from './customNum';

export const jsonParse = (jsonStr) => {
  let index = 0;
  function parseValue() {
    let char = jsonStr[index];
    while (char === ' ' || char === '\n') {
      index++;
      char = jsonStr[index];
    }
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
    } else if (char === ' ' || char === '\n') {
      index++;
      return parseValue();
    } else {
      return parseNumber();
    }
  }
  function parseObject() {
    let obj = {};
    index++;
    while (jsonStr[index] !== '}' && index < jsonStr.length) {
      let key = parseString();
      if (key === '}') {
        index++;
        return obj;
      }
      index++;
      let value = parseValue();
      obj[key] = value;
      if (jsonStr[index] === ',') {
        index++;
      }
      while (jsonStr[index] === ' ' || jsonStr[index] === '\n') {
        index++;
      }
    }
    if (index > jsonStr.length) {
      throwError('Invalid object');
    }
    index++;
    return obj;
  }
  function parseArray() {
    let arr = [];
    index++;
    while (jsonStr[index] !== ']' && index < jsonStr.length) {
      arr.push(parseValue());
      if (jsonStr[index] === ',') {
        index++;
      }
      while(jsonStr[index] === ' ' || jsonStr[index] === '\n'){
        index++;
      }
    }
    if (index > jsonStr.length) {
      throwError('Invalid array');
    }
    index++;
    return arr;
  }
  function parseString() {
    let str = '';
    while(jsonStr[index] === ' ' || jsonStr[index] === '\n'){
      index++;
    }
    index++;
    while (jsonStr[index] !== '"' && index < jsonStr.length) {
      let char = jsonStr[index];
      if (char === '\\') {
        index++;
        let nextChar = jsonStr[index];
        switch (nextChar) {
          case '"':
            str += '"';
            break;
          case '\\':
            str += '\\';
            break;
          case '/':
            str += '/';
            break;
          case 'b':
            str += '\b';
            break;
          case 'f':
            str += '\f';
            break;
          case 'n':
            str += '\n';
            break;
          case 'r':
            str += '\r';
            break;
          case 't':
            str += '\t';
            break;
          case 'u':
            // eslint-disable-next-line no-case-declarations
            let unicode = jsonStr.substr(index + 1, 4);
            str += String.fromCharCode(parseInt(unicode, 16));
            index += 4;
            break;
          default:
            // If an unknown escape sequence is encountered, treat it as a literal character
            str += '\\' + nextChar;
            break;
        }
      } else {
        str += char;
      }
      index++;
    }
    if (index > jsonStr.length) {
      throwError('Invalid string');
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
    if (isNaN(Number(numStr))) {
      throwError('Invalid number');
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
