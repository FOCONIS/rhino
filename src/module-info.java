module org.mozilla.rhino {
    exports org.mozilla.javascript;
    exports org.mozilla.javascript.annotations;
    // exports org.mozilla.javascript.ast;

    exports org.mozilla.javascript.commonjs.module;
    exports org.mozilla.javascript.commonjs.module.provider;
    exports org.mozilla.javascript.debug;

    // optimizer has to be exported for compiled classes
    exports org.mozilla.javascript.optimizer;

    // engine not part of the runtime
    // exports org.mozilla.javascript.engine;

    // exports org.mozilla.javascript.json;
    // exports org.mozilla.javascript.optimizer;
    // exports org.mozilla.javascript.regexp;
    exports org.mozilla.javascript.serialize;
    exports org.mozilla.javascript.typedarrays;
    exports org.mozilla.javascript.xml;

    // from the toolsrc dir (not part of the runtime - needs to be split later)
    exports org.mozilla.javascript.tools;
    exports org.mozilla.javascript.tools.debugger;
    exports org.mozilla.javascript.tools.jsc;
    exports org.mozilla.javascript.tools.shell;

    // required for (optional) property-change support
    requires static java.desktop;

    // required for (optional) ScriptEngineFactory
    requires static java.scripting;

// not part of the runtime
// provides javax.script.ScriptEngineFactory with
//        org.mozilla.javascript.engine.RhinoScriptEngineFactory;
}
