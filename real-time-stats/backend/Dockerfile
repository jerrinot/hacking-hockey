FROM alpine:3.14.1

RUN apk add npm nodejs

RUN mkdir /app

COPY index.js package.json /app/

WORKDIR /app

RUN npm i

ENTRYPOINT ["npm", "start"]
