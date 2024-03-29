package me.leoyakubov.authserver.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Data
@NoArgsConstructor
@Component
public class JwtConfigProperties {

    @Value("${security.jwt.uri:}")
    private String uri;

    @Value("${security.jwt.header:}")
    private String header;

    @Value("${security.jwt.prefix:}")
    private String prefix;

    @Value("${security.jwt.expiration:1234}")
    private int expiration;

    @Value("${security.jwt.secret:}")
    private String secret;
}
