export function addPixelValues(...values: string[]) {
  const pixelValues = values.filter((v) => v.endsWith('px')).map((v) => parseInt(v, 10));
  const totalValue = pixelValues.reduce((acc, val) => acc + val, 0);
  return `${totalValue}px`;
}
export default {};
