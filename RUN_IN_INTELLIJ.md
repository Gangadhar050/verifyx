# Run VerifyX backend in IntelliJ

1. Open this folder (the folder containing `pom.xml`).
2. Use Java 21 as the Project SDK.
3. Allow IntelliJ to import the Maven project.
4. Run `com.verify_x.VerifyXApplication`.
5. Backend URL: `http://localhost:8080`
6. Swagger: `http://localhost:8080/swagger-ui/index.html`
7. Frontend CORS is configured for `http://localhost:5173`.

The default `local` Spring profile contains the supplied Gmail SMTP settings and uses H2, so MySQL is not required for local startup.
`application-local.properties` is gitignored because it contains a mail credential.

If Gmail delivery fails, local startup/registration is not interrupted; the OTP is also written to the backend console in local mode.
