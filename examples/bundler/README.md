# Tyrian counter example (via Bundler)

This is a minimal working project setup to run the counter example.

To compile, follow the instructions in the parent directories README.md file.

To serve the site, you will need a static web server. A simple one is `http-server` (install: `npm install -g http-server`) which you can launch in the same directory as your `index.html` page as follows

```sh
http-server -c-1
```

The arguments disable the servers cache.

Then navigate to [http://localhost:8080/](http://localhost:8080/)
