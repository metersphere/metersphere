// eslint-disable-next-line import/default
import exportPDFWorker from './exportPDFWorker?worker';
import html2canvas from 'html2canvas-pro';

// eslint-disable-next-line new-cap
const worker = new exportPDFWorker();

worker.onmessage = (event: MessageEvent) => {
  const { name, pdfBlob } = event.data;
  const url = URL.createObjectURL(pdfBlob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${name}.pdf`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

const exportPdf = async (name: string, contentId: string) => {
  const element = document.getElementById(contentId);
  if (element) {
    // await replaceSvgWithBase64(element);
  }
};

export default exportPdf;
