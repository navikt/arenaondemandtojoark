package no.nav.arenaondemandtojoark.util;


import org.slf4j.MDC;

import static java.util.UUID.randomUUID;

public class MDCGenerate {

	public static final String NAV_CALL_ID = "Nav-Callid";
	public static void generateNewCallId() {
		MDC.put(NAV_CALL_ID, randomUUID().toString());
	}

	public static void clearCallId() {
		if (MDC.get(NAV_CALL_ID) != null) {
			MDC.remove(NAV_CALL_ID);
		}
	}

	private MDCGenerate() {
	}
}

