# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
ARG API_BASE_URL=http://localhost:8080/api
ENV API_BASE_URL=$API_BASE_URL
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# ---- Runtime stage ----
FROM nginx:1.27-alpine
COPY --from=build /app/dist/jobflow/browser /usr/share/nginx/html
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
