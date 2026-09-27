# Última versión de nginx sirviendo únicamente un index.html
FROM nginx:latest

# Se elimina el contenido por defecto para que el contenedor sirva solo nuestro index.html
RUN rm -rf /usr/share/nginx/html/*
COPY index.html /usr/share/nginx/html/index.html

EXPOSE 80
# nginx:latest ya define CMD ["nginx", "-g", "daemon off;"]

# Construir y ejecutar:
#   docker build -t mi-nginx .
#   docker run -d -p 8081:80 --name mi-nginx mi-nginx
