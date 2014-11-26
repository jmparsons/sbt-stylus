/*global process, require */

var fs = require("fs"),
    jst = require("jstranspiler"),
    nodefn = require("when/node"),
    mkdirp = require("mkdirp"),
    path = require("path"),
    stylus = require("stylus"),
    nib = require("nib");

var promised = {
    mkdirp: nodefn.lift(mkdirp),
    readFile: nodefn.lift(fs.readFile),
    writeFile: nodefn.lift(fs.writeFile)
  };

var args = jst.args(process.argv);

function processor(input, output) {

  return promised.readFile(input, "utf8").then(function(contents) {
    var result = null;

    var options = args.options;
    options.filename = input;

    var style = stylus(contents, options)
    if (options.useNib) style.use(nib());
    if (options.includeCSS) style.set('include css', true);

    if (options.inlineImages) {
      style.define(options.inlineFunction, stylus.url({
        paths : [path.dirname(output)],
        limit : options.inlineThreshold
      }));
    }

    var sourcemap = null;
    if (options.sourceMap) {
      sourcemap = {};
    }
    if (options.sourceMapInline) {
      sourcemap = sourcemap || {};
      sourcemap.inline = true;
    }
    if (options.sourceMapRoot) {
      sourcemap = sourcemap || {};
      sourcemap.sourceRoot = options.sourceMapRoot;
    }
    if (options.sourceMapBase) {
      sourcemap = sourcemap || {};
      sourcemap.basePath = options.sourceMapBase;
    }
    if (sourcemap) {
      style.set('filename', input);
      style.set('sourcemap', sourcemap);
    }

    style.render(function (err, css) {
        if (err) {
          throw parseError(input, contents, err);
        } else {
          result = {
            css: css,
            style: style
          };
        }
      });
    return result;

  }).then(function(result) {
    return promised.mkdirp(path.dirname(output)).yield(result);

  }).then(function(result) {
    if (result.style.options.compress && result.style.sourcemap && !result.style.options.sourceMapInline) {
      result.css = result.css.replace(/\.css\.map/g, ".min.css.map");
    }
    return promised.writeFile(output, result.css, "utf8").yield(result);

  }).then(function(result) {
    if (result.style.sourcemap && !result.style.options.sourceMapInline) {
      return promised.writeFile(output + ".map", JSON.stringify(result.style.sourcemap), "utf8").yield(result);
    }
    return result;

  }).then(function(result) {
    return {
      source: input,
      result: {
          filesRead: [input].concat(result.style.deps()),
          filesWritten: [output].concat((result.style.sourcemap && !result.style.options.sourceMapInline) ? [output + ".map"] : [])
      }
    };
  }).catch(function(e) {
    if (jst.isProblem(e)) return e; else throw e;
  });

}

jst.process({processor: processor, inExt: ".styl", outExt: (args.options.compress? ".min.css" : ".css")}, args);

/**
 * Utility to take a stylus error object and coerce it into a Problem object.
 */
function parseError(input, contents, err) {
  var errLines = err.message.split("\n");
  var lineNumber = (errLines.length > 0? errLines[0].substring(errLines[0].indexOf(":") + 1) : 0);
  var lines = contents.split("\n", lineNumber);
  return {
    message: err.name + ": " + (errLines.length > 2? errLines[errLines.length - 2] : err.message),
    severity: "error",
    lineNumber: parseInt(lineNumber),
    characterOffset: 0,
    lineContent: (lineNumber > 0 && lines.length >= lineNumber? lines[lineNumber - 1] : "Unknown line"),
    source: input
  };
}


