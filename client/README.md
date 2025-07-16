# To work on the frontend...

## Install node.js & npm

On mac if you have brew
`brew install node`

On windows if you have choco
`choco install nodejs-lts`

or install from
https://nodejs.org/en


## Setup directory

Make sure you have the backend Tomcat server running.

```
cd frontend-shadcn/
npm install
npm run dev
```

## Build
we will use `npm run build` to get our static files and put them in our java backend webapp/ 
directory and build the .war file when its time to submit so that Tomcat runs everything.