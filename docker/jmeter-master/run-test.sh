for file in ${TESTS_DIR}/*.jmx; do
  jmeter -n -t ${file} -Jserver.rmi.ssl.disable=${SSL_DISABLED} -l ${TESTS_DIR}/${REPORT_ID}.jtl
done
