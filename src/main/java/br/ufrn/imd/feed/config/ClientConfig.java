package br.ufrn.imd.feed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import reactor.core.publisher.Mono;

@Configuration
public class ClientConfig {
    @Value("${gateaway.api.address}")
    private String baseUrl;

    @LoadBalanced
    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(
                        httpStatusCode -> HttpStatus.NOT_FOUND == httpStatusCode,
                        response -> Mono.empty())
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServicesCommunicationException(
                                "Erro durante a comunicação com serviço externo: " + response.toString())))
                .build();
    }

    @Bean
    SongDayClient songDayClient() {
        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient()))
                .build()
                .createClient(SongDayClient.class);
    }

    @Bean
    SongsClient songsClient() {
        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient()))
                .build()
                .createClient(SongsClient.class);
    }
}
