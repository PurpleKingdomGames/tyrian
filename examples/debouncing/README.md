# Tyrian debouncing example

Input debouncing is a technique used to ensure that a function or action is triggered only after a certain period of inactivity of an input, typically to prevent multiple rapid calls or events from occurring in a short timeframe. This is commonly used in scenarios where user input, such as button clicks or keyboard events, can lead to actions like API calls.

This is an example of how to implement input debouncing with Tyrian according to [a recipe from Elm](https://orasund.gitbook.io/elm-cookbook/recipes-1/writing-a-single-page-application/debounced-validation).

To run the program in a browser you will need to have yarn (or npm) installed.

On first run:

```sh
yarn install
```

and from then on

```sh
yarn start
```

Then navigate to [http://localhost:1234/](http://localhost:1234/).