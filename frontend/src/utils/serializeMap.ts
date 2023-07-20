export const stringify = (source: any) => {
  return JSON.stringify(source, (key, value) => {
    if (value instanceof Map) {
      return {
        __dataType__: 'Map',
        val: Array.from(value.entries()), // or with spread: value: [...value]
      };
    }
    return value;
  });
};

export const parse = (source: string) => {
  return JSON.parse(source, (key, value) => {
    if (typeof value === 'object' && value !== null) {
      if (value.__dataType__ === 'Map') {
        return new Map(value.value);
      }
    }
    return value;
  });
};
