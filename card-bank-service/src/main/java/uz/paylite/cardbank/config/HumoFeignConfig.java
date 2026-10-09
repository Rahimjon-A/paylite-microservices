package uz.paylite.cardbank.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

public class HumoFeignConfig {

    @Bean
    public Encoder humoXmlEncoder() {

        MappingJackson2XmlHttpMessageConverter xmlConverter =
            new MappingJackson2XmlHttpMessageConverter();

        ObjectFactory<HttpMessageConverters> messageConverters =
            () -> new HttpMessageConverters(xmlConverter);

        return new SpringEncoder(messageConverters);
    }

    @Bean
    public Decoder humoXmlDecoder() {

        MappingJackson2XmlHttpMessageConverter xmlConverter =
            new MappingJackson2XmlHttpMessageConverter();

        ObjectFactory<HttpMessageConverters> messageConverters =
            () -> new HttpMessageConverters(xmlConverter);

        return new SpringDecoder(messageConverters);
    }
}
