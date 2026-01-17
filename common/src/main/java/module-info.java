module gay.sylv.polycog {
	requires org.jspecify;
	exports gay.sylv.polycog.api.core;

	exports gay.sylv.polycog.impl.bootstrap to gay.sylv.polycog.client;
}
