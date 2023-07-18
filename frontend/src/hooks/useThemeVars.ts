const bodyStyle = getComputedStyle(document.body);

export const getSingleVar = (_var: string) => {
  return bodyStyle.getPropertyValue(_var);
};

export const P9 = getSingleVar('--primary-9');
export const P5 = getSingleVar('--primary-5');
export const P4 = getSingleVar('--primary-4');
export const P3 = getSingleVar('--primary-3');
export const P2 = getSingleVar('--primary-2');
export const P1 = getSingleVar('--primary-1');

export const primaryVars = {
  P1,
  P2,
  P3,
  P4,
  P5,
  P9,
};

export const allVars = {
  ...primaryVars,
};

export const useThemeVars = () => {
  return {
    allVars,
    getSingleVar,
  };
};
