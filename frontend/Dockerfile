FROM nginx:1.24.0

COPY dist/ /app/
RUN mkdir /cnf && chown 777 /cnf
COPY nginx.conf /cnf/nginx.conf
EXPOSE 5100
WORKDIR /app

CMD [ "sh", "-c", "nginx -g 'daemon off;' -c /cnf/nginx.conf"]