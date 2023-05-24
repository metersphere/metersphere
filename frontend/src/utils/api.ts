// @ts-ignore
const arcoConfig = window.arcoConfig || {};

export const hostname =
  window.location.hostname !== arcoConfig.externalHostName && window.location.hostname !== arcoConfig.internalHostName
    ? arcoConfig.externalHostName
    : window.location.hostname;

export const isExternal = window.location.hostname === arcoConfig.externalHostName;
export const apiBasename = `https://${hostname || 'arco.design'}`;
