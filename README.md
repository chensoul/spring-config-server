# spring-config-server

## How to run

Package the application using the maven command:

```bash
$ ./mvnw package
```

Setting an environment variable named `SPRING_CONFIG_ADDITIONAL_LOCATION` or `SPRING_CONFIG_IMPORT` to the location of the configuration file, for example `samples/config-repo.yml`:

```bash
$ export SPRING_CONFIG_IMPORT=samples/config-repo.yml
```

Using Java 17+ or higher, run the Config Server application:

```bash
$ java -jar target/spring-config-server-1.0.0-SNAPSHOT.jar 
```

To verify, you can use curl to fetch the configuration for the default application and profile by running:

```bash
curl -u user:password http://localhost:8888/application/default
```

## Resources

| Path                           | Description                                                     |
|--------------------------------|-----------------------------------------------------------------|
| /{app}/{profile}               | Configuration data for app in Spring profile (comma-separated). |
| /{app}/{profile}/{label}       | Add a git label                                                 |
| /{app}/{profile}{label}/{path} | An environment-specific plain text config file (at "path")      |

## Security

### HTTP Basic authentication

The server is secure with HTTP Basic authentication by Spring Security (via spring-boot-starter-security). The user name is "user" and the password is "password". You can override the password with the environment variable `SPRING_SECURITY_USER_PASSWORD`. E.g.

```bash
$ SPRING_SECURITY_USER_PASSWORD=pass123
```

### Encryption and decryption

Generate a keystore with a key pair:

```bash
keytool -genkeypair -alias mytestkey -keyalg RSA\
  -dname "CN=Web Server,OU=Unit,O=Organization,L=City,S=State,C=CN" -validity 3650 \
  -storetype PKCS12 -keystore keystore.jks -storepass changeit
```

Run the Config Server application with the environment variable `ENCRYPT_KEYSTORE_PASSWORD` set to the keystore password:

```bash
$ ENCRYPT_KEYSTORE_PASSWORD=changeit java -jar target/*.jar
```

Test encryption and decryption with the following commands:

```bash
VALUE=`curl -s -u user:password http://localhost:8888/encrypt -d hello`
curl -u user:password http://localhost:8888/decrypt -d $VALUE
```

### Enabling Mutual TLS (mTLS)

We will use the [OpenSSL](https://www.openssl.org/) command line tool to generate the certificates.

1. **Generate CA**

First of all, we need a certificate authority (CA) that both the client and the server will trust. We generate these using openssl.

```bash
mkdir -p samples/tls/ca
openssl req -new -x509 -nodes -days 365 -subj '/CN=my-ca' -keyout samples/tls/ca/ca.key -out samples/tls/ca/ca.crt
```

This now puts a private key in ca.key and a certificate in ca.crt on our filesystem. We can inspect these a little further with the following.

```bash
openssl x509 --in samples/tls/ca/ca.crt -text --noout
```

Looking at the output, we see some interesting things about our CA certificate. Most importantly the X509v3 Basic Constraints value is set CA:TRUE, telling us that this certificate can be used to sign other certificates (like CA certificates can).

2. **Generate Server key and certificate**

The server now needs a key and certificate. Key generation is simple, as usual:

```bash
mkdir -p samples/tls/server
openssl genrsa -out samples/tls/server/tls.key 2048
```

We need to create a certificate that has been signed by our CA. This means we need to generate a certificate signing
request, which is then used to produce the signed certificate.

```bash
openssl req -new -key samples/tls/server/tls.key -subj '/CN=localhost' -out samples/tls/server/tls.csr
```

This gives us a signing request for the domain of localhost as mentioned in the -subj parameter. This signing request
now gets used by the CA to generate the certificate.

```bash
openssl x509 -req -in samples/tls/server/tls.csr -CA samples/tls/ca/ca.crt -CAkey samples/tls/ca/ca.key -CAcreateserial -days 365 -out samples/tls/server/tls.crt
```

Inspecting the server certificate, you can see that it’s quite a bit simpler than the CA certificate. We’re only able to
use this certificate for the subject that we nominated; localhost.

3. **Generate Client key and certificate**

The generation of the client certificates is very much the same as the server.

```bash
mkdir -p samples/tls/client
# create a key
openssl genrsa -out samples/tls/client/tls.key 2048

# generate a signing certificate
openssl req -new -key samples/tls/client/tls.key -subj '/CN=my-client' -out samples/tls/client/tls.csr

# create a certificate signed by the CA
openssl x509 -req -in samples/tls/client/tls.csr -CA samples/tls/ca/ca.crt -CAkey samples/tls/ca/ca.key -CAcreateserial -days 365 -out samples/tls/client/tls.crt
```

The subject in this case is my-client.

The `-CAcreateserial` number also ensures that we have unique serial numbers between the server and client certificates. Again, this can be verified when you inspect the certificate.

4. **Run Config Server**

Run the Config Server application:

```bash
$ export SPRING_CONFIG_IMPORT=file:samples/config-repo-tls.yml
$ java -jar target/spring-config-server-1.0.0-SNAPSHOT.jar 
```

5. **Test with certificates and keys**

```bash
curl \
    --cacert samples/tls/ca/ca.crt \
    --cert samples/tls/client/tls.crt \
    --key samples/tls/client/tls.key \
    -u user:password \
    https://localhost:8888/application/default/main
```

## Run with Docker

Create an image with [buildpack](https://buildpacks.io/).

```bash
brew install buildpacks/tap/pack

pack build spring-config-server:1.0.0 \
  --path ./spring-config-server-1.0.0-SNAPSHOT.jar \
  --builder paketobuildpacks/builder:tiny
```

> If you will be running the image on an ARM host (such as an Apple machine with an Apple chipset), you must use a
> different builder:
>
> ```bash
> pack build spring-config-server:1.0.0 \
> --path target/spring-config-server-1.0.0-SNAPSHOT.jar \
> --builder dashaun/builder:tiny
> ```

Or you can create an image using docker build.

```bash
docker build -t spring-config-server:1.0.0 .
```

Start the container by running:

```bash
docker run -it \
  -p 8888:8888 \
  --mount type=bind,source="$(pwd)"/samples,target=/app/samples \
  -e SPRING_CONFIG_IMPORT='file:samples/config-repo-tls.yml' \
  spring-config-server:1.0.0
```

## Enabling Client Applications

Config application.properties file for the client application:

```yaml
spring.config.import: optional:configserver:http://myconfigserver:8888
```

Enabling TLS (mTLS) Authentication if the Config Server is running with TLS:

```yaml
spring.config.import: optional:configserver:http://myconfigserver:8888
spring.cloud.config.tls.enabled: true
spring.cloud.config.tls.key-store: <path-to-key-store>
spring.cloud.config.tls.key-store-type: PKCS12
spring.cloud.config.tls.key-store-password: <key-store-password>
spring.cloud.config.tls.password: <key-password>
spring.cloud.config.tls.trust-store: <path-of-trust-store>
spring.cloud.config.tls.trust-store-type: PKCS12
spring.cloud.config.tls.trust-store-password: <trust-store-password>
```