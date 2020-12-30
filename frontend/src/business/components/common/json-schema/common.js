const Mock = require('mockjs');
const jsf = require('json-schema-faker');

jsf.extend('mock', function () {
  return {
    mock: function (xx) {
      return Mock.mock(xx);
    }
  };
});

const defaultOptions = {
  failOnInvalidTypes: false,
  failOnInvalidFormat: false
};

export const schemaToJson = (schema, options = {}) => {
  Object.assign(options, defaultOptions);

  jsf.option(options);
  let result;
  try {
    result = jsf.generate(schema);
  } catch (err) {
    result = err.message;
  }
  jsf.option(defaultOptions);
  return result;
};
