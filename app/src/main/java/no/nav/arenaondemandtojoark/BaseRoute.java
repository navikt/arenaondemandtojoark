package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
public abstract class BaseRoute extends RouteBuilder {

	private final AvvikService avvikService;

	protected BaseRoute(AvvikService avvikService) {
		this.avvikService = avvikService;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		onException(Exception.class)
				.bean(avvikService)
				.handled(true)
				.end();
		//@formatter:on
	}
}
