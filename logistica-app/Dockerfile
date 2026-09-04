FROM node:20-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

ARG API_URL=/api
RUN sed -i -E "s|apiUrl: '[^']*'|apiUrl: '${API_URL}'|" src/environments/environment.ts src/environments/environment.prod.ts
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/logistica-app/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template

# nginx listens on $PORT (Railway injects it). Locally (docker-compose) it stays 80.
ENV PORT=80
# Base URL of the Spring Boot API. Locally the compose network name; on Railway
# set this to the api service domain (e.g. https://<api>.up.railway.app).
ENV BACKEND_INTERNAL_URL=http://backend:8080

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]