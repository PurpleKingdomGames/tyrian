---
title: "Built-in Goodies"
menuTitle: "Goodies"
---

Tyrian comes with a number of handy functions built-in that you can make use of and explore:

## Built-in `Cmd` goodies

These nuggets of functionality are used as commands.

- `Http.send` - Makes and HTTP request that returns the respose as a message.
- `FileReader` - Given the id of a file input field that has had a file selected, this Cmd will read either an image or text file to return an `HTMLImageElement` or `String` respectively.
- `ImageLoader` - Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
- `Logger` - A simple logger that logs to the Browsers console with a few standard headers and the log message.
- `Random` - A Cmd to generate random values.

## Built-in Pub/Sub goodies

These entries form a pub/sub relationship where you are required to store an object that holds state in your app's model, and which allows you to then subscribe to events and publish messages via given `Sub`s and `Cmd`s respectively.

- `WebSocket` - Allows you to send and receive data to/from a socket server.
- `TyrianIndigoBridge` - Allows your Tyrian app to communicates with embedded Indigo games.
