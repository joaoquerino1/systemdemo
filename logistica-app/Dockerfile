FROM node:20-alpine AS build

WORKDIR /app
COPY logistica-app/package*.json ./
RUN npm ci
COPY logistica-app/ .

ARG API_URL=/api
RUN sed -i -E "s|apiUrl: '[^']*'|apiUrl: '${API_URL}'|" src/environments/environment.ts src/environments/environment.prod.ts
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/logistica-app/browser /usr/share/nginx/html
COPY logistica-app/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
