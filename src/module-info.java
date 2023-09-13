module org.mozilla.rhino {
	exports org.mozilla.javascript;
	exports org.mozilla.javascript.annotations;
	exports org.mozilla.javascript.tools.shell;
	exports org.mozilla.javascript.ast;
	exports org.mozilla.javascript.tools;
	exports org.mozilla.javascript.commonjs.module;
	exports org.mozilla.javascript.commonjs.module.provider;
	exports org.mozilla.javascript.typedarrays;
	exports org.mozilla.javascript.optimizer;
	exports org.mozilla.javascript.engine;
	exports org.mozilla.javascript.serialize;
	exports org.mozilla.javascript.json;
	exports org.mozilla.classfile;
	// required for (optional) property-change support
	requires static java.desktop;

	// required for (optional) ScriptEngineFactory
	requires static java.scripting;

	provides javax.script.ScriptEngineFactory with org.mozilla.javascript.engine.RhinoScriptEngineFactory;
}