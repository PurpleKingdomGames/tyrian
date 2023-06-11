# Tyrian Electron Example (using the Counter example)

![Tyrian Electron Example (using the Counter example)](tyrian_electron_example.png "Tyrian Electron Example (using the Counter example)")

This is an as-simple-as-it-gets example of building an [Electron](https://www.electronjs.org/) desktop app using Tyrian.

To run the program you will need to have yarn (or npm) installed.

## Running the example

There are three steps to this:

1. Output the Scala.js app
2. Package up the app as a website
3. Run it via Electron

Points (1) and (2) are 90% just the usual stuff we see in all the other examples. Let's get started!

### Step 1 - Output the Scala.js app

From the root of the examples folder containing the `build.sbt` file, run the following to compile our Scala.js app:

```sh
sbt electron/fastLinkJS
```

Then make sure parcel is set up:

```sh
yarn install
```

### Step 2 - Package up the app as a website

Now lets package up our website, this throws all the linked up code into the `dist` directory.

```sh
yarn build
```

**If** you went into the `dist` folder now, you could run your site in a browser using a static web server like `http-server -c-1` (install with `npm install -g http-server`) and then navigate to [http://localhost:8080/](http://localhost:8080/).

### Step 3 - Run it via Electron

..and that's just as well, because Electron ***(waves hands)*** _is a static web server with a packaged up Google Chrome sitting on top of it_. Let's see how that works.

`cd` into the `app` directory (so: `tyrian/examples/electron/app`).

> Exercise of the reader: There are some files in here for you to explore in conjunction with the Electron documentation.

Now we need to install Electron and run our app. Luckily, all that is described in our `package.json`:

```sh
yarn install
```

Installs electron, and you run your app with the usual:

```sh
yarn start
```
