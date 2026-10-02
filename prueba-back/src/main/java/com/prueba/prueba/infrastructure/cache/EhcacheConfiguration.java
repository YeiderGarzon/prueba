package com.prueba.prueba.infrastructure.cache;

import java.time.Duration;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EhcacheConfiguration {
	private static final Duration LOAN_QUERY_TTL = Duration.ofSeconds(20);

	@Bean(destroyMethod = "close")
	CacheManager loanEhcacheManager() {
		CacheConfiguration<Object, Object> configuration = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(
						Object.class,
						Object.class,
						ResourcePoolsBuilder.heap(500))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(LOAN_QUERY_TTL))
				.build();

		return CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("loansById", configuration)
				.withCache("allLoans", configuration)
				.withCache("loansByApplicant", configuration)
				.build(true);
	}

	@Bean
	EhcacheLoanQueryCache loanQueryCache(CacheManager loanEhcacheManager) {
		Cache<Object, Object> byId = loanEhcacheManager.getCache("loansById", Object.class, Object.class);
		Cache<Object, Object> all = loanEhcacheManager.getCache("allLoans", Object.class, Object.class);
		Cache<Object, Object> byApplicant = loanEhcacheManager.getCache(
				"loansByApplicant", Object.class, Object.class);
		return new EhcacheLoanQueryCache(byId, all, byApplicant);
	}
}
