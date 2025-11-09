## Lending Portal Frontend

Vite + React single-page app that lets students and staff request or manage school equipment. The UI is now broken into small components and can be shipped as a static bundle behind Nginx.

### Requirements

- Node.js 20+ (for local development)
- npm (ships with Node)
- Docker 20.10+ (optional, for container builds). Compose v2 is recommended.

### Configuration File

Runtime configuration lives in `public/config/app.properties`. The app reads this file before bootstrapping React, which lets you change the backend endpoint without rebuilding the bundle.

```properties
# public/config/app.properties
API_ROOT=http://localhost:8080/api
```

Provide a full `http(s)` URL. Multiple properties are supported, but only `API_ROOT` (or `VITE_API_URL` for backward compatibility) is used today. If the file is missing, the app falls back to `import.meta.env.VITE_API_URL`.

**Supported keys**

| Key | Description | Required |
| --- | --- | --- |
| `API_ROOT` | Absolute URL of the backend (e.g., `http://localhost:8080/api`). All fetch calls get prefixed with this value. | ✅ |
| `VITE_API_URL` | Legacy alias read only if `API_ROOT` is missing. Helpful when reusing preexisting config files. | ⛔️ |

Feel free to create multiple property files (for example, `config/dev.app.properties`, `config/prod.app.properties`) and point Docker/Compose at the one you want to bake or mount. The file path inside the container is always `/usr/share/nginx/html/config/app.properties`.

### Local Development

```sh
npm install
npm run dev
```

Visit `http://localhost:5173` (or the port shown in the CLI). You can override the dev server port by setting `FRONT_PORT`.

To ship a production-like bundle locally (after editing `public/config/app.properties` if needed):

```sh
npm run preview
```

### Direct Links & Routing

Each primary screen now has its own path:

- `/dashboard` – equipment catalog (default)
- `/requests` – borrow requests list
- `/manage` – equipment admin tools (requires an ADMIN role)

You can bookmark or refresh these routes directly; the Nginx config already falls back to `index.html` so client-side routing survives hard reloads.

### Docker Image

The repo contains a multi-stage `Dockerfile` that:

1. Installs npm dependencies.
2. Copies the property file you specify (defaults to `public/config/app.properties`).
3. Builds the Vite bundle and serves the static assets from Nginx.

Build the image (optionally point at a different property file inside the repo):

```sh
docker build \
  --build-arg APP_CONFIG_FILE=config/app.properties \
  -t lending-portal-frontend:latest .
```

A starter `config/app.properties` lives in the repo with `API_ROOT=http://localhost:8080/api`. Tweak it (or point at your own file) before invoking the build so the correct backend endpoint gets baked into the image.

> The file you pass to `APP_CONFIG_FILE` must live inside the build context (this repo). It will be copied to `public/config/app.properties` before the Vite build runs.

Run it:

```sh
docker run --rm \
  -p 4173:80 \
  -v $(pwd)/public/config/app.properties:/usr/share/nginx/html/config/app.properties:ro \
  lending-portal-frontend:latest
```

To hot-swap the backend endpoint, swap the mounted file (e.g., `-v /abs/path/prod.properties:/usr/share/nginx/html/config/app.properties:ro`) and refresh the browser—no rebuild needed.
On Windows PowerShell use `${PWD}` and on Command Prompt use `%cd%` instead of `$(pwd)`.

### Docker Compose

`docker-compose.yml` wires everything together for local use. It exposes the static site on port `4173` (configurable with `FRONTEND_PORT`), copies the property file specified by `APP_CONFIG_FILE`, and bind-mounts `APP_CONFIG_PATH` so you can edit the properties locally while the container is running. Just run:

```sh
docker compose up --build
```

Useful overrides:

```sh
APP_CONFIG_FILE=config/app.properties \
APP_CONFIG_PATH=./config/app.properties \
FRONTEND_PORT=8081 \
docker compose up --build
```

When running both frontend and backend in the same Compose project, set `API_ROOT` inside the property file to something like `http://backend:8080/api` so the browser calls the correct container hostname.

### Project Structure Highlights

- `src/App.jsx` – stateful container that orchestrates authentication, equipment catalog, and requests.
- `src/components/*` – modularized UI components (auth screen, navigation, catalog, request form/table, admin form).
- `Dockerfile`, `docker-compose.yml`, `nginx.conf` – container build + runtime config.

Feel free to extend this setup with CI or additional Compose services as your backend evolves.
