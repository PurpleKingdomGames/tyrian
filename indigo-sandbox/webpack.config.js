const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  mode: 'development',
  entry: {
    index: './tyrianapp.js'
  },
  devtool: 'source-map',
  devServer: {
    static: './dist',
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'Tyrian/Indigo Sandbox',
      filename: 'index.html',
      template: 'index.html'
    }),
  ],
  output: {
    filename: '[name].bundle.js',
    path: path.resolve(__dirname, 'dist'),
    clean: true,
  },
  module: {
    rules: [{
        test: /\.js$/,
        use: [{
          loader: "scalajs-friendly-source-map-loader",
          options: {
            skipFileURLWarnings: true, // or false, default is true
            bundleHttp: true, // or false, default is true,
            cachePath: ".scala-js-sources", // cache dir name, exclude in .gitignore
            noisyCache: false, // whether http cache changes are output
            useCache: true, // false => remove any http cache processing
          }
        }],
        enforce: "pre",
        // include: [path.resolve(__dirname, 'src')],
      },
    ],
  },
};
