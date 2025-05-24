# file: ./frontend/Dockerfile

# Этап 1: Сборка React-приложения
FROM node:18-alpine AS build-stage
WORKDIR /app

# Копируем package.json и package-lock.json (или yarn.lock)
COPY package.json package-lock.json ./
# Если используете yarn, замените на:
# COPY package.json yarn.lock ./

# Устанавливаем зависимости
RUN npm install --legacy-peer-deps
# Если используете yarn, замените на:
# RUN yarn install --frozen-lockfile

# Копируем остальные файлы проекта
COPY . .

# Собираем приложение
RUN npm run build
# Если используете yarn, замените на:
# RUN yarn build

# Этап 2: Сервирование статических файлов с помощью Nginx
FROM nginx:stable-alpine

# Удаляем стандартную конфигурацию Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Копируем нашу конфигурацию Nginx (создадим ее на следующем шаге)
COPY nginx.conf /etc/nginx/conf.d/

# Копируем собранные статические файлы из этапа сборки
COPY --from=build-stage /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]