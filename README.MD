# 🛒 Carrito de Compras - Frontend React

Este es el frontend de una aplicación de carrito de compras desarrollada con **ReactJS** y contenedorizada con **Docker** y **Docker Compose**.

## 🚀 Requisitos

Antes de comenzar, asegúrate de tener instalado:

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

## 🧱 Estructura Principal

- `Dockerfile`: Configuración de entorno React.
- `docker-compose.yml`: Orquestación del contenedor.
- `.env`: Variables de entorno necesarias para la ejecución.
- `package.json`: Dependencias del proyecto React.

## ⚙️ Configuración

1. Clona este repositorio o descomprime el proyecto.
2. Crea un archivo `.env` en la raíz (ya incluido) con las siguientes variables:

```env
REACT_APP_AUTH_URL=http://localhost:8081
REACT_APP_CATALOG_URL=http://localhost:8082
REACT_APP_CART_URL=http://localhost:8083
REACT_APP_ORDER_URL=http://localhost:8084
```

> Asegúrate de que estos servicios estén disponibles o reemplaza las URLs según tu entorno.

## 🐳 Ejecutar con Docker

Desde la raíz del proyecto (donde se encuentra `docker-compose.yml`), ejecuta:

```bash
docker compose up --build -d
```

Esto levantará la aplicación React en modo desarrollo en `http://localhost:3000`.

## 🧪 Scripts útiles

Puedes usar los siguientes comandos dentro del contenedor o directamente si no usas Docker:

```bash
npm install      # Instala dependencias
npm start        # Ejecuta la app en modo desarrollo
npm run build    # Genera una versión optimizada para producción
```

## 📦 Dependencias principales

- React 18.2
- Axios
- React Router DOM
- React Hook Form + Yup
- Bootstrap + React Bootstrap

## 🛠 Desarrollo

Si deseas desarrollar sin Docker:

```bash
npm install
npm start
```

Y asegúrate de tener configurado el archivo `.env` localmente.

---

© 2025 - Proyecto desarrollado por Miguel Angel Villalba Cabañas