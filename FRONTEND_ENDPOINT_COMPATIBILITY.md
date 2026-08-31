# Frontend endpoint compatibility

Backend base URL: `http://localhost:8080`
Frontend origin allowed by CORS: `http://localhost:5173`

Verified/added routes used by the current frontend:

- `GET /api/auth/registration-options`
- `POST /api/auth/candidateRegister`
- `POST /api/auth/candidateLogin`
- `POST /api/auth/candidate/verify-email`
- `POST /api/auth/candidate/resend-verification`
- `POST /api/auth/hrLogin`
- `POST /api/auth/hr/forgot-password`
- `POST /api/auth/hr/reset-password`
- `GET /api/auth/me`
- `POST /api/auth/candidateLogout`
- `POST /api/auth/hrLogout`
- `GET/POST/PUT /api/candidate/profile`
- `GET/PUT /api/candidate/profile/photo`
- `POST /api/education/auto-fill/{documentType}`
- `GET /api/hr/candidates`
- `GET/DELETE /api/hr/candidates/{id}`
- `PUT /api/hr/candidates/{id}/status`
- `PUT /api/hr/candidates/{id}/verify-uan`
- `GET /api/hr/document-review/{id}`
- `GET /api/hr/document-review/document/{id}`
- `PUT /api/hr/document-review/verify/{id}`
- `PUT /api/hr/document-review/reject/{id}`
- `GET /api/hr/dashboard/report`
- `GET /api/hr/dashboard/candidates`
- `GET /api/hr/reports`
- `POST /upload/upload`
- `PUT /upload/re-upload`
- `GET /upload/my-documents`
- `GET /upload/view/{id}`

The registration-options API is backend-owned; the frontend can display the backend role/candidate-type values without maintaining its own role catalogue.
