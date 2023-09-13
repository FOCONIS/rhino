module rhino.test {
	requires junit;
	requires org.mozilla.rhino;
	requires java.xml;
	requires org.yaml.snakeyaml;
	requires java.scripting;
	requires java.sql;
	requires java.naming;
	requires java.xml.soap;

	exports org.mozilla.classfile.tests;
// Required for Junit
	exports org.mozilla.javascript.drivers;
	opens org.mozilla.javascript.tests.es6;

	opens org.mozilla.javascript.tests;
}