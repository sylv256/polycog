import org.jspecify.annotations.NullMarked;

@NullMarked
module gay.sylv.polycog {
	requires org.jspecify;
	requires org.slf4j;
	requires org.apache.logging.log4j.core;
	exports gay.sylv.polycog.api.core;

	exports gay.sylv.polycog.impl.bootstrap to gay.sylv.polycog.client;
	exports gay.sylv.polycog.impl.share to gay.sylv.polycog.client;
}
