FROM node:18

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Build del frontend con las variables disponibles
RUN npm run build

EXPOSE 3000

CMD ["npm", "start"]
