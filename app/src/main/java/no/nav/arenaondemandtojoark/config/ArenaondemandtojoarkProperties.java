package no.nav.arenaondemandtojoark.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("arenaondemandtojoark")
public class ArenaondemandtojoarkProperties {

	private final Endpoints endpoints = new Endpoints();
	private final SftpProperties sftp = new SftpProperties();

	@Data
	public static class Endpoints {
		@NotNull
		private AzureEndpoint dokarkiv;

		@NotNull
		private String ondemand;
	}

	@Data
	public static class AzureEndpoint {
		@NotEmpty
		private String url;

		@NotEmpty
		private String scope;
	}

	@Data
	@Validated
	public static class SftpProperties {
		@ToString.Exclude
		@NotEmpty
		private String host;

		@ToString.Exclude
		@NotEmpty
		private String privateKey;

		@ToString.Exclude
		@NotEmpty
		private String hostKey;

		@ToString.Exclude
		@NotEmpty
		private String username;

		@ToString.Exclude
		@NotEmpty
		private String port;
	}
}
