FROM node:20-alpine AS builder

WORKDIR /app

COPY .yarnrc.yml ./
COPY .yarn/releases ./.yarn/releases
COPY package.json yarn.lock ./

# Normalise line endings in case files were checked out on Windows
RUN apk add --no-cache dos2unix \
  && dos2unix .yarnrc.yml yarn.lock \
  && dos2unix .yarn/releases/yarn-3.8.2.cjs || true

RUN yarn install

COPY . .
RUN yarn build:prod

FROM node:20-alpine

WORKDIR /app

COPY .yarnrc.yml ./
COPY .yarn/releases ./.yarn/releases
COPY package.json yarn.lock ./

RUN apk add --no-cache dos2unix \
  && dos2unix .yarnrc.yml yarn.lock \
  && dos2unix .yarn/releases/yarn-3.8.2.cjs || true

RUN yarn install

COPY --from=builder /app/src ./src

EXPOSE 3100

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3100/ || exit 1

CMD ["yarn", "start"]
