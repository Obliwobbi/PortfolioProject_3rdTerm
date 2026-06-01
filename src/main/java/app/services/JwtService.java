package app.services;

import app.dto.login.AuthUserDTO;
import app.dto.login.LoginUserDTO;
import app.entities.Role;
import app.utils.Utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JwtService
{

    private final String issuer;
    private final long tokenExpireTime;
    private final String secretKey;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService()
    {
        if (System.getenv("DEPLOYED") != null)
        {
            this.issuer = System.getenv("ISSUER");
            this.tokenExpireTime = Long.parseLong(System.getenv("TOKEN_EXPIRE_TIME"));
            this.secretKey = System.getenv("SECRET_KEY");
        }
        else
        {
            this.issuer = Utils.getPropertyValue("ISSUER", "config.properties");
            this.tokenExpireTime = Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties"));
            this.secretKey = Utils.getPropertyValue("SECRET_KEY", "config.properties");
        }

        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public String createToken(LoginUserDTO loginUserDTO)
    {
        try
        {
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(loginUserDTO.email())
                    .withClaim("userId", loginUserDTO.id())
                    .withClaim("role", loginUserDTO.role())
                    .withClaim("companyId", loginUserDTO.companyId())
                    .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpireTime))
                    .sign(algorithm);
        } catch (Exception e)
        {
            throw new RuntimeException("Could not create token", e);
        }
    }

    public DecodedJWT verifyToken(String token)
    {
        try
        {
            return verifier.verify(token);
        } catch (Exception e)
        {
            throw new app.exceptions.UnauthorizedException("Invalid or expired token");
        }
    }

    public AuthUserDTO getAuthUserFromToken(String token){
        DecodedJWT decodedJWT = verifyToken(token);

        return new AuthUserDTO(
                decodedJWT.getClaim("userId").asLong(),
                decodedJWT.getSubject(),
                Role.valueOf(decodedJWT.getClaim("role").asString()),
                decodedJWT.getClaim("companyId").asLong()
        );
    }

    public String getEmailFromToken(String token)
    {
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);

        return decodedJWT.getSubject();
    }
}