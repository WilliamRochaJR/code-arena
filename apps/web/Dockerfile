FROM node:22-alpine AS build

WORKDIR /workspace
ENV HUSKY=0

COPY package.json package-lock.json ./
COPY apps/web/package.json apps/web/package.json
COPY packages/java-quiz-sdk/package.json packages/java-quiz-sdk/package.json
COPY packages/ui/package.json packages/ui/package.json
RUN npm ci

COPY tsconfig.base.json ./
COPY apps/web/ apps/web/
COPY packages/java-quiz-sdk/ packages/java-quiz-sdk/
COPY packages/ui/ packages/ui/
RUN npm run build:sdk && npm run build --workspace @code-arena/web

FROM nginxinc/nginx-unprivileged:1.29-alpine AS runtime

COPY apps/web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/apps/web/dist/ /usr/share/nginx/html/

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1:8080/ || exit 1
