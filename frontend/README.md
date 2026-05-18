# LedgerFlow Frontend

Minimal React frontend for the Splitwise-style LedgerFlow API.

## Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend is configured to call the deployed backend:

```env
VITE_API_BASE_URL=https://ledgerflow-5zs2.onrender.com
```

For local backend testing, temporarily set `VITE_API_BASE_URL` to `http://localhost:8080`.

## Deploying On Vercel

This repo has a root `vercel.json`, so Vercel can build the frontend from the
`frontend` folder while leaving the Spring Boot backend alone.

Use these settings if Vercel asks:

- Install command: `cd frontend && npm install`
- Build command: `cd frontend && npm run build`
- Output directory: `frontend/dist`

Set this environment variable in Vercel:

```env
VITE_API_BASE_URL=https://ledgerflow-5zs2.onrender.com
```

After Vercel gives you a frontend URL, add that URL to the backend CORS setting
on Render:

```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*,https://your-frontend.vercel.app
```

## Notes

The app is wired to the backend routes currently present in the Spring Boot controllers:

- `/api/auth`
- `/api/group`
- `/api/expenses`
- `/api/settlement`

If browser requests fail with a CORS error, the backend must allow the frontend origin.
