# omkara

A starting point for Clojure web apps based on the Om/React.js library.

It makes good the promise of "write once, run client and server side" for HTML templates.  While the repository in it's current state is a bit rough, I hope you will take the time to fork it, make it better, and submit pull requests so that we, as the Clojure community, can have an example project to point to for those who want to get started with [Om/React](https://github.com/swannodette/om).

## Server-side rendering

The `omkara.react` namespace is a simple implementation of server-side rendering of [Om/React](https://github.com/swannodette/om) templates.  Adjusting to your own needs should be straight-forward once you understand the basic architecture.

The [react.js](https://github.com/facebook/react) library can be used outside a browser, however, you need to supply a global object named `global` or `self` for the library to install itself into.  Omkara uses the `:preamble` option of [cljsbuild](https://github.com/emezeske/lein-cljsbuild) to create a javascript file along the lines of

```javascript
var global = {}

/* react.js source which installs itself in global.React */

var React = global.React; // create a React property on the global js object

/* compiled clojurescript code follows */
```

This can be fed to [Rhino](https://github.com/mozilla/rhino) and allows you to call `React.renderComponentToString()` to perform server-side rendering.  See the comments in `omkara.react` for some gotchas when dealing with Rhino.

## Understanding Om

If you're looking into Om 0.1.0-SNAPSHOT for the first time, here are a couple of things to keep in mind:

1. `om.core/component` doesn't create a React component.  It creates an object which implements `IRender` but which needs to be passed to `om.core/pure` to create an actual component via `React.createClass`.

2. `om.core/root` takes a function as its second argument which returns rendering code wrapped in `om.core/component`.  See `omkara.datetime/render` for how to eventually create a React component.

3. You will want to make your own global state atom and pass that to `om.core/root` as the first argument.  Otherwise, you won't have a reference to update when app data needs to be updated. See the code in `omkara.datetime` for details.

## If you're new to Clojurescript...

You should be aware of the following *peculiarities* before charging in:

1. The compiler option `:optimizations :none` is completely different than `:optimizations :simple|:whitespace|:advanced`.  The output file will asynchronously load files in the `:output-dir` which you will need to either place in a folder you are already serving static files from, or have special code to serve only during development.  The other optimization options will pack everything into a single file.

2. The options `:libs` and `:foreign-libs` aren't particularly helpful.  All they do is trigger a recompile if one of the referenced files changes.  They do *not* add the javascript library code to the `:output-to` javascript file.  Use `:preamble` for this instead.

3. The option `:preamble` only works for `:optimizations :simple|:whitespace|:advanced`.  It does nothing for `:optimization :none`.

4. It's easy to create compiled ClojureScript which will not run headless in Rhino.  Particularly, using `clojure.browser.repl` brings in code which manipulates `window.location` which means that any embedded or headless build needs to exclude this code.  See the "embedded" cljsbuild for a way to conditionally bring in the nREPL code by keeping it in a separate directory.

## License

Copyright © 2013 brendanyounger

Distributed under the Eclipse Public License, the same as Clojure.
