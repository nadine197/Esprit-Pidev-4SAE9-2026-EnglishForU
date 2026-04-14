# syntax=docker/dockerfile:1
FROM node:20-slim AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN node ./node_modules/@angular/cli/bin/ng.js build --configuration production

FROM nginx:alpine
COPY --from=build /app/dist/job-board /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
