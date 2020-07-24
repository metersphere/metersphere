for file in ${TESTS_DIR}/*.jmx; do
  echo "one shot run."
  jmeter -n -t ${file} -Jserver.rmi.ssl.disable=${SSL_DISABLED}
done
