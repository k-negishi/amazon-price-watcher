package com.acme.amazonpricewatcher.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
@ConfigurationPropertiesBinding
class AmazonItemsConverter : Converter<String, List<AmazonItemProperties.ItemConfig>> {
    private val objectMapper = jacksonObjectMapper()

    override fun convert(source: String): List<AmazonItemProperties.ItemConfig> {
        val trimmed = source.trim()
        if (trimmed.isBlank()) {
            return emptyList()
        }
        return objectMapper.readValue(trimmed, object : TypeReference<List<AmazonItemProperties.ItemConfig>>() {})
    }
}
