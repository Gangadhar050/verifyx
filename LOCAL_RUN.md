# VerifyX local run

This build starts locally without MySQL or SMTP environment variables.

Defaults:
- Backend: http://localhost:8080
- Frontend CORS: http://localhost:5173
- Database: embedded H2 file database under ./data
- Mail: local mode when MAIL_USERNAME is empty; OTP is printed in the backend console.

Run:

```powershell
.\\mvnw.cmd clean install -DskipTests
.\\mvnw.cmd spring-boot:run
```

For real email, set MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM and optionally MAIL_SMTP_AUTH / MAIL_STARTTLS.
For document AI extraction, set a real GEMINI_API_KEY.
