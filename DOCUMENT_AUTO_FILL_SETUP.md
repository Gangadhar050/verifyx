# VerifyX education document auto-fill

The candidate education page calls:

`POST /api/education/auto-fill/{documentType}`

Supported document types:
- `TENTH_MARKS_CARD`
- `TWELFTH_MARKS_CARD`
- `DEGREE_CERTIFICATE`
- `MASTERS_MARKS_CARD`

## How extraction works

1. **Text-based PDFs**: backend first tries local PDF text extraction. No Gemini key is required.
2. **Scanned PDFs / JPG / JPEG / PNG**: backend uses Gemini vision/document extraction.

For scanned/image documents, configure a real environment variable before starting IntelliJ:

`GEMINI_API_KEY=<your real Gemini API key>`

Do not put the real key in Git.

Frontend origin is allowed by CORS:

`http://localhost:5173`

Backend runs on:

`http://localhost:8080`

The endpoint requires a logged-in candidate JWT because it is protected with `ROLE_CANDIDATE`.
