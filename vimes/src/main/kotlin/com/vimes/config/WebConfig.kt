package com.vimes.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val requestIdInterceptor: RequestIdInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestIdInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("X-Request-Id", "Retry-After")
            .allowCredentials(true)
    }

    @Bean
    fun characterEncodingFilter(): CharacterEncodingFilter {
        val filter = CharacterEncodingFilter()
        filter.encoding = "UTF-8"
        filter.setForceEncoding(true)
        return filter
    }
}
