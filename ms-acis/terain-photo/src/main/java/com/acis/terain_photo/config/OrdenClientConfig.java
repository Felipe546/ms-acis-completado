package com.acis.terain_photo.config;
import org.springframework.context.annotation.*;import org.springframework.web.client.RestClient;import org.springframework.beans.factory.annotation.Value;
@Configuration public class OrdenClientConfig {@Bean RestClient ordenesRestClient(@Value("${ot-gestion.url}")String url){return RestClient.builder().baseUrl(url).build();}}
