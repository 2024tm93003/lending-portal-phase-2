FROM node:20-alpine AS deps
WORKDIR /web
COPY package*.json ./
RUN npm install

FROM node:20-alpine AS build
WORKDIR /web
COPY --from=deps /web/node_modules ./node_modules
COPY . .
ARG APP_CONFIG_FILE=public/config/app.properties
RUN if [ "$APP_CONFIG_FILE" != "public/config/app.properties" ]; then \
      test -f "$APP_CONFIG_FILE"; \
      cp "$APP_CONFIG_FILE" public/config/app.properties; \
    fi
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /web/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
