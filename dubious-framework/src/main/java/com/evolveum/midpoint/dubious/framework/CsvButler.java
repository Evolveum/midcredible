package com.evolveum.midpoint.dubious.framework;

import org.apache.commons.csv.CSVFormat;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CsvButler extends ResourceButler<CSVFormat> {

	public CsvButler(String id, Context context) {
		super(id, context);
	}

	public CsvButler(String id, Context context, CSVFormat client) {
		super(id, context, client);
	}

	@Override
	protected CSVFormat init() throws Exception {
		return super.init();
	}

	public long count() {
		return count(null);
	}

	public long count(Function<Map<String, String>, Boolean> filter) {
		// todo implement
		return 0L;
	}
}
